// The admin's session (docs/the-api.md, "The website's sign-in"): the server keeps the tokens in
// HttpOnly cookies, so this code never sees them. Every call sends the cookies (`credentials`), and a
// 401 is answered once by swapping the refresh cookie for new ones before the call is made again.
import { apiBase } from './api'

export type Me = { accountId: string; email: string; role: string }
export type AdminAccount = { id: string; email: string; role: string; confirmed: boolean; createdAt: string; disabledAt: string | null }
export type AuditEntry = { actorEmail: string | null; action: string; target: string; at: string }

/** Signed in, wrong credentials (or not the role asked for), too many tries, or the server can't be reached. */
export type SignInAnswer = 'signedIn' | 'refused' | 'throttled' | 'offline'

async function call(method: string, path: string, body?: object): Promise<Response> {
  return fetch(`${apiBase}/api/v1${path}`, {
    method,
    credentials: 'include',
    headers: body ? { 'content-type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
}

async function authorized(path: string, method = 'GET', body?: object): Promise<Response> {
  const first = await call(method, path, body)
  if (first.status !== 401) return first
  const refreshed = await call('POST', '/web/session/refresh')
  return refreshed.ok ? call(method, path, body) : first
}

/** Who the cookies sign in, or nobody. */
export async function currentAccount(): Promise<Me | undefined> {
  const response = await authorized('/me')
  return response.ok ? ((await response.json()) as Me) : undefined
}

/** Signs in; with `only`, an account of another role is signed out again at once and `refused`, so a screen never says who has that role. */
export async function signIn(email: string, password: string, only?: string): Promise<SignInAnswer> {
  try {
    const response = await call('POST', '/web/session', { email, password })
    if (response.status === 429) return 'throttled'
    if (!response.ok) return response.status === 400 ? 'refused' : 'offline'
    const me = await currentAccount()
    if (me && (!only || me.role === only)) return 'signedIn'
    // This account has no business here: end the session this sign-in just made.
    await signOut()
    return 'refused'
  } catch {
    return 'offline'
  }
}

export async function signOut(): Promise<void> {
  await call('DELETE', '/web/session').catch(() => undefined)
}

/** The admins' recent changes, newest first, or nothing when they can't be loaded. */
export async function auditLog(): Promise<AuditEntry[] | undefined> {
  try {
    const response = await authorized('/admin/audit')
    return response.ok ? ((await response.json()) as { entries: AuditEntry[] }).entries : undefined
  } catch {
    return undefined
  }
}

/** What the admin asks of the accounts table: filters (empty means all), one sort, and a page from 1. */
export type AccountsQuery = {
  q: string
  role: string
  confirmed: string
  signIn: string
  sort: 'email' | 'role' | 'confirmed' | 'signIn' | 'created'
  order: 'asc' | 'desc'
  page: number
  size: number
}

export type AccountsPage = { accounts: AdminAccount[]; total: number }

/** One page of accounts for the admin (#336), or nothing when it can't be loaded. */
export async function listAccounts(query: AccountsQuery): Promise<AccountsPage | undefined> {
  const params = new URLSearchParams()
  for (const [name, value] of Object.entries(query)) if (value !== '') params.set(name, String(value))
  try {
    const response = await authorized(`/admin/accounts?${params}`)
    return response.ok ? ((await response.json()) as AccountsPage) : undefined
  } catch {
    return undefined
  }
}

/** One account, for a link that opens it; nothing when it doesn't exist or can't be loaded. */
export async function getAccount(id: string): Promise<AdminAccount | undefined> {
  try {
    const response = await authorized(`/admin/accounts/${encodeURIComponent(id)}`)
    return response.ok ? ((await response.json()) as AdminAccount) : undefined
  } catch {
    return undefined
  }
}

/** Disables or enables an account; true when the server did it. */
export async function setAccountDisabled(id: string, disabled: boolean): Promise<boolean> {
  try {
    return (await authorized(`/admin/accounts/${id}/${disabled ? 'disable' : 'enable'}`, 'POST')).ok
  } catch {
    return false
  }
}

/** Deletes an account, once its email is typed again; true when the server did it. */
export async function deleteAccount(id: string, confirmEmail: string): Promise<boolean> {
  try {
    return (await authorized(`/admin/accounts/${id}`, 'DELETE', { confirmEmail })).ok
  } catch {
    return false
  }
}

/** The server settings the admin changes without a deploy (#339): the from-address in use, saved here or MAIL_FROM. */
export type AdminSettings = { mailFrom: string | null; mailFromSaved: boolean }

/** The settings, or nothing when they can't be loaded. */
export async function getSettings(): Promise<AdminSettings | undefined> {
  try {
    const response = await authorized('/admin/settings')
    return response.ok ? ((await response.json()) as AdminSettings) : undefined
  } catch {
    return undefined
  }
}

/** Saved, refused because it isn't an email address, or not reached. */
export type SaveAnswer = 'saved' | 'invalid' | 'failed'

/** Saves the from-address of the next emails. */
export async function saveMailFrom(mailFrom: string): Promise<SaveAnswer> {
  try {
    const response = await authorized('/admin/settings/mail-from', 'POST', { mailFrom })
    if (response.ok) return 'saved'
    return response.status === 400 ? 'invalid' : 'failed'
  } catch {
    return 'failed'
  }
}

/** What the test email did: sent, refused with what the mail server said, or the server wasn't reached. */
export type TestEmailAnswer = { sent: true } | { sent: false; error: string } | undefined

/** Sends a test email to the signed-in admin with the saved settings. */
export async function sendTestEmail(): Promise<TestEmailAnswer> {
  try {
    const response = await authorized('/admin/settings/test-email', 'POST')
    if (!response.ok) return undefined
    const body = (await response.json()) as { sent: boolean; error: string | null }
    return body.sent ? { sent: true } : { sent: false, error: body.error ?? '' }
  } catch {
    return undefined
  }
}
