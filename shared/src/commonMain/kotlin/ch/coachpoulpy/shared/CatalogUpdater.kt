package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.CatalogRevision
import ch.coachpoulpy.data.remote.CatalogClient
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Brings the phone's catalog up to date from the server at launch (ADR 0030, #27): no account, and
 * nothing the user waits for. Offline, or on any failure, the phone keeps the catalog it has and the
 * next launch asks again from the same cursor.
 */
class CatalogUpdater(
    private val repository: CatalogRepository,
    private val client: CatalogClient,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    /** Asks the server in the background, once. */
    fun start() {
        scope.launch {
            try {
                update()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Offline or an answer that wasn't one: keep what the phone has.
            }
        }
    }

    /** Asks for what was published after the phone's cursor and stores it. Returns how many exercises changed; throws when the server can't be reached. */
    suspend fun update(): Int {
        val answer = client.since(repository.cursor) ?: return 0
        val revisions = answer.exercises.map { CatalogRevision(it.exercise, it.revision, it.variationGroup, it.variationRank) }
        return repository.apply(revisions, answer.cursor)
    }
}
