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

/**
 * Marks a route for the admin area (#335): an anonymous caller gets 401, and a caller that isn't
 * an admin signed in through the website gets 403. Admin is a role on the account, never a scope,
 * and a personal access token or a third-party app never reaches these routes (ADR 0020).
 */
fun requiresAdmin(): Security = BearerAuthSecurity(
    Filter { next ->
        { request ->
            when (val caller = request.caller) {
                Caller.Anonymous -> Response(UNAUTHORIZED).header("WWW-Authenticate", "Bearer")
                is Caller.Signed ->
                    if (caller.role == Role.Admin && caller.client == Client.Website) next(request) else Response(FORBIDDEN)
            }
        }
    },
)
