package ch.coachpoulpy.shared

import ch.coachpoulpy.data.remote.Tokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SecretTokenStoreTest {
    private val secrets = object : SecretStore {
        val values = mutableMapOf<String, String>()
        override fun getSecret(key: String) = values[key]
        override fun putSecret(key: String, value: String?) {
            if (value == null) values.remove(key) else values[key] = value
        }
    }
    private val store = SecretTokenStore(secrets)

    @Test
    fun tokensComeBackAsTheyWereKept() {
        val tokens = Tokens("cp_at_1", "cp_rt_1", Instant.fromEpochSeconds(1_700_000_900))
        store.write(tokens)

        assertEquals(tokens, store.read())
        store.write(null)
        assertNull(store.read())
        assertEquals(emptyMap(), secrets.values)
    }

    @Test
    fun aSecretThatNoLongerReadsIsSignedOut() {
        secrets.putSecret("account.tokens", "not json")

        assertNull(store.read())
    }
}
