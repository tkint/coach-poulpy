package ch.coachpoulpy.server.accounts

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

/**
 * Argon2id password hashes in the PHC string form (`$argon2id$v=19$m=…,t=…,p=…$salt$hash`), so the
 * cost can rise later and older hashes still verify with the cost they were made with. The default
 * cost is OWASP's: 19 MiB of memory, 2 passes, 1 lane.
 */
class Passwords(
    private val memoryKib: Int = 19 * 1024,
    private val iterations: Int = 2,
    private val parallelism: Int = 1,
) {
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(password, salt, memoryKib, iterations, parallelism, HASH_BYTES)
        return "\$argon2id\$v=19\$m=$memoryKib,t=$iterations,p=$parallelism\$${encoder.encodeToString(salt)}\$${encoder.encodeToString(hash)}"
    }

    /** Whether [password] is the one [encoded] was made from; false for anything that isn't an Argon2id hash. */
    fun verify(password: String, encoded: String): Boolean {
        val parts = encoded.split('$')
        if (parts.size != 6 || parts[1] != "argon2id" || parts[2] != "v=19") return false
        val cost = parts[3].split(',').associate { it.substringBefore('=') to it.substringAfter('=').toIntOrNull() }
        val memory = cost["m"] ?: return false
        val passes = cost["t"] ?: return false
        val lanes = cost["p"] ?: return false
        val salt = runCatching { decoder.decode(parts[4]) }.getOrNull() ?: return false
        val expected = runCatching { decoder.decode(parts[5]) }.getOrNull() ?: return false
        return MessageDigest.isEqual(expected, derive(password, salt, memory, passes, lanes, expected.size))
    }

    private fun derive(password: String, salt: ByteArray, memory: Int, passes: Int, lanes: Int, length: Int): ByteArray {
        val parameters = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(memory)
            .withIterations(passes)
            .withParallelism(lanes)
            .withSalt(salt)
            .build()
        val generator = Argon2BytesGenerator().apply { init(parameters) }
        return ByteArray(length).also { generator.generateBytes(password.toCharArray(), it) }
    }

    private companion object {
        const val SALT_BYTES = 16
        const val HASH_BYTES = 32
        val random = SecureRandom()
        val encoder: Base64.Encoder = Base64.getEncoder().withoutPadding()
        val decoder: Base64.Decoder = Base64.getDecoder()
    }
}
