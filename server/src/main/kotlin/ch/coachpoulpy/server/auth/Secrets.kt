package ch.coachpoulpy.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** The random tokens the server hands out, in links and to clients, and the hash it keeps of them. */
internal object Secrets {
    private val random = SecureRandom()

    /** 256 random bits, URL-safe, after an optional readable [prefix] such as `cp_at_`, so secret scanners spot a leak. */
    fun newToken(prefix: String = ""): String =
        prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))

    /** Only this is stored, so a leaked table gives no token. */
    fun hashOf(token: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
}
