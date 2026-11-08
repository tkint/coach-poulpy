// The admin's session (docs/the-api.md, "The website's sign-in"): the server keeps the tokens in
// HttpOnly cookies, so this code never sees them. Every call sends the cookies (`credentials`), and a
// 401 is answered once by swapping the refresh cookie for new ones before the call is made again.
import { apiBase } from './api'

export type Me = { accountId: string; email: string; role: string }
export type AuditEntry = { actorEmail: string | null; action: string; target: string; at: string }

/** Signed in as an admin, wrong credentials or not an admin, too many tries, or the server can't be reached. */
export type SignInAnswer = 'admin' | 'refused' | 'throttled' | 'offline'

async function call(method: string, path: string, body?: object): Promise<Response> {
  return fetch(`${apiBase}/api/v1${path}`, {
    method,
    credentials: 'include',
    headers: body ? { 'content-type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
}

async function authorized(path: string): Promise<Response> {
  const first = await call('GET', path)
  if (first.status !== 401) return first
  const refreshed = await call('POST', '/web/session/refresh')
  return refreshed.ok ? call('GET', path) : first
}

/** Who the cookies sign in, or nobody. */
export async function currentAccount(): Promise<Me | undefined> {
  const response = await authorized('/me')
  return response.ok ? ((await response.json()) as Me) : undefined
}

export async function signIn(email: string, password: string): Promise<SignInAnswer> {
  try {
    const response = await call('POST', '/web/session', { email, password })
    if (response.status === 429) return 'throttled'
    if (!response.ok) return response.status === 400 ? 'refused' : 'offline'
    const me = await currentAccount()
    if (me?.role === 'admin') return 'admin'
    // A member has no business here: end the session this sign-in just made.
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
