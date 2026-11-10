package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.admin.AccountAction
import ch.coachpoulpy.server.admin.AccountQuery
import ch.coachpoulpy.server.admin.AccountSort
import ch.coachpoulpy.server.admin.AccountView
import ch.coachpoulpy.server.admin.AdminAccounts
import ch.coachpoulpy.server.admin.AdminAudit
import ch.coachpoulpy.server.admin.AdminSettings
import ch.coachpoulpy.server.admin.AdminStatus
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requiresAdmin
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.uuid

@Serializable
data class AuditEntryResponse(val actorEmail: String?, val action: String, val target: String, val at: String)

@Serializable
data class AuditResponse(val entries: List<AuditEntryResponse>)

private val auditBody = Body.auto<AuditResponse>().toLens()
private val adminTag = Tag("admin")

/** The admin area's routes (#335). Each admin write is recorded in the audit log. */
fun adminRoutes(audit: AdminAudit, accounts: AdminAccounts, settings: AdminSettings, status: AdminStatus): List<ContractRoute> = listOf(
    auditRoute(audit),
    listAccountsRoute(accounts),
    getAccountRoute(accounts),
    disableAccountRoute(accounts),
    enableAccountRoute(accounts),
    deleteAccountRoute(accounts),
    settingsRoute(settings),
    saveMailFromRoute(settings),
    testEmailRoute(settings),
    statusRoute(status),
    pendingRegistrationsRoute(status),
)

