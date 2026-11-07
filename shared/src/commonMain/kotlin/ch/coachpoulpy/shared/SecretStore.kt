package ch.coachpoulpy.shared

import ch.coachpoulpy.data.remote.TokenStore
import ch.coachpoulpy.data.remote.Tokens
import kotlinx.serialization.json.Json

/** Secrets kept between launches, encrypted by the phone: the Keychain on iOS, the Android Keystore. */
interface SecretStore {
    fun getSecret(key: String): String?
    fun putSecret(key: String, value: String?)
}

/** The sign-in's tokens, kept as one secret. One that no longer reads, such as from another phone, is signed out. */
class SecretTokenStore(private val secrets: SecretStore) : TokenStore {
    override fun read(): Tokens? =
        secrets.getSecret(KEY)?.let { runCatching { json.decodeFromString(Tokens.serializer(), it) }.getOrNull() }

    override fun write(tokens: Tokens?) = secrets.putSecret(KEY, tokens?.let { json.encodeToString(Tokens.serializer(), it) })

    private companion object {
        const val KEY = "account.tokens"
        val json = Json { ignoreUnknownKeys = true }
    }
}
