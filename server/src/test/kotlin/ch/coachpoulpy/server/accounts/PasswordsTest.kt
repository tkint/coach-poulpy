package ch.coachpoulpy.server.accounts

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordsTest {
    private val passwords = Passwords()

    @Test
    fun aHashIsArgon2idWithOwaspCost() {
        assertTrue(passwords.hash("correct horse").startsWith("\$argon2id\$v=19\$m=19456,t=2,p=1\$"))
    }

    @Test
    fun theRightPasswordVerifies() {
        val hash = passwords.hash("correct horse")
        assertTrue(passwords.verify("correct horse", hash))
        assertFalse(passwords.verify("correct horsf", hash))
    }

    @Test
    fun eachHashHasItsOwnSalt() {
        assertNotEquals(passwords.hash("correct horse"), passwords.hash("correct horse"))
    }

    @Test
    fun aHashVerifiesWithTheCostItWasMadeWith() {
        val cheap = Passwords(memoryKib = 64, iterations = 1).hash("correct horse")
        assertTrue(passwords.verify("correct horse", cheap))
    }

    @Test
    fun anythingElseNeverVerifies() {
        for (encoded in listOf("", "plain", "\$argon2i\$v=19\$m=64,t=1,p=1\$c2FsdA\$aGFzaA", "\$argon2id\$v=19\$m=x,t=1,p=1\$c2FsdA\$aGFzaA")) {
            assertFalse(passwords.verify("correct horse", encoded), encoded)
        }
    }
}
