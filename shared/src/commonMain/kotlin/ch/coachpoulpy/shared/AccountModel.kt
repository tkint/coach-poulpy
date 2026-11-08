package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.outboxSize
import ch.coachpoulpy.data.remote.AccountClient
import ch.coachpoulpy.data.remote.DeleteAnswer
import ch.coachpoulpy.data.remote.NewPasswordAnswer
import ch.coachpoulpy.data.remote.SignInAnswer
import ch.coachpoulpy.data.remote.SignInRefusedByServer
import ch.coachpoulpy.data.remote.SignUpAnswer
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Where the phone stands with its account (docs/accounts.md, Screens). */
sealed interface AccountState {
    /** All data on the phone, nothing sent. */
    data object Guest : AccountState

    /** Signed up, waiting for the link in the email to [email] to be opened, on this phone or anywhere else. */
    data class Pending(val email: String) : AccountState

    /** Signed in: [waiting] changes not sent yet, and when a sync last finished. */
    data class SignedIn(val email: String, val waiting: Long, val lastSyncedAt: Instant?) : AccountState {
        val upToDate: Boolean get() = waiting == 0L

        /** [lastSyncedAt] for Swift, which has no Kotlin Instant. */
        val lastSyncedEpochMillis: Long? get() = lastSyncedAt?.toEpochMilliseconds()
    }
}

/** The first upload after signing in or confirming the email: [sent] of [total] changes. */
data class UploadProgress(val sent: Long, val total: Long) {
    val done: Boolean get() = sent >= total
}

sealed interface SignInResult {
    /** Signed in on a phone with nothing to send. */
    data object SignedIn : SignInResult
    /** Signed in on a phone with guest data, which now joins the account: Welcome back, with the upload. */
    data object Joining : SignInResult
    /** One message for both: the server never says which was wrong. */
    data object WrongEmailOrPassword : SignInResult
    data class TooManyTries(val retryAfterSeconds: Long) : SignInResult
    data object Offline : SignInResult
}

/** What is wrong in the sign-up form, checked on the phone before anything is sent. */
data class SignUpProblems(val email: Boolean = false, val password: Boolean = false, val consent: Boolean = false) {
    val none: Boolean get() = !email && !password && !consent
}

sealed interface SignUpResult {
    /** Check your email: the answer is the same whether or not the address had an account. */
    data object Sent : SignUpResult
    data class Invalid(val problems: SignUpProblems) : SignUpResult
    data object Offline : SignUpResult
}

enum class ConfirmResult {
    /** Confirmed and signed in: Email confirmed, with the upload. */
    CONFIRMED,
    /** Confirmed, but this phone didn't sign up: Sign in. */
    CONFIRMED_SIGN_IN,
    /** Used or older than 24 hours: This link doesn't work anymore. */
    LINK_EXPIRED,
    OFFLINE,
}

enum class NewPasswordResult { SAVED, LINK_EXPIRED, TOO_SHORT, OFFLINE }

enum class DeleteResult { DELETED, WRONG_PASSWORD, OFFLINE }

/**
 * The account behind Settings' Account section, its screens and the email links: sign in, sign up
 * and confirm, reset a password, sign out and delete. While signed in, [sync] keeps the phone and
 * the account in step. [memory] is the apps' own store; [secrets] keeps the password between
 * signing up and opening the link, then forgets it.
 */