private fun auditRoute(audit: AdminAudit): ContractRoute = "$API_PREFIX/admin/audit" meta {
    summary = "What the admins did"
    description = "The last 100 admin writes, newest first. Admins signed in through the website only."
    operationId = "getAdminAudit"
    tags += adminTag
    security = requiresAdmin()
    returning(
        OK,
        auditBody to AuditResponse(listOf(AuditEntryResponse("ana@example.com", "admin.promote", "0190d4d2-0000-7000-8000-000000000001", "2026-11-08T10:00:00Z"))),
        "The audit log",
        definitionId = "Audit",
    )
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { _ ->
    Response(OK).with(auditBody of AuditResponse(audit.recent().map { AuditEntryResponse(it.actorEmail, it.action, it.target, it.at.toInstant().toString()) }))
}

@Serializable
data class AdminAccountResponse(
    val id: String,
    val email: String,
    val role: String,
    val confirmed: Boolean,
    val createdAt: String,
    /** When the account was disabled, or null while it can sign in. */
    val disabledAt: String?,
)

@Serializable
data class AdminAccountPageResponse(val accounts: List<AdminAccountResponse>, val total: Int)

@Serializable
data class DeleteAccountConfirmation(val confirmEmail: String)

private val accountBody = Body.auto<AdminAccountResponse>().toLens()
private val accountPageBody = Body.auto<AdminAccountPageResponse>().toLens()
private val confirmationBody = Body.auto<DeleteAccountConfirmation>().toLens()
private val adminProblemBody = Body.auto<Problem>().toLens()
private val accountId = Path.uuid().of("id", "The account's id")

private fun AccountView.toResponse() =
    AdminAccountResponse(id.toString(), email, role, confirmed, createdAt.toInstant().toString(), disabledAt?.toInstant()?.toString())

private val exampleAccount = AdminAccountResponse("0190d4d2-0000-7000-8000-000000000042", "lea@example.com", "member", true, "2026-10-02T09:00:00Z", null)

private fun Request.admin(): Caller.Signed = caller as Caller.Signed

/** The answer for what [AdminAccounts] did: 204, or why not. */
private fun AccountAction.toResponse(): Response = when (this) {
    AccountAction.Done -> Response(NO_CONTENT)
    AccountAction.Unknown -> Response(NOT_FOUND).with(adminProblemBody of Problem("unknown-account"))
    AccountAction.OwnAccount -> Response(CONFLICT).with(adminProblemBody of Problem("own-account"))
    AccountAction.ConfirmationMismatch -> Response(BAD_REQUEST).with(adminProblemBody of Problem("confirmation-mismatch"))
}

private val searchQuery = Query.optional("q", "Part of the address, whatever its case")
private val roleQuery = Query.optional("role", "member or admin")
private val confirmedQuery = Query.optional("confirmed", "yes or no: whether the email is confirmed")
private val signInQuery = Query.optional("signIn", "active or disabled")
private val sortQuery = Query.optional("sort", "email, role, confirmed, signIn or created (default created)")
private val orderQuery = Query.optional("order", "asc or desc (default desc)")
private val pageQuery = Query.int().defaulted("page", 1, "The page, from 1")
private val sizeQuery = Query.int().defaulted("size", AccountQuery.DEFAULT_PAGE_SIZE, "Accounts per page, up to ${AccountQuery.MAX_PAGE_SIZE}")

private val sorts = mapOf(
    "email" to AccountSort.Email,
    "role" to AccountSort.Role,
    "confirmed" to AccountSort.EmailStatus,
    "signIn" to AccountSort.SignIn,
    "created" to AccountSort.Created,
)

/** The table's question from the query string, or null when a value isn't one of the allowed words. */
private fun Request.accountQuery(): AccountQuery? {
    val role = roleQuery(this)
    val confirmed = confirmedQuery(this)
    val signIn = signInQuery(this)
    val sort = sortQuery(this)
    val order = orderQuery(this)
    if (role !in listOf(null, "member", "admin") || confirmed !in listOf(null, "yes", "no") ||
        signIn !in listOf(null, "active", "disabled") || sort != null && sort !in sorts || order !in listOf(null, "asc", "desc")
    ) return null
    return AccountQuery(
        emailContains = searchQuery(this),
        role = role,
        confirmed = confirmed?.let { it == "yes" },
        disabled = signIn?.let { it == "disabled" },
        sort = sort?.let(sorts::getValue) ?: AccountSort.Created,
        ascending = order == "asc",
        page = pageQuery(this),
        size = sizeQuery(this),
    )
}

private fun listAccountsRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" meta {
    summary = "List the accounts"
    description = "One page of accounts, filtered and sorted, with how many match in all: email, whether it is confirmed, role, " +
        "creation date and whether it is disabled. Never a workout or health data. A value that isn't an allowed word is a 400."
    operationId = "listAdminAccounts"
    tags += adminTag
    queries += searchQuery
    queries += roleQuery
    queries += confirmedQuery
    queries += signInQuery
    queries += sortQuery
    queries += orderQuery
    queries += pageQuery
    queries += sizeQuery
    security = requiresAdmin()
    returning(OK, accountPageBody to AdminAccountPageResponse(listOf(exampleAccount), 1), "A page of accounts", definitionId = "AdminAccountPage")
    returning(BAD_REQUEST, adminProblemBody to Problem("bad-query"), "A filter or sort value isn't allowed", definitionId = "Problem")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { request ->
    request.accountQuery()?.let { query ->
        val page = accounts.list(query)
        Response(OK).with(accountPageBody of AdminAccountPageResponse(page.accounts.map { it.toResponse() }, page.total))
    } ?: Response(BAD_REQUEST).with(adminProblemBody of Problem("bad-query"))
}

private fun getAccountRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" / accountId meta {
    summary = "One account"
    description = "The account's state, as the list shows it, so a link to one account opens it even when it is not on the page. 404 when the id is unknown."
    operationId = "getAdminAccount"
    tags += adminTag
    security = requiresAdmin()
    returning(OK, accountBody to exampleAccount, "The account", definitionId = "AdminAccount")
    returning(NOT_FOUND, adminProblemBody to Problem("unknown-account"), "No account with this id", definitionId = "Problem")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { id ->
    { _: Request ->
        accounts.get(id)?.let { Response(OK).with(accountBody of it.toResponse()) }
            ?: Response(NOT_FOUND).with(adminProblemBody of Problem("unknown-account"))
    }
}

private fun disableAccountRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" / accountId / "disable" meta {
    summary = "Disable an account"
    description = "Blocks sign-in and signs every device of the account out, tokens included. Reversible with enable. " +
        "An admin can't disable their own account (409)."
    operationId = "disableAdminAccount"
    tags += adminTag
    security = requiresAdmin()
    returning(NO_CONTENT to "The account is disabled")
    returning(NOT_FOUND to "Unknown account")
    returning(CONFLICT to "Your own account")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { id, _ ->
    { request: Request -> accounts.disable(request.admin().accountId, id).toResponse() }
}

private fun enableAccountRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" / accountId / "enable" meta {
    summary = "Enable a disabled account"
    description = "The account can sign in again; its devices sign in anew."
    operationId = "enableAdminAccount"
    tags += adminTag
    security = requiresAdmin()
    returning(NO_CONTENT to "The account is enabled")
    returning(NOT_FOUND to "Unknown account")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { id, _ ->
    { request: Request -> accounts.enable(request.admin().accountId, id).toResponse() }
}

private fun deleteAccountRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" / accountId meta {
    summary = "Delete an account"
    description = "Deletes the account and every row of it, as the user's own deletion does. The body must repeat the " +
        "account's email (400 confirmation-mismatch otherwise). An admin can't delete their own account (409)."
    operationId = "deleteAdminAccount"
    tags += adminTag
    security = requiresAdmin()
    receiving(confirmationBody to DeleteAccountConfirmation("lea@example.com"), definitionId = "DeleteAccountConfirmation")
    returning(NO_CONTENT to "The account is gone")
    returning(BAD_REQUEST to "The email typed isn't the account's")
    returning(NOT_FOUND to "Unknown account")
    returning(CONFLICT to "Your own account")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract DELETE to { id ->
    { request: Request -> accounts.delete(request.admin().accountId, id, confirmationBody(request).confirmEmail).toResponse() }
}

@Serializable
data class AdminSettingsResponse(
    /** The from-address emails leave from now, or null when none is set (no MAIL_FROM, as on a laptop). */
    val mailFrom: String?,
    /** Whether [mailFrom] was saved in the admin; false while it is MAIL_FROM. */
    val mailFromSaved: Boolean,
)

@Serializable
data class MailFromChange(val mailFrom: String)

@Serializable
data class TestEmailResponse(
    val sent: Boolean,
    /** What the mail server said when it refused the email, or null when it took it. */
    val error: String?,
)

private val settingsBody = Body.auto<AdminSettingsResponse>().toLens()
private val mailFromBody = Body.auto<MailFromChange>().toLens()
private val testEmailBody = Body.auto<TestEmailResponse>().toLens()

private fun settingsRoute(settings: AdminSettings): ContractRoute = "$API_PREFIX/admin/settings" meta {
    summary = "The server settings"
    description = "The settings an admin changes without a deploy: the from-address of the emails, the saved one or else MAIL_FROM. " +
        "The SMTP host, port, user and password stay in the environment and are not shown."
    operationId = "getAdminSettings"
    tags += adminTag
    security = requiresAdmin()
    returning(OK, settingsBody to AdminSettingsResponse("Coach Poulpy <hello@example.com>", true), "The settings", definitionId = "AdminSettings")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { _ ->
    val mail = settings.mail()
    Response(OK).with(settingsBody of AdminSettingsResponse(mail.mailFrom, mail.saved))
}

private fun saveMailFromRoute(settings: AdminSettings): ContractRoute = "$API_PREFIX/admin/settings/mail-from" meta {
    summary = "Change the from-address"
    description = "Saves the address the next emails come from, such as Coach Poulpy <hello@example.com>, in place of MAIL_FROM. " +
        "Something that isn't one email address is a 400 invalid-address. Written to the audit log."
    operationId = "saveAdminMailFrom"
    tags += adminTag
    security = requiresAdmin()
    receiving(mailFromBody to MailFromChange("Coach Poulpy <hello@example.com>"), definitionId = "MailFromChange")
    returning(NO_CONTENT to "Saved: the next email uses it")
    returning(BAD_REQUEST, adminProblemBody to Problem("invalid-address"), "Not an email address", definitionId = "Problem")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { request ->
    if (settings.saveMailFrom(request.admin().accountId, mailFromBody(request).mailFrom)) {
        Response(NO_CONTENT)
    } else {
        Response(BAD_REQUEST).with(adminProblemBody of Problem("invalid-address"))
    }
}

private fun testEmailRoute(settings: AdminSettings): ContractRoute = "$API_PREFIX/admin/settings/test-email" meta {
    summary = "Send a test email"
    description = "Sends an email to the signed-in admin's own address with the saved settings, and says whether the mail server took it " +
        "or what it answered when it refused it."
    operationId = "sendAdminTestEmail"
    tags += adminTag
    security = requiresAdmin()
    returning(OK, testEmailBody to TestEmailResponse(false, "535 Authentication failed"), "What the mail server answered", definitionId = "TestEmail")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract POST to { request ->
    val result = settings.sendTestEmail(request.admin().accountId)
    Response(OK).with(testEmailBody of TestEmailResponse(result.error == null, result.error))
}

@Serializable
data class LastSendResponse(
    val at: String,
    /** `confirmation`, `password-reset` or `test`. */
    val kind: String,
    val sent: Boolean,
    /** What the mail server said when it refused the email, or null when it took it. */
    val error: String?,
)

@Serializable
data class MailStatusResponse(
    /** False while the server has no mail server (SMTP_HOST unset): no email can be sent. */
    val configured: Boolean,
    /** The last email the server tried to send since it started, or null. */
    val lastSend: LastSendResponse?,
)

@Serializable
data class AdminStatusResponse(val mail: MailStatusResponse)

@Serializable
data class PendingRegistrationResponse(val email: String, val signedUpAt: String, val linkExpiresAt: String?)

@Serializable
data class PendingRegistrationsResponse(val registrations: List<PendingRegistrationResponse>)

private val statusBody = Body.auto<AdminStatusResponse>().toLens()
private val pendingBody = Body.auto<PendingRegistrationsResponse>().toLens()

private fun statusRoute(status: AdminStatus): ContractRoute = "$API_PREFIX/admin/status" meta {
    summary = "What the server can do right now"
    description = "Whether a mail server is configured, and how the last email since the server started went: when, which kind, " +
        "and the mail server's error if it refused it. The SMTP settings themselves are never shown."
    operationId = "getAdminStatus"
    tags += adminTag
    security = requiresAdmin()
    returning(
        OK,
        statusBody to AdminStatusResponse(MailStatusResponse(true, LastSendResponse("2026-11-09T07:41:00Z", "confirmation", true, null))),
        "The status",
        definitionId = "AdminStatus",
    )
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { _ ->
    val mail = status.mail()
    val last = mail.last?.let { LastSendResponse(it.at.toString(), it.kind.code, it.sent, it.error) }
    Response(OK).with(statusBody of AdminStatusResponse(MailStatusResponse(mail.configured, last)))
}

private fun pendingRegistrationsRoute(status: AdminStatus): ContractRoute = "$API_PREFIX/admin/pending-registrations" meta {
    summary = "The accounts not confirmed yet"
    description = "The ${AdminStatus.PENDING_LIMIT} newest accounts whose email isn't confirmed, newest first: the address, when it signed up, " +
        "and when its latest confirmation link expires (null when none is left). Sends no email."
    operationId = "getAdminPendingRegistrations"
    tags += adminTag
    security = requiresAdmin()
    returning(
        OK,
        pendingBody to PendingRegistrationsResponse(listOf(PendingRegistrationResponse("lea@example.com", "2026-11-09T07:41:00Z", "2026-11-10T07:41:00Z"))),
        "The pending registrations",
        definitionId = "PendingRegistrations",
    )
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { _ ->
    val registrations = status.pendingRegistrations().map {
        PendingRegistrationResponse(it.email, it.signedUpAt.toInstant().toString(), it.linkExpiresAt?.toInstant()?.toString())
    }
    Response(OK).with(pendingBody of PendingRegistrationsResponse(registrations))
}
