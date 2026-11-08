package ch.coachpoulpy.server.auth

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.UNAUTHORIZED

/**
 * Checks one kind of credential, recognised by its prefix (`cp_at_`, `cp_pat_`...), and
 * returns the caller it belongs to, or null when the credential is unknown, expired or
 * revoked. Adding a way to sign in is adding one of these; no route changes.
 */
fun interface Authenticator {
    fun authenticate(credential: String): Caller.Signed?
}

/**
 * The one filter every API request passes through. No credential gives the anonymous caller; a
 * `Bearer` token goes to the authenticator registered for its prefix. Anything else, including a
 * token no authenticator accepts, is a 401, so a bad credential is never quietly treated as no
 * credential.
 *
 * With [cookies], a request with no `Authorization` header may carry the website's access-token
 * cookie instead. It must belong to a website sign-in, a stale or foreign one counts as no
 * credential (the sign-in page itself must still work), and a write that doesn't come from the
 * website's own pages is a 403 (CSRF, ADR 0020).
 */
fun authenticate(authenticators: Map<String, Authenticator>, cookies: WebCookies? = null): Filter = Filter { next ->
    { request ->
        val header = request.header("Authorization")
        if (header == null) {
            val caller = cookies?.accessToken(request)?.let { token ->
                authenticators.entries.firstOrNull { (prefix, _) -> token.startsWith(prefix) }?.value?.authenticate(token)
            }?.takeIf { it.client == Client.Website }
            when {
                caller == null -> next(request.withCaller(Caller.Anonymous))
                !cookies.originAllowed(request) -> Response(FORBIDDEN)
                else -> next(request.withCaller(caller))
            }
        } else {
            val token = header.removePrefix("Bearer ").takeIf { it != header }?.trim().orEmpty()
            val caller = authenticators.entries
                .firstOrNull { (prefix, _) -> token.startsWith(prefix) }
                ?.value?.authenticate(token)
            if (caller == null) unauthorized() else next(request.withCaller(caller))
        }
    }
}

private fun unauthorized() = Response(UNAUTHORIZED).header("WWW-Authenticate", "Bearer")
