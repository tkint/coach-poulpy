package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.Workout
import ch.coachpoulpy.core.model.WorkoutId
import ch.coachpoulpy.data.local.LocalStore
import ch.coachpoulpy.data.local.OutboxEntry
import ch.coachpoulpy.data.local.applyPulled
import ch.coachpoulpy.data.local.clearAccountData
import ch.coachpoulpy.data.local.dropFromOutbox
import ch.coachpoulpy.data.local.forkUnsent
import ch.coachpoulpy.data.local.outboxSize
import ch.coachpoulpy.data.local.syncCursor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The account's sync calls (docs/accounts.md, Sync), with the sign-in's tokens behind them. The
 * apps give one over HTTP once the account screens are built; tests give a fake.
 */
interface SyncServer {
    /** The changes after [cursor], all of them when it is null. Throws [SignInRefused] when the refresh token is refused. */
    suspend fun pull(cursor: String?, limit: Int): SyncPage

    /** Throws [SignInRefused] when the refresh token is refused. */
    suspend fun push(push: SyncPush): PushAnswer

    /** Revokes this phone's sign-in (RFC 7009). */
    suspend fun revoke()
}

/** What the server answers a push. */
sealed interface PushAnswer {
    /** All of it is stored. */
    data object Accepted : PushAnswer

    /** Nothing is stored: these workouts' versions don't follow the server's latest (409 stale-base). */
    data class StaleBase(val workouts: List<WorkoutId>) : PushAnswer

    /** Nothing is stored: the change at [index] can never be (400 invalid-change). */
    data class InvalidChange(val index: Int) : PushAnswer
}

/** The server refused the refresh token: after a password reset, the account's deletion or a revoked sign-in. */
class SignInRefused : Exception("The server refused the sign-in")

/** How a run ended. */
enum class SyncResult {
    /** Pulled everything and pushed the whole outbox. */
    SYNCED,
    /** The phone is a guest or its email is unconfirmed: nothing to do. */
    NOT_SIGNED_IN,
    /** Another run was going; it runs once more when it ends. */
    ALREADY_RUNNING,
    /** The server could not be reached or kept refusing; the outbox waits for the next run. */
    FAILED,
    /** The sign-in was refused: the phone is a guest again, with its data. */
    SIGNED_OUT,
}

enum class SignOutResult {
    SIGNED_OUT,
    /** Changes still wait in the outbox: the phone offers to wait, or to sign out anyway. */
    CHANGES_WAITING,
}

/**
 * Pulls then pushes for a phone signed in with a confirmed email ([LocalStore.recordingChanges]).
 * It runs when the app opens ([start]), [afterChange] once a change is made, and on pull to refresh
 * ([run]), never two runs at once. There is no background sync. [memory] is the phone's own
 * key-value store, not one that records changes: what a pull brings is not pushed back.
 */
