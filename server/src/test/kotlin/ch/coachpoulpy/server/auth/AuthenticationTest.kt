package ch.coachpoulpy.server.auth

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticationTest {
    private val ana = Caller.Signed(AccountId("0190d4d2-0000-7000-8000-000000000001"), Client("app"), setOf(Scope.Sync))

    private val app = authenticate(
        mapOf(
            "cp_at_" to Authenticator { if (it == "cp_at_valid") ana else null },
            "cp_pat_" to Authenticator { null },
        ),
    ).then { request -> Response(OK).body(request.caller.toString()) }

    @Test
    fun noCredentialGivesTheAnonymousCaller() {
        val response = app(Request(GET, "/"))
        assertEquals(OK, response.status)
        assertEquals(Caller.Anonymous.toString(), response.bodyString())
    }

    @Test
    fun aKnownTokenGivesItsCaller() {
        val response = app(Request(GET, "/").header("Authorization", "Bearer cp_at_valid"))
        assertEquals(ana.toString(), response.bodyString())
    }

    @Test
    fun aTokenItsAuthenticatorRefusesIs401() {
        assertUnauthorized("Bearer cp_at_expired")
        assertUnauthorized("Bearer cp_pat_anything")
    }

    @Test
    fun aTokenWithNoAuthenticatorIs401() {
        assertUnauthorized("Bearer cp_rt_refresh-tokens-are-not-access-tokens")
        assertUnauthorized("Bearer ")
    }

    @Test
    fun aCredentialThatIsNotBearerIs401() {
        assertUnauthorized("Basic YW5hOnNlY3JldA==")
    }

    private fun assertUnauthorized(authorization: String) {
        val response = app(Request(GET, "/").header("Authorization", authorization))
        assertEquals(UNAUTHORIZED, response.status)
        assertEquals("Bearer", response.header("WWW-Authenticate"))
    }
}
