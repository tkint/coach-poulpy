package ch.coachpoulpy.server.admin

import ch.coachpoulpy.server.accounts.AccountDeletion
import ch.coachpoulpy.server.auth.AccountId
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.db.Database
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID
import org.http4k.events.Events

/** The state of an account as the admin sees it (#336): never a workout or any health data. */
data class AccountView(
    val id: UUID,
    val email: String,
    val role: String,
    val confirmed: Boolean,
    val createdAt: OffsetDateTime,
    val disabledAt: OffsetDateTime?,
)

/** What an admin did to an account, or why it was refused. */
enum class AccountAction { Done, Unknown, OwnAccount, ConfirmationMismatch }

/**
 * Finding, disabling and deleting accounts for the admin (#336). Each change emits an
 * [AdminAction] whose target is the account's id, never its address.
 */
class AdminAccounts(
    private val database: Database,
    private val signIns: SignIns,
    private val deletion: AccountDeletion,
    private val events: Events,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val queries get() = database.queries.accountQueries

    /** The account with [email], whatever its case, or null. */
    fun find(email: String): AccountView? = queries.adminView(email.trim()).executeAsOneOrNull()?.let {
        AccountView(it.id, it.email, it.role, it.confirmed_at != null, it.created_at, it.disabled_at)
    }

    /** Blocks sign-in and signs every device of the account out; [AccountAction.Done] also for an account already disabled. */
    fun disable(actor: AccountId, id: UUID): AccountAction {
        if (actor.value == id) return AccountAction.OwnAccount
        if (queries.byId(id).executeAsOneOrNull() == null) return AccountAction.Unknown
        val now = OffsetDateTime.now(clock)
        database.queries.transaction {
            if (queries.disable(now, id).executeAsOneOrNull() != null) signIns.revokeAll(id, now)
        }
        events(AdminAction(actor, "admin.account.disable", id.toString()))
        return AccountAction.Done
    }

    /** Lets the account sign in again; its devices sign in anew. */
    fun enable(actor: AccountId, id: UUID): AccountAction {
        if (queries.byId(id).executeAsOneOrNull() == null) return AccountAction.Unknown
        queries.enable(id).executeAsOneOrNull()
        events(AdminAction(actor, "admin.account.enable", id.toString()))
        return AccountAction.Done
    }

    /** Deletes the account and every row of it, as the user's own deletion does, once [confirmEmail] is its address. */
    fun delete(actor: AccountId, id: UUID, confirmEmail: String): AccountAction {
        if (actor.value == id) return AccountAction.OwnAccount
        val account = queries.byId(id).executeAsOneOrNull() ?: return AccountAction.Unknown
        if (!account.email.equals(confirmEmail.trim(), ignoreCase = true)) return AccountAction.ConfirmationMismatch
        deletion.erase(id)
        events(AdminAction(actor, "admin.account.delete", id.toString()))
        return AccountAction.Done
    }
}
