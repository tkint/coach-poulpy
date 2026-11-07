package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.SignInResult
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.auth.Tokens
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import ch.coachpoulpy.server.db.Database
import java.util.UUID
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    /** What the person sees in their list of signed-in devices, such as "Ana's iPhone". */
    val deviceName: String,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class TokensResponse(val accessToken: String, val refreshToken: String, val tokenType: String, val expiresIn: Long)

@Serializable
data class MeResponse(val accountId: String, val email: String, val role: String, val client: String, val scopes: List<String>)

private val signInBody = Body.auto<SignInRequest>().toLens()
private val refreshBody = Body.auto<RefreshRequest>().toLens()
private val tokensBody = Body.auto<TokensResponse>().toLens()
private val meBody = Body.auto<MeResponse>().toLens()
private val problemBody = Body.auto<Problem>().toLens()

private val signInsTag = Tag("sign-ins")

private const val DEVICE_NAME_MAX = 100

private val exampleTokens = TokensResponse("cp_at_…", "cp_rt_…", "Bearer", SignIns.ACCESS_LIFETIME.seconds)

private fun Tokens.toResponse() = TokensResponse(accessToken, refreshToken, "Bearer", expiresIn.seconds)

private fun refused(error: String) = Response(UNAUTHORIZED).with(problemBody of Problem(error))

/** Signs a phone in with an email and a password. */
fun signInRoute(signIns: SignIns): ContractRoute = "/sign-ins" meta {
    summary = "Sign in with an email and a password"
    description = "Returns a ${SignIns.ACCESS_LIFETIME.toMinutes()}-minute access token for the Authorization header and a refresh token " +
        "for POST /api/v1/sign-ins/refresh. A wrong address or password and an email not confirmed yet all get the same 401. " +
        "After ${SignIns.MAX_FAILURES} failures for one address within ${SignIns.FAILURE_WINDOW.toMinutes()} minutes, " +
        "sign-ins for it get 429 until the window has passed."
    operationId = "signIn"
    tags += signInsTag
    security = Public
    receiving(signInBody to SignInRequest("ana@example.com", "correct horse", "Ana's iPhone"), definitionId = "SignIn")
    returning(OK, tokensBody to exampleTokens, "Signed in", definitionId = "Tokens")
    returning(UNAUTHORIZED, problemBody to Problem("invalid-credentials"), "invalid-credentials", definitionId = "Problem")
    returning(TOO_MANY_REQUESTS, problemBody to Problem("too-many-attempts"), "too-many-attempts, with Retry-After", definitionId = "Problem")
} bindContract POST to { request ->
    val body = signInBody(request)
    if (body.deviceName.isBlank() || body.deviceName.length > DEVICE_NAME_MAX) {
        Response(BAD_REQUEST).with(problemBody of Problem("invalid-device-name"))
    } else {
        when (val result = signIns.signIn(body.email, body.password, body.deviceName)) {
            is SignInResult.Signed -> Response(OK).with(tokensBody of result.tokens.toResponse())
            SignInResult.Refused -> refused("invalid-credentials")
            is SignInResult.Throttled -> Response(TOO_MANY_REQUESTS)
                .header("Retry-After", result.retryAfter.seconds.toString())
                .with(problemBody of Problem("too-many-attempts"))
        }
    }
}

/** Trades a refresh token for new tokens; the old refresh token stops working. */
fun refreshRoute(signIns: SignIns): ContractRoute = "/sign-ins/refresh" meta {
    summary = "Get new tokens with a refresh token"
    description = "Each refresh token works once and the answer carries the next one. Using a refresh token a second time " +
        "signs that device out, since someone else may hold a copy."
    operationId = "refreshTokens"
    tags += signInsTag
    security = Public
    receiving(refreshBody to RefreshRequest("cp_rt_…"), definitionId = "Refresh")
    returning(OK, tokensBody to exampleTokens, "New tokens", definitionId = "Tokens")
    returning(UNAUTHORIZED, problemBody to Problem("invalid-token"), "invalid-token: unknown, used, expired or signed out", definitionId = "Problem")
} bindContract POST to { request ->
    signIns.refresh(refreshBody(request).refreshToken)
        ?.let { Response(OK).with(tokensBody of it.toResponse()) }
        ?: refused("invalid-token")
}

/** Signs out the device the access token belongs to. */
fun signOutRoute(signIns: SignIns): ContractRoute = "/sign-ins/current" meta {
    summary = "Sign this device out"
    description = "Revokes the access and refresh tokens of the device making the request."
    operationId = "signOut"
    tags += signInsTag
    security = requires()
    returning(NO_CONTENT to "Signed out")
} bindContract DELETE to { request ->
    signIns.signOut(request.header("Authorization").orEmpty().removePrefix("Bearer ").trim())
    Response(NO_CONTENT)
}

/** Who the caller is: the account and the client and scopes of the token used (ADR 0020). */
fun meRoute(database: Database): ContractRoute = "/me" meta {
    summary = "Who you are"
    description = "The account, and the client and scopes of the token sent."
    operationId = "getMe"
    tags += signInsTag
    security = requires()
    returning(
        OK,
        meBody to MeResponse("0190d4d2-0000-7000-8000-000000000001", "ana@example.com", "member", "app", listOf("sync")),
        "The caller",
        definitionId = "Me",
    )
} bindContract GET to { request ->
    val caller = request.caller as Caller.Signed
    val account = database.queries.accountQueries.byId(UUID.fromString(caller.accountId.value)).executeAsOne()
    Response(OK).with(
        meBody of MeResponse(
            accountId = caller.accountId.value,
            email = account.email,
            role = account.role,
            client = caller.client.value,
            scopes = caller.scopes.map { it.value }.sorted(),
        ),
    )
}
