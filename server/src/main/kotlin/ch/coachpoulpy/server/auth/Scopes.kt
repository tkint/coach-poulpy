package ch.coachpoulpy.server.auth

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.security.BearerAuthSecurity
import org.http4k.security.Security

/**
 * Marks a route for signed-in callers holding every one of [scopes] ([ADR 0020]): an anonymous
 * caller gets 401, a caller missing a scope 403. With no scope, any signed-in caller passes.
 * The OpenAPI description shows these routes as bearer-token ones.
 *
 * [ADR 0020]: https://github.com/tkint/coach-poulpy/blob/main/docs/adr/0020-every-credential-resolves-to-one-caller.md
 */
fun requires(vararg scopes: Scope): Security = BearerAuthSecurity(
    Filter { next ->
        { request ->
            when (val caller = request.caller) {
                Caller.Anonymous -> Response(UNAUTHORIZED).header("WWW-Authenticate", "Bearer")
                is Caller.Signed ->
                    if (caller.scopes.containsAll(scopes.toList())) {
                        next(request)
                    } else {
                        val needed = scopes.joinToString(" ") { it.value }
                        Response(FORBIDDEN).header("WWW-Authenticate", "Bearer error=\"insufficient_scope\", scope=\"$needed\"")
                    }
            }
        }
    },
)