class SyncClient(
    private val store: LocalStore,
    private val memory: KeyValueStore,
    private val server: SyncServer,
    private val deviceName: String,
    private val languageCode: () -> String,
    private val scope: CoroutineScope,
    private val onSignInRefused: () -> Unit = {},
    private val afterChange: Duration = 5.seconds,
    private val clock: Clock = Clock.System,
) {
    private val running = Mutex()
    private var again = false
    private val _lastSyncedAt = MutableStateFlow(memory.getString(LAST_SYNCED)?.let { runCatching { Instant.parse(it) }.getOrNull() })

    /** When a run last pulled and pushed everything, kept between launches; null before the first. */
    val lastSyncedAt: StateFlow<Instant?> = _lastSyncedAt.asStateFlow()

    /** Runs once now, then [afterChange] after each change while [scope] lives. */
    fun start(): Job = scope.launch {
        launch { run() }
        store.outboxSize().collectLatest { waiting ->
            if (waiting == 0L) return@collectLatest
            delay(afterChange)
            // Launched apart, so a change made during the run doesn't cancel it.
            scope.launch { run() }
        }
    }

    /** One run: pull until nothing more waits, then push the outbox in batches. */
    suspend fun run(): SyncResult {
        if (!running.tryLock()) {
            again = true
            return SyncResult.ALREADY_RUNNING
        }
        try {
            var result: SyncResult
            do {
                again = false
                result = once()
            } while (again && result == SyncResult.SYNCED)
            return result
        } finally {
            running.unlock()
        }
    }

    /**
     * Revokes the sign-in and clears what follows the account: workouts, sessions, plans, the body
     * log, the profile, units and language, with the outbox and cursor. Theme, app icon and paired
     * devices stay. With changes still to push it signs out only when [evenWithChanges].
     */
    suspend fun signOut(evenWithChanges: Boolean = false): SignOutResult = running.withLock {
        if (!evenWithChanges && store.outbox(1).isNotEmpty()) return SignOutResult.CHANGES_WAITING
        try {
            server.revoke()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // The phone forgets its tokens either way; one left unrevoked ends when it expires.
        }
        store.clearAccountData()
        SyncedSettings.KEYS.forEach { memory.putString(it, null) }
        memory.putString(LAST_SYNCED, null)
        _lastSyncedAt.value = null
        SignOutResult.SIGNED_OUT
    }

    private suspend fun once(): SyncResult {
        if (!store.recordingChanges()) return SyncResult.NOT_SIGNED_IN
        return try {
            pullAll()
            repeat(STALE_BASE_ATTEMPTS) {
                val stale = pushAll() ?: return SyncResult.SYNCED.also { synced() }
                // Another phone pushed first: pulling forks what clashes, and what is left is forked here.
                pullAll()
                store.forkUnsent(stale.workouts, ::forkName)
            }
            SyncResult.FAILED
        } catch (e: CancellationException) {
            throw e
        } catch (_: SignInRefused) {
            store.stopRecordingChanges()
            onSignInRefused()
            SyncResult.SIGNED_OUT
        } catch (_: Exception) {
            SyncResult.FAILED
        }
    }

    private fun synced() {
        val now = clock.now()
        memory.putString(LAST_SYNCED, now.toString())
        _lastSyncedAt.value = now
    }

    private suspend fun pullAll() {
        do {
            val page = server.pull(store.syncCursor(), PAGE_MAX)
            store.applyPulled(page, ::forkName) { change ->
                if (SettingChange.follows(change.key)) memory.putString(change.key, change.value)
            }
        } while (page.more)
    }

    /** Pushes until the outbox is empty, or returns the stale-base answer that stopped it. */
    private suspend fun pushAll(): PushAnswer.StaleBase? {
        while (true) {
            val batch = batch()
            if (batch.isEmpty()) return null
            when (val answer = server.push(SyncPush(batch.map { it.change }))) {
                PushAnswer.Accepted -> store.removeFromOutbox(batch.last().seq)
                is PushAnswer.StaleBase -> return answer
                // Sending it again would be refused again, and hold back every change after it.
                is PushAnswer.InvalidChange -> store.dropFromOutbox(batch[answer.index.coerceIn(batch.indices)].seq)
            }
        }
    }

    /** The oldest changes that fit in one push: at most [PAGE_MAX] of them and [PUSH_BYTES]. */
    private fun batch(): List<OutboxEntry> {
        var bytes = 0
        return store.outbox(PAGE_MAX).takeWhileIndexed { index, entry ->
            bytes += entry.bytes + 1
            index == 0 || bytes <= PUSH_BYTES
        }
    }

    private fun forkName(workout: Workout) = Labels(languageCode()).forkName(workout.name, deviceName)

    private inline fun <T> List<T>.takeWhileIndexed(predicate: (Int, T) -> Boolean): List<T> {
        val taken = mutableListOf<T>()
        for ((index, item) in withIndex()) if (predicate(index, item)) taken += item else break
        return taken
    }

    private companion object {
        const val PAGE_MAX = 500
        /** Under the server's 1 MB, leaving room for the wrapping object. */
        const val PUSH_BYTES = 900_000
        const val STALE_BASE_ATTEMPTS = 3
        const val LAST_SYNCED = "account.lastSyncedAt"
    }
}
