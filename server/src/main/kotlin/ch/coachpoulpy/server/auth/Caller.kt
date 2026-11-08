package ch.coachpoulpy.server.auth

import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import java.util.UUID
import org.http4k.core.Request
import org.http4k.lens.RequestKey

/**
 * Whoever an API request comes from, as the server sees it ([ADR 0020]): nobody, or an
 * account reaching the API through one client with the scopes that client was granted.
 * Routes ask only "does this caller hold the scope, and is it their data"; how the
 * credential was checked stays with the [Authenticator] that made the caller.
 *
 * [ADR 0020]: https://github.com/tkint/coach-poulpy/blob/main/docs/adr/0020-every-credential-resolves-to-one-caller.md
 */
sealed interface Caller {
    /** A request with no credential. Only public routes accept it. */
    data object Anonymous : Caller

    data class Signed(
        val accountId: AccountId,
        val client: Client,
        val scopes: Set<Scope>,
        val role: Role = Role.Member,
    ) : Caller
}

/** An account's id, a UUID that can only be made through [AccountId.of] or [AccountId.parse] (values4k). */
@JvmInline
value class AccountId private constructor(override val value: UUID) : Value<UUID> {
    companion object : UUIDValueFactory<AccountId>(::AccountId)
}

/** The account's role, which is not a scope anyone can ask for (ADR 0020). */
enum class Role(val value: String) {
    Member("member"),
    Admin("admin"),
    ;

    companion object {
        fun of(value: String): Role = entries.first { it.value == value }
    }
}

/** What holds the credential: the phone app, the website, a personal access token or a third-party app. */
@JvmInline
value class Client(val value: String) {
    companion object {
        /** The Android and iOS apps. */
        val App = Client("app")

        /** The website, signed in with cookies (ADR 0020): the only client the admin area accepts. */
        val Website = Client("website")
    }
}

/** What a caller may do, as `resource:action` (ADR 0020). */
enum class Scope(val value: String) {
    WorkoutsRead("workouts:read"),
    WorkoutsWrite("workouts:write"),
    PlansRead("plans:read"),
    PlansWrite("plans:write"),
    SessionsRead("sessions:read"),
    SessionsWrite("sessions:write"),
    ProfileRead("profile:read"),
    ProfileWrite("profile:write"),
    Sync("sync"),
}

private val callerKey = RequestKey.required<Caller>("caller")

/** The caller [authenticate] stored on this request. */
val Request.caller: Caller get() = callerKey(this)

internal fun Request.withCaller(caller: Caller): Request = callerKey(caller, this)
