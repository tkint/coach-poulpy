package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutFormat
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.remote.AccountClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class AccountModelTest {
    private val store = LocalStore(DriverFactory())
    private val memory = MemoryStore()
    private val secrets = MemorySecrets()
    private val legs = Workout(WorkoutId("w1"), 1, "Legs", WorkoutFormat.SETS, emptyList())

    /** What the server answers, by method and path; anything else is a 404. */
    private val answers = mutableMapOf<String, MockRequestHandleScope.() -> HttpResponseData>()
    private val calls = mutableListOf<String>()
    private var offline = false

    private val engine = MockEngine { request ->
        val call = "${request.method.value} ${request.url.encodedPath}"
        calls += call
        if (offline) throw IOException("no connection")
        answers[call]?.invoke(this) ?: respond("", HttpStatusCode.NotFound)
    }

    private fun model() = AccountModel(
        store, memory, secrets, AccountClient("https://api.example.com", SecretTokenStore(secrets), engine),
        "Ana's iPhone", { "en" }, CoroutineScope(SupervisorJob() + Dispatchers.Default),
        reconnectAfter = 20.milliseconds,
    )

    /** Whether the server says the link was opened, as confirmation-status answers. */
    private fun linkOpened(opened: Boolean) {
        answers["POST /api/v1/accounts/confirmation-status"] = { json("""{"confirmed":$opened}""") }
    }

    private fun MockRequestHandleScope.json(text: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(text, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun serverAccepts() {
        answers["POST /oauth/token"] = {
            json("""{"access_token":"cp_at_1","token_type":"Bearer","expires_in":900,"refresh_token":"cp_rt_1","scope":"sync"}""")
        }
        answers["GET /api/v1/sync/pull"] = { json("""{"changes":[],"cursor":"c1","more":false}""") }
        answers["POST /api/v1/sync/push"] = { json("{}") }
        answers["POST /oauth/revoke"] = { respond("", HttpStatusCode.OK) }
    }

    private suspend fun AccountModel.becomes(check: (AccountState) -> Boolean): AccountState =
        withContext(Dispatchers.Default) { withTimeout(5.seconds) { state.first(check) } }

    @Test
    fun aGuestSigningInSendsWhatThePhoneHolds() = runTest {
        serverAccepts()
        store.saveWorkout(legs)
        val account = model()

        assertEquals(SignInResult.Joining, account.signIn(" ana@example.com ", "secret-pass"))

        val synced = account.becomes { it is AccountState.SignedIn && it.upToDate && it.lastSyncedAt != null }
        assertEquals("ana@example.com", (synced as AccountState.SignedIn).email)
        assertTrue(assertNotNull(account.upload.value).done)
        assertEquals(listOf("Legs"), store.savedWorkouts().map { it.workout.name })
        assertEquals("ana@example.com", account.knownEmail)
    }

    @Test
    fun aPhoneWithNothingToSendIsSimplySignedIn() = runTest {
        serverAccepts()

        assertEquals(SignInResult.SignedIn, model().signIn("ana@example.com", "secret-pass"))
    }

    @Test
    fun aSignedInPhoneStaysSignedInAfterARestart() = runTest {
        serverAccepts()
        model().signIn("ana@example.com", "secret-pass")

        assertIs<AccountState.SignedIn>(model().state.value)
    }

    @Test
    fun aRefusedSignInSaysOnlyThatTheEmailOrPasswordIsWrong() = runTest {
        answers["POST /oauth/token"] = { json("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }
        val account = model()

        assertEquals(SignInResult.WrongEmailOrPassword, account.signIn("ana@example.com", "wrong-pass"))
        assertEquals(AccountState.Guest, account.state.value)

        answers["POST /oauth/token"] = {
            respond("""{"error":"too_many_requests"}""", HttpStatusCode.TooManyRequests, headersOf(HttpHeaders.RetryAfter, "30"))
        }
        assertEquals(SignInResult.TooManyTries(30), account.signIn("ana@example.com", "wrong-pass"))
    }

    @Test
    fun noConnectionReadsAsOffline() = runTest {
        offline = true
        val account = model()

        assertEquals(SignInResult.Offline, account.signIn("ana@example.com", "secret-pass"))
        assertEquals(SignUpResult.Offline, account.signUp("ana@example.com", "secret-pass", healthDataConsent = true))
        assertEquals(AccountState.Guest, account.state.value)
    }

    @Test
    fun theSignUpFormIsCheckedBeforeAnythingIsSent() = runTest {
        val account = model()

        val result = account.signUp("ana.example.com", "short", healthDataConsent = false)

        assertEquals(SignUpResult.Invalid(SignUpProblems(email = true, password = true, consent = true)), result)
        assertEquals(emptyList(), calls)
    }

    @Test
    fun signingUpWaitsForTheEmailEvenAfterARestart() = runTest {
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        val account = model()

        assertEquals(SignUpResult.Sent, account.signUp("ana@example.com", "secret-pass", healthDataConsent = true))

        assertEquals(AccountState.Pending("ana@example.com"), account.state.value)
        assertEquals(AccountState.Pending("ana@example.com"), model().state.value)
        assertEquals(SignUpResult.Sent, account.sendAgain())
        assertEquals(2, calls.count { it == "POST /api/v1/accounts" })

        account.changeAddress()
        assertEquals(AccountState.Guest, account.state.value)
        assertNull(secrets.getSecret(AccountModel.PENDING))
    }

    @Test
    fun theConfirmationLinkSignsInThePhoneThatSignedUp() = runTest {
        serverAccepts()
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        answers["POST /api/v1/accounts/confirm"] = { respond("", HttpStatusCode.NoContent) }
        store.saveWorkout(legs)
        val account = model()
        account.signUp("ana@example.com", "secret-pass", healthDataConsent = true)
        linkOpened(true)

        assertEquals(ConfirmResult.CONFIRMED, account.confirmEmail("token"))
        // Opened on this phone: its own screen moves on, nothing else needs showing.
        assertEquals(false, account.confirmedElsewhere.value)

        account.becomes { it is AccountState.SignedIn && it.upToDate }
        assertNull(secrets.getSecret(AccountModel.PENDING))
        assertTrue(calls.contains("POST /api/v1/sync/push"))
    }

    @Test
    fun aPhoneWaitingForTheEmailSignsInOnceItIsConfirmedElsewhere() = runTest {
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        linkOpened(false)
        store.saveWorkout(legs)
        val account = model()
        account.signUp("ana@example.com", "secret-pass", healthDataConsent = true)

        assertEquals(false, account.checkConfirmed())
        assertEquals(AccountState.Pending("ana@example.com"), account.state.value)
        // Asking costs no sign-in try, so the address is never throttled.
        assertEquals(0, calls.count { it == "POST /oauth/token" })

        // The link was opened on the website; the app opens again.
        serverAccepts()
        linkOpened(true)
        val reopened = model()

        reopened.becomes { it is AccountState.SignedIn && it.upToDate }
        assertNull(secrets.getSecret(AccountModel.PENDING))
        assertTrue(calls.contains("POST /api/v1/sync/push"))
    }

    @Test
    fun checkYourEmailMovesOnTheMomentTheServerSaysTheLinkWasOpened() = runTest {
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        linkOpened(false)
        // The stream drops twice with nothing, then says confirmed.
        var streams = 0
        answers["GET /api/v1/accounts/confirmation-events"] = {
            streams++
            if (streams < 3) {
                respond(":\n\n", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/event-stream"))
            } else {
                serverAccepts()
                linkOpened(true)
                respond("event:confirmed\ndata:{}\n\n", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/event-stream"))
            }
        }
        val account = model()
        account.signUp("ana@example.com", "secret-pass", healthDataConsent = true)

        val watching = account.watchConfirmation()
        // The app shows Email confirmed over whatever is on screen, once, after signing in.
        withContext(Dispatchers.Default) { withTimeout(5.seconds) { account.confirmedElsewhere.first { it } } }
        watching.cancel()
        assertIs<AccountState.SignedIn>(account.state.value)
        account.confirmedShown()
        assertEquals(false, account.confirmedElsewhere.value)
        assertEquals(3, streams)
        assertEquals(1, calls.count { it == "POST /oauth/token" })
    }

    @Test
    fun sendingAgainKeepsTheKeySoTheFirstLinkStillMovesThePhoneOn() = runTest {
        val keys = mutableListOf<String>()
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        val account = model()
        account.signUp("ana@example.com", "secret-pass", healthDataConsent = true)
        keys += secrets.getSecret(AccountModel.PENDING)!!
        account.sendAgain()
        keys += secrets.getSecret(AccountModel.PENDING)!!

        assertEquals(keys[0], keys[1])
        assertTrue(Regex("\"key\":\"[0-9a-f]{64}\"").containsMatchIn(keys[0]), keys[0])
    }

    @Test
    fun aConfirmedEmailWhosePasswordChangedAsksToSignIn() = runTest {
        answers["POST /api/v1/accounts"] = { respond("", HttpStatusCode.Accepted) }
        answers["POST /oauth/token"] = { json("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }
        linkOpened(true)
        val account = model()
        account.signUp("ana@example.com", "secret-pass", healthDataConsent = true)

        val watching = account.watchConfirmation()
        account.becomes { it == AccountState.Guest }
        watching.cancel()
        assertEquals(1, calls.count { it == "POST /oauth/token" })
    }

    @Test
    fun theConfirmationLinkOnAnotherPhoneAsksToSignIn() = runTest {
        answers["POST /api/v1/accounts/confirm"] = { respond("", HttpStatusCode.NoContent) }

        assertEquals(ConfirmResult.CONFIRMED_SIGN_IN, model().confirmEmail("token"))
    }

    @Test
    fun aUsedConfirmationLinkHasExpired() = runTest {
        answers["POST /api/v1/accounts/confirm"] = { json("""{"error":"invalid-token"}""", HttpStatusCode.BadRequest) }

        assertEquals(ConfirmResult.LINK_EXPIRED, model().confirmEmail("token"))
    }

    @Test
    fun aNewPasswordSignsThisPhoneOutButKeepsItsData() = runTest {
        serverAccepts()
        answers["POST /api/v1/password-resets/confirm"] = { respond("", HttpStatusCode.NoContent) }
        store.saveWorkout(legs)
        val account = model()
        account.signIn("ana@example.com", "secret-pass")

        assertEquals(NewPasswordResult.TOO_SHORT, account.setNewPassword("token", "short"))
        assertEquals(NewPasswordResult.SAVED, account.setNewPassword("token", "new-secret-pass"))

        assertEquals(AccountState.Guest, account.state.value)
        assertEquals(listOf("Legs"), store.savedWorkouts().map { it.workout.name })
        assertNull(secrets.getSecret(AccountModel.TOKENS))
    }

    @Test
    fun signingOutWithChangesWaitingAsksFirst() = runTest {
        serverAccepts()
        answers["POST /api/v1/sync/push"] = { respond("", HttpStatusCode.ServiceUnavailable) }
        store.saveWorkout(legs)
        val account = model()
        account.signIn("ana@example.com", "secret-pass")

        assertEquals(SignOutResult.CHANGES_WAITING, account.signOut())
        assertIs<AccountState.SignedIn>(account.state.value)

        assertEquals(SignOutResult.SIGNED_OUT, account.signOut(evenWithChanges = true))
        assertEquals(AccountState.Guest, account.state.value)
        assertEquals(emptyList(), store.savedWorkouts())
    }

    @Test
    fun deletingTheAccountLeavesAGuestWithItsData() = runTest {
        serverAccepts()
        answers["DELETE /api/v1/me"] = { json("""{"error":"wrong-password"}""", HttpStatusCode.Forbidden) }
        store.saveWorkout(legs)
        val account = model()
        account.signIn("ana@example.com", "secret-pass")

        assertEquals(DeleteResult.WRONG_PASSWORD, account.deleteAccount("wrong-pass"))
        answers["DELETE /api/v1/me"] = { respond("", HttpStatusCode.NoContent) }
        assertEquals(DeleteResult.DELETED, account.deleteAccount("secret-pass"))

        assertEquals(AccountState.Guest, account.state.value)
        assertEquals(listOf("Legs"), store.savedWorkouts().map { it.workout.name })
        assertEquals(0, store.outbox(10).size)
    }
}

internal class MemorySecrets : SecretStore {
    private val values = mutableMapOf<String, String>()
    override fun getSecret(key: String) = values[key]
    override fun putSecret(key: String, value: String?) {
        if (value == null) values.remove(key) else values[key] = value
    }
}
