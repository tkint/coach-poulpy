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

/** The columns the admin's accounts table sorts on. */
enum class AccountSort { Email, Role, EmailStatus, SignIn, Created }

/** What the admin asked of the accounts table: filters, one sort and a page. A null filter lets everything through. */
data class AccountQuery(
    val emailContains: String? = null,
    val role: String? = null,
    val confirmed: Boolean? = null,
    val disabled: Boolean? = null,
    val sort: AccountSort = AccountSort.Created,
    val ascending: Boolean = false,
    val page: Int = 1,
    val size: Int = DEFAULT_PAGE_SIZE,
) {
    companion object {
        const val DEFAULT_PAGE_SIZE = 8
        const val MAX_PAGE_SIZE = 100
    }
}

/** One page of accounts, and how many accounts match the filters in all. */
data class AccountPage(val accounts: List<AccountView>, val total: Int)

/** Filters, sorts and cuts [all] as [query] says; ties on the sort keep the email order. */
fun pageOf(all: List<AccountView>, query: AccountQuery): AccountPage {
    val needle = query.emailContains?.trim()?.lowercase().orEmpty()
    val matching = all.filter {
        it.email.lowercase().contains(needle) &&
            (query.role == null || it.role == query.role) &&
            (query.confirmed == null || it.confirmed == query.confirmed) &&
            (query.disabled == null || (it.disabledAt != null) == query.disabled)
    }
    val key: Comparator<AccountView> = when (query.sort) {
        AccountSort.Email -> compareBy { it.email.lowercase() }
        AccountSort.Role -> compareBy { it.role }
        AccountSort.EmailStatus -> compareBy { it.confirmed }
        AccountSort.SignIn -> compareBy { it.disabledAt != null }
        AccountSort.Created -> compareBy { it.createdAt }
    }
    val byEmail = compareBy<AccountView> { it.email.lowercase() }
    val sorted = matching.sortedWith((if (query.ascending) key else key.reversed()).then(byEmail))
    val size = query.size.coerceIn(1, AccountQuery.MAX_PAGE_SIZE)
    return AccountPage(sorted.drop((query.page.coerceAtLeast(1) - 1) * size).take(size), matching.size)
}

/** What an admin did to an account, or why it was refused. */
enum class AccountAction { Done, Unknown, OwnAccount, ConfirmationMismatch }

/**
 * Listing, disabling and deleting accounts for the admin (#336). Each change emits an
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

    /** The page of accounts [query] asks for. */
    fun list(query: AccountQuery): AccountPage = pageOf(
        queries.adminList().executeAsList().map {
            AccountView(it.id, it.email, it.role, it.confirmed_at != null, it.created_at, it.disabled_at)
        },
        query,
    )

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
