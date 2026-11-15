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

export async function authorized(path: string, method = 'GET', body?: object): Promise<Response> {
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

/** The last email the server tried to send since it started: when, which kind, and the mail server's error if it refused it. */
export type LastSend = { at: string; kind: 'confirmation' | 'password-reset' | 'test'; sent: boolean; error: string | null }

/** What the server can do right now (#340): for now, whether a mail server is configured and how the last send went. */
export type AdminStatus = { mail: { configured: boolean; lastSend: LastSend | null } }

/** An account whose email isn't confirmed yet. */
export type PendingRegistration = { email: string; signedUpAt: string; linkExpiresAt: string | null }

/** The server's status for the admin, or nothing when it can't be loaded. */
export async function getStatus(): Promise<AdminStatus | undefined> {
  try {
    const response = await authorized('/admin/status')
    return response.ok ? ((await response.json()) as AdminStatus) : undefined
  } catch {
    return undefined
  }
}

/** The accounts not confirmed yet, newest first, or nothing when they can't be loaded. */
export async function pendingRegistrations(): Promise<PendingRegistration[] | undefined> {
  try {
    const response = await authorized('/admin/pending-registrations')
    return response.ok ? ((await response.json()) as { registrations: PendingRegistration[] }).registrations : undefined
  } catch {
    return undefined
  }
}


/** An exercise as the catalog's JSON has it: texts are maps from language to string, enums are uppercase words. Fields the editor doesn't edit (pictures) travel as they came. */
export type ExerciseJson = {
  slug: string
  name: Record<string, string>
  aliases?: Record<string, string[]>
  movementPattern: string
  muscleGroups: string[]
  /** Each need is the alternatives of which one must be at hand. */
  equipment?: string[][]
  optionalEquipment?: string[]
  measurement: string
  perSide?: boolean
  focus?: string[]
  positions?: string[]
  jumps?: boolean
  instructions?: Record<string, string>[]
  variationNote?: Record<string, string>
  retired?: boolean
  [other: string]: unknown
}

/** One exercise of the catalog draft as the admin lists it: the catalog's JSON, whether a version holds it, what changed since and the shipped languages it lacks. */
export type CatalogExercise = {
  exercise: ExerciseJson
  published: boolean
  change: 'added' | 'edited' | 'retired' | 'offered' | null
  missing: string[]
}
/** A column the catalog table sorts on. */
export type CatalogSort = 'name' | 'pattern' | 'languages' | 'change' | 'status'
/** The catalog table's question; an empty string asks for no filter. Lists are comma-separated lowercase words. */
export type CatalogQuery = {
  q: string
  pattern: string
  muscles: string
  positions: string
  equipment: string
  translation: string
  content: string
  lifecycle: string
  /** Comma-separated columns, the most important first, each with a leading - for descending: `-change,name`. Empty for the catalog's order. */
  sort: string
  page: number
  size: number
}
export type CatalogPage = { exercises: CatalogExercise[]; total: number }

/** One page of the catalog draft's exercises, or nothing when it can't be loaded. */
export async function listCatalogExercises(query: CatalogQuery): Promise<CatalogPage | undefined> {
  const params = new URLSearchParams()
  for (const [name, value] of Object.entries(query)) if (value !== '') params.set(name, String(value))
  try {
    const response = await authorized(`/admin/catalog/exercises?${params}`)
    return response.ok ? ((await response.json()) as CatalogPage) : undefined
  } catch {
    return undefined
  }
}

/** One exercise of the draft, or nothing when it doesn't exist or can't be loaded. */
export async function getCatalogExercise(slug: string): Promise<CatalogExercise | undefined> {
  try {
    const response = await authorized(`/admin/catalog/exercises/${encodeURIComponent(slug)}`)
    return response.ok ? ((await response.json()) as CatalogExercise) : undefined
  } catch {
    return undefined
  }
}

/** What saving an exercise answered: the saved exercise, a slug that is taken or published, the reason the server refused it, or that it couldn't be reached. */
export type SaveExerciseAnswer = { saved: CatalogExercise } | { error: 'slug-taken' | 'slug-published' | 'unknown-exercise' | 'invalid-exercise' | 'failed'; reason?: string }

/** Adds [exercise] to the draft, or with a [slug] replaces that exercise of it. */
export async function saveCatalogExercise(slug: string | undefined, exercise: ExerciseJson): Promise<SaveExerciseAnswer> {
  try {
    const response = await authorized(slug ? `/admin/catalog/exercises/${encodeURIComponent(slug)}` : '/admin/catalog/exercises', slug ? 'PUT' : 'POST', exercise)
    if (response.ok) return { saved: (await response.json()) as CatalogExercise }
    const problem = (await response.json().catch(() => ({}))) as { error?: string; reason?: string }
    const known = ['slug-taken', 'slug-published', 'unknown-exercise', 'invalid-exercise'] as const
    const error = known.find((one) => one === problem.error)
    return error ? { error, reason: problem.reason } : { error: 'failed' }
  } catch {
    return { error: 'failed' }
  }
}

/** One changed field of an edited exercise: its path in the exercise JSON, such as `name.fr`, and its value in the latest version and in the draft (null when absent). */
export type FieldChange = { field: string; before: unknown; after: unknown }

/** The fields of one exercise that its draft changed since the latest version; empty when it has none, nothing when it can't be loaded. */
export async function changedFields(slug: string): Promise<FieldChange[] | undefined> {
  try {
    const response = await authorized('/admin/catalog/changes')
    if (!response.ok) return undefined
    const changes = (await response.json()) as { edited: { slug: string; fields: FieldChange[] }[] }
    return changes.edited.find((one) => one.slug === slug)?.fields ?? []
  } catch {
    return undefined
  }
}

/** An exercise a publication would add, retire or offer again. */
export type ChangedExercise = { slug: string; name: Record<string, string> }
/** An edited exercise and the fields its draft changed. */
export type EditedExercise = { slug: string; name: Record<string, string>; fields: FieldChange[] }
/** An exercise that lacks a text in some shipped languages, so it can't be published yet. */
export type MissingText = { slug: string; name: Record<string, string>; languages: string[] }
/** What publishing would do, per exercise: the draft against each exercise's latest published revision. */
export type CatalogChanges = { added: ChangedExercise[]; edited: EditedExercise[]; retired: ChangedExercise[]; offered: ChangedExercise[]; missing: MissingText[] }

/** What publishing would change, or nothing when it can't be loaded. */
export async function getCatalogChanges(): Promise<CatalogChanges | undefined> {
  try {
    const response = await authorized('/admin/catalog/changes')
    return response.ok ? ((await response.json()) as CatalogChanges) : undefined
  } catch {
    return undefined
  }
}

/** What publishing answered: the exercises that got a new revision, or why not. */
export type PublishAnswer = { published: string[] } | { error: 'missing-translations' | 'no-changes' | 'failed' }

/** Publishes the draft revision of each of [slugs]; the other exercises are not written. */
export async function publishCatalog(slugs: string[]): Promise<PublishAnswer> {
  try {
    const response = await authorized('/admin/catalog/publish', 'POST', { slugs })
    if (response.ok) return { published: ((await response.json()) as { slugs: string[] }).slugs }
    const problem = (await response.json().catch(() => ({}))) as { error?: string }
    return problem.error === 'missing-translations' || problem.error === 'no-changes' ? { error: problem.error } : { error: 'failed' }
  } catch {
    return { error: 'failed' }
  }
}

/** Retires the exercise [slug] in the draft; it stays offered until a publication carries it. True when it worked. */
export async function retireCatalogExercise(slug: string): Promise<boolean> {
  try {
    return (await authorized(`/admin/catalog/exercises/${encodeURIComponent(slug)}/retire`, 'POST')).ok
  } catch {
    return false
  }
}
