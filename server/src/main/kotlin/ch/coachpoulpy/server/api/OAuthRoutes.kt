package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.auth.Client
import ch.coachpoulpy.server.auth.SignInResult
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.auth.Tokens
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.TOO_MANY_REQUESTS
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.webForm

/** The token response of RFC 6749, section 5.1. */
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
    val scope: String,
)

/** The error response of RFC 6749, section 5.2. */
@Serializable
data class OAuthError(
    val error: String,
    @SerialName("error_description") val description: String? = null,
)

private val tokenBody = Body.auto<TokenResponse>().toLens()
private val errorBody = Body.auto<OAuthError>().toLens()

// Declared for the OpenAPI description only; the handlers read the fields themselves, so a missing
// one gets OAuth's invalid_request rather than a generic 400.
private val tokenForm = Body.webForm(
    Validator.Ignore,
    FormField.required("grant_type", "password or refresh_token"),
    FormField.optional("username", "password grant: the account's email"),
    FormField.optional("password", "password grant"),
    FormField.optional("device_name", "password grant: what the person sees in their list of devices, such as Ana's iPhone"),
    FormField.optional("refresh_token", "refresh_token grant"),
    FormField.optional("client_id", "app, the only client so far"),
).toLens()

private val revokeForm = Body.webForm(
    Validator.Ignore,
    FormField.required("token", "An access or a refresh token"),
    FormField.optional("token_type_hint", "access_token or refresh_token; either way the whole device is signed out"),
).toLens()

private val oauthTag = Tag("oauth")

/** The clients that may use the password grant: Coach Poulpy's own apps (ADR 0020). */
private val firstPartyClients = setOf(Client.App)

private const val DEVICE_NAME_MAX = 100

private fun Tokens.toResponse() = TokenResponse(
    accessToken = accessToken,
    tokenType = "Bearer",
    expiresIn = expiresIn.seconds,
    refreshToken = refreshToken,
    scope = SignIns.FIRST_PARTY_SCOPES.map { it.value }.sorted().joinToString(" "),
)

// RFC 6749, section 5.1: token responses are never cached.
private fun Response.noStore() = header("Cache-Control", "no-store").header("Pragma", "no-cache")

private fun oauthError(status: Status, error: String, description: String) =
    Response(status).noStore().with(errorBody of OAuthError(error, description))

private fun Request.field(name: String): String? = form(name)?.takeIf { it.isNotEmpty() }

/**
 * The OAuth 2.0 token endpoint (RFC 6749): the password grant signs a phone in, the
 * refresh_token grant trades a refresh token for new tokens. Outside `/api/v1`, like OAuth
 * routes on most APIs (ADR 0021). The authorization_code grant joins it with the website and
 * third-party apps (#228).
 */
fun tokenRoute(signIns: SignIns): ContractRoute = "/oauth/token" meta {
    summary = "Get tokens (OAuth 2.0 token endpoint)"
    description = "grant_type=password with username, password and device_name signs a device in; grant_type=refresh_token " +
        "with refresh_token gives new tokens. The access token lasts ${SignIns.ACCESS_LIFETIME.toMinutes()} minutes. " +
        "Each refresh token works once and the answer carries the next one; using one a second time signs that device out. " +
        "A wrong email or password and an email not confirmed yet all get invalid_grant. After ${SignIns.MAX_FAILURES} failures " +
        "for one email within ${SignIns.FAILURE_WINDOW.toMinutes()} minutes, the password grant answers 429 with Retry-After."
    operationId = "token"
    tags += oauthTag
    security = Public
    receiving(tokenForm)
    returning(
        OK,
        tokenBody to TokenResponse("cp_at_…", "Bearer", SignIns.ACCESS_LIFETIME.seconds, "cp_rt_…", "sync workouts:read"),
        "The tokens",
        definitionId = "TokenResponse",
    )
    returning(
        BAD_REQUEST,
        errorBody to OAuthError("invalid_grant", "Wrong email or password"),
        "invalid_request, invalid_grant or unsupported_grant_type",
        definitionId = "OAuthError",
    )
    returning(UNAUTHORIZED, errorBody to OAuthError("invalid_client"), "invalid_client: an unknown client_id", definitionId = "OAuthError")
    returning(TOO_MANY_REQUESTS, errorBody to OAuthError("invalid_grant"), "Too many failures for this email", definitionId = "OAuthError")
} bindContract POST to { request ->
    val client = Client(request.field("client_id") ?: Client.App.value)
    if (client !in firstPartyClients) {
        oauthError(UNAUTHORIZED, "invalid_client", "Unknown client_id")
    } else {
        when (request.field("grant_type")) {
            "password" -> passwordGrant(signIns, request, client)
            "refresh_token" -> refreshGrant(signIns, request)
            null -> oauthError(BAD_REQUEST, "invalid_request", "grant_type is missing")
            else -> oauthError(BAD_REQUEST, "unsupported_grant_type", "Use password or refresh_token")
        }
    }
}

private fun passwordGrant(signIns: SignIns, request: Request, client: Client): Response {
    val email = request.field("username")
    val password = request.field("password")
    val deviceName = request.field("device_name")?.trim() ?: "Unnamed device"
    return when {
        email == null || password == null -> oauthError(BAD_REQUEST, "invalid_request", "username and password are needed")
        deviceName.isEmpty() || deviceName.length > DEVICE_NAME_MAX ->
            oauthError(BAD_REQUEST, "invalid_request", "device_name has 1 to $DEVICE_NAME_MAX characters")
        else -> when (val result = signIns.signIn(email, password, deviceName, client)) {
            is SignInResult.Signed -> Response(OK).noStore().with(tokenBody of result.tokens.toResponse())
            SignInResult.Refused -> oauthError(BAD_REQUEST, "invalid_grant", "Wrong email or password, or the email isn't confirmed")
            is SignInResult.Throttled -> oauthError(TOO_MANY_REQUESTS, "invalid_grant", "Too many failed sign-ins for this email")
                .header("Retry-After", result.retryAfter.seconds.toString())
        }
    }
}

private fun refreshGrant(signIns: SignIns, request: Request): Response {
    val refreshToken = request.field("refresh_token")
        ?: return oauthError(BAD_REQUEST, "invalid_request", "refresh_token is missing")
    return signIns.refresh(refreshToken)
        ?.let { Response(OK).noStore().with(tokenBody of it.toResponse()) }
        ?: oauthError(BAD_REQUEST, "invalid_grant", "The refresh token is unknown, used, expired or signed out")
}

/**
 * Token revocation (RFC 7009): signs out the device an access or refresh token belongs to. It
 * answers 200 for an unknown token too, as the RFC asks, so it tells nothing about tokens.
 */
fun revokeRoute(signIns: SignIns): ContractRoute = "/oauth/revoke" meta {
    summary = "Sign a device out (OAuth 2.0 token revocation)"
    description = "Revokes the device's access and refresh tokens, whichever of the two is sent. Always 200."
    operationId = "revoke"
    tags += oauthTag
    security = Public
    receiving(revokeForm)
    returning(OK to "Revoked, or the token was not live")
    returning(BAD_REQUEST, errorBody to OAuthError("invalid_request"), "invalid_request: token is missing", definitionId = "OAuthError")
} bindContract POST to { request ->
    when (val token = request.field("token")) {
        null -> oauthError(BAD_REQUEST, "invalid_request", "token is missing")
        else -> {
            signIns.revoke(token)
            Response(OK)
        }
    }
}
