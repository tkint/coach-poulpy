package ch.coachpoulpy.server.auth

import java.time.Duration
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies

/**
 * How the website holds its sign-in (ADR 0020): the same tokens as the app, in HttpOnly, Secure,
 * SameSite=Strict cookies the page's script can't read. The access token's cookie is sent to the
 * whole API; the refresh token's only to the session routes, so it travels as little as possible.
 *
 * Cookies are sent by the browser on its own, so every write made with one must come from the
 * website: [originAllowed] checks the `Origin` header, and SameSite=Strict and the CORS policy
 * (which allows only the website) back it up.
 */
class WebCookies(private val websiteOrigin: String) {
    /** The access token's cookie. The `__Host-` prefix makes browsers refuse it unless it is Secure, for the whole path and with no Domain. */
    fun access(token: String, lifetime: Duration): Response.() -> Response = { cookie(cookie(ACCESS, token, lifetime, "/")) }

    fun refresh(token: String, lifetime: Duration): Response.() -> Response = { cookie(cookie(REFRESH, token, lifetime, SESSION_PATH)) }

    /** Cookies that make the browser forget both tokens. */
    fun cleared(): Response.() -> Response = {
        cookie(cookie(ACCESS, "", Duration.ZERO, "/")).cookie(cookie(REFRESH, "", Duration.ZERO, SESSION_PATH))
    }

    fun accessToken(request: Request): String? = request.cookies().firstOrNull { it.name == ACCESS }?.value?.takeIf { it.isNotEmpty() }

    fun refreshToken(request: Request): String? = request.cookies().firstOrNull { it.name == REFRESH }?.value?.takeIf { it.isNotEmpty() }

    /** True for a read, or a write made from the website's own pages. */
    fun originAllowed(request: Request): Boolean =
        request.method in SAFE_METHODS || request.header("Origin") == websiteOrigin

    private fun cookie(name: String, value: String, lifetime: Duration, path: String) = Cookie(
        name = name,
        value = value,
        maxAge = lifetime.seconds,
        path = path,
        secure = true,
        httpOnly = true,
        sameSite = SameSite.Strict,
    )

    companion object {
        const val ACCESS = "__Host-cp_at"
        const val REFRESH = "__Secure-cp_rt"

        /** The routes that sign in, refresh and sign out: where the refresh cookie goes. */
        const val SESSION_PATH = "/api/v1/web/session"
        private val SAFE_METHODS = setOf(Method.GET, Method.HEAD, Method.OPTIONS)
    }
}