class AccountModel(
    private val store: LocalStore,
    private val memory: KeyValueStore,
    private val secrets: SecretStore,
    private val client: AccountClient,
    private val deviceName: String,
    private val languageCode: () -> String,
    private val scope: CoroutineScope,
    /** How long [watchConfirmation] waits before opening a dropped stream again. */
    private val reconnectAfter: Duration = 3.seconds,
) {
    val sync = SyncClient(
        store, memory, RemoteSyncServer(client), deviceName, languageCode, scope,
        onSignInRefused = { becomeGuest() },
    )

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _upload = MutableStateFlow<UploadProgress?>(null)
    /** The first upload, while Email confirmed or Welcome back shows it; null otherwise. */
    val upload: StateFlow<UploadProgress?> = _upload.asStateFlow()

    private var syncing: Job? = null
    private var watching: Job? = null
    private val checking = Mutex()

    init {
        when (_state.value) {
            is AccountState.SignedIn -> startSyncing()
            // The link may have been opened elsewhere, such as on the website.
            is AccountState.Pending -> scope.launch { checkConfirmed() }
            AccountState.Guest -> Unit
        }
    }

    fun watch(onChange: (AccountState) -> Unit): Cancellable = state.watchIn(scope, onChange)
    fun watchUpload(onChange: (UploadProgress?) -> Unit): Cancellable = upload.watchIn(scope, onChange)

    /** The email to fill in the forms with: the one waiting for confirmation, or the last one used. */
    val knownEmail: String? get() = pending()?.email ?: memory.getString(EMAIL)

    suspend fun signIn(email: String, password: String): SignInResult = offlineAs(SignInResult.Offline) {
        when (val answer = client.signIn(email.trim(), password, deviceName)) {
            SignInAnswer.SignedIn -> {
                forgetPending()
                if (signedIn(email.trim())) SignInResult.Joining else SignInResult.SignedIn
            }
            SignInAnswer.Refused -> SignInResult.WrongEmailOrPassword
            is SignInAnswer.TooManyTries -> SignInResult.TooManyTries(answer.retryAfterSeconds)
        }
    }

    suspend fun signUp(email: String, password: String, healthDataConsent: Boolean): SignUpResult {
        val address = email.trim()
        val problems = SignUpProblems(
            email = !EMAIL_SHAPE.matches(address),
            password = password.length !in PASSWORD_LENGTH,
            consent = !healthDataConsent,
        )
        if (!problems.none) return SignUpResult.Invalid(problems)
        return offlineAs(SignUpResult.Offline) {
            // Sending again keeps the key, so the first link moves the phone on too.
            val key = pending()?.takeIf { it.email == address }?.key ?: newPendingKey()
            when (client.signUp(address, password, languageCode(), healthDataConsent, key)) {
                SignUpAnswer.ACCEPTED -> {
                    keepPending(Pending(address, password, key))
                    _state.value = AccountState.Pending(address)
                    SignUpResult.Sent
                }
                SignUpAnswer.INVALID_EMAIL -> SignUpResult.Invalid(SignUpProblems(email = true))
                SignUpAnswer.INVALID_PASSWORD -> SignUpResult.Invalid(SignUpProblems(password = true))
                SignUpAnswer.CONSENT_REQUIRED -> SignUpResult.Invalid(SignUpProblems(consent = true))
            }
        }
    }

    /** Didn't get the email: signs up again with the same address, which sends a new link. */
    suspend fun sendAgain(): SignUpResult {
        val waiting = pending() ?: return SignUpResult.Invalid(SignUpProblems(email = true))
        return signUp(waiting.email, waiting.password, healthDataConsent = true)
    }

    /** Wrong address: back to the form. Nothing left the phone, and the address stays unconfirmed. */
    fun changeAddress() {
        forgetPending()
        _state.value = AccountState.Guest
    }

    /** The link in the confirmation email, `…/confirm-email#<token>`, opened on this phone. */
    suspend fun confirmEmail(token: String): ConfirmResult = offlineAs(ConfirmResult.OFFLINE) {
        if (!client.confirmEmail(token)) return@offlineAs ConfirmResult.LINK_EXPIRED
        if (pending() == null) return@offlineAs ConfirmResult.CONFIRMED_SIGN_IN
        // The check on Check your email may have signed in a moment before.
        if (checkConfirmed() || _state.value is AccountState.SignedIn) ConfirmResult.CONFIRMED else ConfirmResult.CONFIRMED_SIGN_IN
    }

    /**
     * While the email waits for confirmation: asks whether the link was opened anywhere, such as on
     * the website, and if so signs in with the kept password. False while it is still unconfirmed or
     * offline. Asking costs no sign-in try, so the address is never throttled by it; a phone that
     * signed up before the key existed tries the password instead.
     */
    suspend fun checkConfirmed(): Boolean = checking.withLock {
        offlineAs(false) {
            val waiting = pending() ?: return@offlineAs false
            if (waiting.key != null && !client.isConfirmed(waiting.key)) return@offlineAs false
            when (client.signIn(waiting.email, waiting.password, deviceName)) {
                SignInAnswer.SignedIn -> {
                    forgetPending()
                    signedIn(waiting.email)
                    true
                }
                // Confirmed, but the password changed since: the person signs in themselves.
                SignInAnswer.Refused -> {
                    if (waiting.key != null) changeAddress()
                    false
                }
                is SignInAnswer.TooManyTries -> false
            }
        }
    }

    /**
     * While Check your email is on screen: listens for the server to say the link was opened, on any
     * device, and then signs in, turning the state [AccountState.SignedIn]. A dropped stream is
     * opened again after [reconnectAfter], with a check first for what it missed. Cancel it when
     * the screen goes. A sign-up from before the key existed is only checked once, since trying the
     * password again and again would lock the address.
     */
    fun watchConfirmation(): Cancellable {
        val job = scope.launch {
            while (_state.value is AccountState.Pending && !checkConfirmed()) {
                val key = pending()?.key ?: break
                val heard = try {
                    // Off the main thread: Android forbids network there, and closing a stream
                    // mid-read writes to its socket.
                    withContext(Dispatchers.Default) { client.awaitConfirmation(key) }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    false
                }
                if (!heard) delay(reconnectAfter)
            }
        }
        // Cancelled off the main thread too, since cancelling closes the stream.
        return Cancellable { CoroutineScope(Dispatchers.Default).launch { job.cancel() } }
    }

    /** Forgot your password: mails a link when the address has an account, without saying whether. */
    suspend fun requestPasswordReset(email: String): Boolean = offlineAs(false) {
        client.requestPasswordReset(email.trim())
        true
    }

    /** The link in the reset email, `…/reset-password#<token>`: every phone is signed out, this one included. */
    suspend fun setNewPassword(token: String, password: String): NewPasswordResult {
        if (password.length !in PASSWORD_LENGTH) return NewPasswordResult.TOO_SHORT
        return offlineAs(NewPasswordResult.OFFLINE) {
            when (client.setNewPassword(token, password)) {
                NewPasswordAnswer.SAVED -> NewPasswordResult.SAVED.also { if (_state.value is AccountState.SignedIn) becomeGuest() }
                NewPasswordAnswer.INVALID_TOKEN -> NewPasswordResult.LINK_EXPIRED
                NewPasswordAnswer.INVALID_PASSWORD -> NewPasswordResult.TOO_SHORT
            }
        }
    }

    /**
     * Back to an empty guest: workouts, sessions and what else follows the account leave the phone.
     * With changes still waiting it signs out only when [evenWithChanges].
     */
    suspend fun signOut(evenWithChanges: Boolean = false): SignOutResult {
        val result = sync.signOut(evenWithChanges)
        if (result == SignOutResult.SIGNED_OUT) {
            stopSyncing()
            _state.value = AccountState.Guest
        }
        return result
    }

    /** Deletes the account at once; this phone, like every other, goes back to a guest that keeps its copy. */
    suspend fun deleteAccount(password: String): DeleteResult = offlineAs(DeleteResult.OFFLINE) {
        when (client.deleteAccount(password)) {
            DeleteAnswer.DELETED -> DeleteResult.DELETED.also { becomeGuest() }
            DeleteAnswer.WRONG_PASSWORD -> DeleteResult.WRONG_PASSWORD
        }
    }

    /** Pull to refresh. */
    suspend fun refresh(): SyncResult = sync.run()

    /** Starts recording and syncing, and says whether the phone has guest data to send. */
    private fun signedIn(email: String): Boolean {
        memory.putString(EMAIL, email)
        store.startRecordingChanges(SyncedSettings.values(memory))
        val total = store.outbox(Int.MAX_VALUE).size.toLong()
        _upload.value = if (total > 0) UploadProgress(0, total) else null
        _state.value = AccountState.SignedIn(email, total, sync.lastSyncedAt.value)
        startSyncing()
        return total > 0
    }

    private fun startSyncing() {
        stopSyncing(keepUpload = true)
        syncing = sync.start()
        watching = scope.launch {
            combine(store.outboxSize(), sync.lastSyncedAt) { waiting, last -> waiting to last }.collect { (waiting, last) ->
                _state.update { current ->
                    if (current is AccountState.SignedIn) current.copy(waiting = waiting, lastSyncedAt = last) else current
                }
                _upload.update { progress -> progress?.copy(sent = (progress.total - waiting).coerceIn(0, progress.total)) }
            }
        }
    }

    private fun stopSyncing(keepUpload: Boolean = false) {
        syncing?.cancel()
        watching?.cancel()
        syncing = null
        watching = null
        if (!keepUpload) _upload.value = null
    }

    /** A refused sign-in, a deleted account or a new password: a guest again, keeping the phone's data. */
    private fun becomeGuest() {
        stopSyncing()
        store.stopRecordingChanges()
        secrets.putSecret(TOKENS, null)
        _state.value = AccountState.Guest
    }

    private fun initialState(): AccountState = when {
        secrets.getSecret(TOKENS) != null && memory.getString(EMAIL) != null ->
            AccountState.SignedIn(memory.getString(EMAIL)!!, 0, sync.lastSyncedAt.value)
        else -> pending()?.let { AccountState.Pending(it.email) } ?: AccountState.Guest
    }

    /** [key] is null for a sign-up from before the phone sent one. */
    @Serializable
    private data class Pending(val email: String, val password: String, val key: String? = null)

    /** 244 random bits from a secure generator, as 64 hex characters. */
    private fun newPendingKey(): String = Uuid.random().toHexString() + Uuid.random().toHexString()

    private fun pending(): Pending? =
        secrets.getSecret(PENDING)?.let { runCatching { json.decodeFromString(Pending.serializer(), it) }.getOrNull() }

    private fun keepPending(pending: Pending) = secrets.putSecret(PENDING, json.encodeToString(Pending.serializer(), pending))

    private fun forgetPending() = secrets.putSecret(PENDING, null)

    /** Any failure to reach the server, or an answer it shouldn't give, reads as no connection. */
    private suspend fun <T> offlineAs(offline: T, call: suspend () -> T): T = try {
        call()
    } catch (e: CancellationException) {
        throw e
    } catch (_: SignInRefusedByServer) {
        becomeGuest()
        offline
    } catch (_: Exception) {
        offline
    }

    internal companion object {
        const val EMAIL = "account.email"
        const val PENDING = "account.pending"
        /** The key [SecretTokenStore] keeps the tokens under. */
        const val TOKENS = "account.tokens"
        val PASSWORD_LENGTH = 8..256
        val EMAIL_SHAPE = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        private val json = Json { ignoreUnknownKeys = true }
    }
}
