package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.SyncPage
import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.data.remote.AccountClient
import ch.coachpoulpy.data.remote.PushResult
import ch.coachpoulpy.data.remote.SignInRefusedByServer
import ch.coachpoulpy.data.remote.UnexpectedAnswer

/** The sync client's server over HTTP, with the sign-in the [client] holds. */
class RemoteSyncServer(private val client: AccountClient) : SyncServer {
    override suspend fun pull(cursor: String?, limit: Int): SyncPage = refusedAsSignInRefused { client.pull(cursor, limit) }

    override suspend fun push(push: SyncPush): PushAnswer = refusedAsSignInRefused {
        when (val result = client.push(push)) {
            PushResult.Accepted -> PushAnswer.Accepted
            is PushResult.StaleBase -> PushAnswer.StaleBase(result.workouts)
            is PushResult.InvalidChange -> PushAnswer.InvalidChange(result.index)
            // The client keeps under the limits, so this is a single change too large to ever go.
            PushResult.TooLarge -> if (push.changes.size == 1) PushAnswer.InvalidChange(0) else throw UnexpectedAnswer(413)
        }
    }

    override suspend fun revoke() = client.signOut()

    private inline fun <T> refusedAsSignInRefused(call: () -> T): T = try {
        call()
    } catch (_: SignInRefusedByServer) {
        throw SignInRefused()
    }
}
