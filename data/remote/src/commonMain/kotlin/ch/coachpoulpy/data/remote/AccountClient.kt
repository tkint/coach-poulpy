package ch.coachpoulpy.data.remote

import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.WorkoutId
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A device's sign-in: the access token until [accessExpiresAt], and the refresh token for the next one. */
@Serializable
data class Tokens(val accessToken: String, val refreshToken: String, val accessExpiresAt: Instant)

/** Where the apps keep the [Tokens]: the Keychain on iOS, the Keystore on Android. Null when signed out. */
interface TokenStore {
    fun read(): Tokens?
    fun write(tokens: Tokens?)
}

/** The server refused the refresh token: the tokens are gone, and the phone is signed out. */
class SignInRefusedByServer : Exception("The server refused the refresh token")

/** The server answered something this client doesn't expect, such as a 500. */
class UnexpectedAnswer(val status: Int) : Exception("The server answered $status")

enum class SignUpAnswer { ACCEPTED, INVALID_EMAIL, INVALID_PASSWORD, CONSENT_REQUIRED }

sealed interface SignInAnswer {
    data object SignedIn : SignInAnswer
    /** A wrong email or password, or an email not confirmed yet: the server doesn't say which. */
    data object Refused : SignInAnswer
    /** Too many failures for this email: try again after [retryAfterSeconds]. */
    data class TooManyTries(val retryAfterSeconds: Long) : SignInAnswer
}

enum class DeleteAnswer { DELETED, WRONG_PASSWORD }

sealed interface PushResult {
    data object Accepted : PushResult
    data class StaleBase(val workouts: List<WorkoutId>) : PushResult
    data class InvalidChange(val index: Int) : PushResult
    data object TooLarge : PushResult
}

/** Who is signed in, from `GET /api/v1/me`. */
@Serializable
data class Me(val accountId: String, val email: String, val role: String, val client: String, val scopes: List<String>)

/**
 * The phone's calls to the account API (docs/the-api.md). [baseUrl] is the API's origin, such as
 * `https://api.coach-poulpy.com`. Signed-in calls refresh the access token before it expires or on a
 * 401, one refresh at a time: a refresh token used twice signs the device out.
 */
