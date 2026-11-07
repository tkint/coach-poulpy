package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.SyncPush
import ch.coachpoulpy.data.remote.AccountClient
import ch.coachpoulpy.data.remote.TokenStore
import ch.coachpoulpy.data.remote.Tokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class RemoteSyncServerTest {
    @Test
    fun aRefusedRefreshTokenReachesTheSyncClientAsARefusedSignIn() = runTest {
        val tokens = object : TokenStore {
            var held: Tokens? = Tokens("cp_at_1", "cp_rt_1", Instant.fromEpochSeconds(0))
            override fun read() = held
            override fun write(tokens: Tokens?) { held = tokens }
        }
        val engine = MockEngine { respond("""{"error":"invalid_grant"}""", HttpStatusCode.BadRequest) }
        val server = RemoteSyncServer(AccountClient("https://api.example.com", tokens, engine))

        assertFailsWith<SignInRefused> { server.pull(null, 500) }
        assertFailsWith<SignInRefused> { server.push(SyncPush(emptyList())) }
    }
}
