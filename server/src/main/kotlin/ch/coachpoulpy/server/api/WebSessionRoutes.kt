package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.auth.WebCookies
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class WebSignInRequest(val email: String, val password: String)

private val webSignInBody = Body.auto<WebSignInRequest>().toLens()
private val webTag = Tag("website")

/** What a browser calls a sign-in, in the person's list of devices. */
private const val BROWSER = "Web browser"

/**
 * The website's sign-in, refresh and sign-out (ADR 0020): the same sign-ins as the app's, but the
 * tokens live in HttpOnly cookies ([WebCookies]) and never reach the page's script. The answers
 * carry no body; `GET /api/v1/me` says who is signed in.
 */
fun webSessionRoutes(signIns: SignIns, cookies: WebCookies): List<ContractRoute> =
    listOf(signInRoute(signIns, cookies), refreshRoute(signIns, cookies), signOutRoute(signIns, cookies))

/** A sign-in is a write made without a cookie yet, so it checks the origin itself: nobody may sign a browser in from another site. */
private fun fromWebsite(cookies: WebCookies, request: Request) = cookies.originAllowed(request)

private fun signInRoute(signIns: SignIns, cookies: WebCookies): ContractRoute = "$API_PREFIX/web/session" meta {
    summary = "Sign in on the website"
    description = "Email and password. The answer sets the access and refresh cookies. After ${SignIns.MAX_FAILURES} failures for " +
        "one email within ${SignIns.FAILURE_WINDOW.toMinutes()} minutes it answers 429 with Retry-After. Only the website may call it."
    operationId = "webSignIn"
    tags += webTag
    security = Public
    receiving(webSignInBody to WebSignInRequest("ana@example.com", "correct horse"))
    returning(NO_CONTENT to "Signed in; the cookies are set")
    returning(BAD_REQUEST to "Wrong email or password, or the email isn't confirmed")
    returning(TOO_MANY_REQUESTS to "Too many failures for this email")
    returning(FORBIDDEN to "Not called from the website")
} bindContract POST to { request ->
    if (!fromWebsite(cookies, request)) {
        Response(FORBIDDEN)
    } else {
        val body = webSignInBody(request)
        when (val result = signIns.signIn(body.email, body.password, BROWSER, Client.Website)) {
            is SignInResult.Signed -> sessionCookies(Response(NO_CONTENT), cookies, result.tokens)
            SignInResult.Refused -> Response(BAD_REQUEST)
            is SignInResult.Throttled -> Response(TOO_MANY_REQUESTS).header("Retry-After", result.retryAfter.seconds.toString())
        }
    }
}

private fun refreshRoute(signIns: SignIns, cookies: WebCookies): ContractRoute = "$API_PREFIX/web/session/refresh" meta {
    summary = "Refresh the website's sign-in"
    description = "Trades the refresh cookie for new cookies. Each refresh token works once; using one a second time signs the browser out."
    operationId = "webRefresh"
    tags += webTag
    security = Public
    returning(NO_CONTENT to "New cookies are set")
    returning(UNAUTHORIZED to "No refresh cookie, or it is unknown, used, expired or signed out; the cookies are cleared")
    returning(FORBIDDEN to "Not called from the website")
} bindContract POST to { request ->
    if (!fromWebsite(cookies, request)) {
        Response(FORBIDDEN)
    } else {
        val tokens = cookies.refreshToken(request)?.let(signIns::refresh)
        if (tokens == null) Response(UNAUTHORIZED).let(cookies.cleared()) else sessionCookies(Response(NO_CONTENT), cookies, tokens)
    }
}

private fun signOutRoute(signIns: SignIns, cookies: WebCookies): ContractRoute = "$API_PREFIX/web/session" meta {
    summary = "Sign out on the website"
    description = "Revokes the browser's tokens and clears the cookies. Always 204, signed in or not."
    operationId = "webSignOut"
    tags += webTag
    security = Public
    returning(NO_CONTENT to "Signed out")
    returning(FORBIDDEN to "Not called from the website")
} bindContract DELETE to { request ->
    if (!fromWebsite(cookies, request)) {
        Response(FORBIDDEN)
    } else {
        listOfNotNull(cookies.accessToken(request), cookies.refreshToken(request)).forEach(signIns::revoke)
        Response(NO_CONTENT).let(cookies.cleared())
    }
}

private fun sessionCookies(response: Response, cookies: WebCookies, tokens: ch.coachpoulpy.server.auth.Tokens): Response =
    response.let(cookies.access(tokens.accessToken, tokens.expiresIn))
        .let(cookies.refresh(tokens.refreshToken, SignIns.REFRESH_LIFETIME))
        .header("Cache-Control", "no-store")
