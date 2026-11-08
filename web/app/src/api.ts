// The account endpoints the email links need (docs/the-api.md). The server allows this site's
// origin (ADR 0021); during `npm run dev`, Vite proxies /api to the local server.
export const apiBase = import.meta.env.VITE_API_URL ?? (import.meta.env.DEV ? '/api' : 'https://api.coach-poulpy.com')

/** Done, the link is used or expired, the password is refused, or the server can't be reached. */
export type LinkAnswer = 'done' | 'expired' | 'invalid-password' | 'offline'

async function post(path: string, body: object): Promise<LinkAnswer> {
  try {
    const response = await fetch(`${apiBase}/api/v1${path}`, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (response.ok) return 'done'
    if (response.status === 400) {
      const problem = await response.json().catch(() => ({}))
      return problem.error === 'invalid-password' ? 'invalid-password' : 'expired'
    }
    return 'offline'
  } catch {
    return 'offline'
  }
}

export const confirmEmail = (token: string) => post('/accounts/confirm', { token })
export const setNewPassword = (token: string, password: string) => post('/password-resets/confirm', { token, password })
export const requestPasswordReset = (email: string) => post('/password-resets', { email })