class AccountClient(
    baseUrl: String,
    private val tokens: TokenStore,
    engine: HttpClientEngine? = null,
    private val clock: Clock = Clock.System,
) {
    private val base = baseUrl.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true }
    private val http = (if (engine != null) HttpClient(engine) { setUp() } else HttpClient { setUp() })
    private val refreshing = Mutex()

    private fun io.ktor.client.HttpClientConfig<*>.setUp() {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
    }

    /** Whether this phone holds a sign-in. */
    val signedIn: Boolean get() = tokens.read() != null

    suspend fun signUp(email: String, password: String, language: String, healthDataConsent: Boolean): SignUpAnswer {
        val response = http.post("$base/api/v1/accounts") {
            contentType(ContentType.Application.Json)
            setBody(SignUpRequest(email, password, language, healthDataConsent))
        }
        return when (response.status) {
            HttpStatusCode.Accepted -> SignUpAnswer.ACCEPTED
            HttpStatusCode.BadRequest -> when (response.body<Problem>().error) {
                "invalid-email" -> SignUpAnswer.INVALID_EMAIL
                "invalid-password" -> SignUpAnswer.INVALID_PASSWORD
                "consent-required" -> SignUpAnswer.CONSENT_REQUIRED
                else -> throw UnexpectedAnswer(400)
            }
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }

    /** Signs this device in as [deviceName], such as "Ana's iPhone", and keeps its tokens. */
    suspend fun signIn(email: String, password: String, deviceName: String): SignInAnswer {
        val response = http.submitForm(
            "$base/oauth/token",
            parameters {
                append("grant_type", "password")
                append("username", email)
                append("password", password)
                append("device_name", deviceName)
                append("client_id", CLIENT_ID)
            },
        )
        return when (response.status) {
            HttpStatusCode.OK -> {
                keep(response.body())
                SignInAnswer.SignedIn
            }
            HttpStatusCode.TooManyRequests ->
                SignInAnswer.TooManyTries(response.headers["Retry-After"]?.toLongOrNull() ?: 60)
            HttpStatusCode.BadRequest -> SignInAnswer.Refused
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }

    /** Mails a reset link when the address has a confirmed account; the server never says whether it has. */
    suspend fun requestPasswordReset(email: String) {
        val response = http.post("$base/api/v1/password-resets") {
            contentType(ContentType.Application.Json)
            setBody(EmailRequest(email))
        }
        if (response.status != HttpStatusCode.Accepted) throw UnexpectedAnswer(response.status.value)
    }

    suspend fun me(): Me = signedInCall { get("$base/api/v1/me", it) }.let { response ->
        if (response.status != HttpStatusCode.OK) throw UnexpectedAnswer(response.status.value)
        response.body()
    }

    /** Deletes the account at once; the tokens go with it. */
    suspend fun deleteAccount(password: String): DeleteAnswer {
        val response = signedInCall {
            http.delete("$base/api/v1/me") {
                bearerAuth(it)
                contentType(ContentType.Application.Json)
                setBody(PasswordRequest(password))
            }
        }
        return when (response.status) {
            HttpStatusCode.NoContent -> DeleteAnswer.DELETED.also { tokens.write(null) }
            HttpStatusCode.Forbidden -> DeleteAnswer.WRONG_PASSWORD
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }

    /** Revokes this device's sign-in (RFC 7009) and forgets its tokens, even when the server can't be reached. */
    suspend fun signOut() {
        val held = tokens.read() ?: return
        tokens.write(null)
        try {
            http.submitForm("$base/oauth/revoke", parameters { append("token", held.refreshToken) })
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Forgotten here either way; the refresh token expires unused after 90 days.
        }
    }

    suspend fun pull(cursor: String?, limit: Int): SyncPage {
        val response = signedInCall {
            get("$base/api/v1/sync/pull", it) {
                cursor?.let { value -> parameter("cursor", value) }
                parameter("limit", limit)
            }
        }
        if (response.status != HttpStatusCode.OK) throw UnexpectedAnswer(response.status.value)
        return response.body()
    }

    suspend fun push(push: SyncPush): PushResult {
        val response = signedInCall {
            http.post("$base/api/v1/sync/push") {
                bearerAuth(it)
                contentType(ContentType.Application.Json)
                setBody(push)
            }
        }
        return when (response.status) {
            HttpStatusCode.OK -> PushResult.Accepted
            HttpStatusCode.Conflict -> PushResult.StaleBase(response.body<ChangeProblem>().workouts.orEmpty().map(::WorkoutId))
            HttpStatusCode.BadRequest -> PushResult.InvalidChange(response.body<ChangeProblem>().index ?: 0)
            HttpStatusCode.PayloadTooLarge -> PushResult.TooLarge
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }

    private suspend fun get(url: String, token: String, block: HttpRequestBuilder.() -> Unit = {}) =
        http.get(url) { bearerAuth(token); block() }

    /** Sends [call] with a fresh access token, refreshing once more on a 401. */
    private suspend fun signedInCall(call: suspend (String) -> HttpResponse): HttpResponse {
        val held = tokens.read() ?: throw SignInRefusedByServer()
        val fresh = if (held.accessExpiresAt - EXPIRY_MARGIN <= clock.now()) refresh(held) else held
        val response = call(fresh.accessToken)
        if (response.status != HttpStatusCode.Unauthorized) return response
        return call(refresh(fresh).accessToken)
    }

    /**
     * New tokens from [stale]'s refresh token. Calls waiting meanwhile take the tokens the first one
     * got rather than spend the refresh token again.
     */
    private suspend fun refresh(stale: Tokens): Tokens = refreshing.withLock {
        val held = tokens.read() ?: throw SignInRefusedByServer()
        if (held.refreshToken != stale.refreshToken) return@withLock held
        val response = http.submitForm(
            "$base/oauth/token",
            parameters {
                append("grant_type", "refresh_token")
                append("refresh_token", held.refreshToken)
                append("client_id", CLIENT_ID)
            },
        )
        when (response.status) {
            HttpStatusCode.OK -> keep(response.body())
            HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                tokens.write(null)
                throw SignInRefusedByServer()
            }
            else -> throw UnexpectedAnswer(response.status.value)
        }
    }

    private fun keep(answer: TokenAnswer): Tokens =
        Tokens(answer.accessToken, answer.refreshToken, clock.now() + answer.expiresIn.seconds).also(tokens::write)

    private companion object {
        const val CLIENT_ID = "app"
        /** Refreshed this long before it expires, so a call doesn't race the clock. */
        val EXPIRY_MARGIN = 30.seconds
    }
}

@Serializable
private data class SignUpRequest(val email: String, val password: String, val language: String, val healthDataConsent: Boolean)

@Serializable
private data class EmailRequest(val email: String)

@Serializable
private data class PasswordRequest(val password: String)

@Serializable
private data class Problem(val error: String)

@Serializable
private data class ChangeProblem(val error: String, val index: Int? = null, val workouts: List<String>? = null)

@Serializable
private data class TokenAnswer(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
)
