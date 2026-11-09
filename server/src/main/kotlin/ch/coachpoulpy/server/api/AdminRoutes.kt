package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.admin.AccountAction
import ch.coachpoulpy.server.admin.AccountView
import ch.coachpoulpy.server.admin.AdminAccounts
import ch.coachpoulpy.server.admin.AdminAudit
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requiresAdmin
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
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
import org.http4k.lens.uuid

@Serializable
data class AuditEntryResponse(val actorEmail: String?, val action: String, val target: String, val at: String)

@Serializable
data class AuditResponse(val entries: List<AuditEntryResponse>)

private val auditBody = Body.auto<AuditResponse>().toLens()
private val adminTag = Tag("admin")

/** The admin area's routes (#335). Each admin write is recorded in the audit log. */
fun adminRoutes(audit: AdminAudit, accounts: AdminAccounts): List<ContractRoute> = listOf(
    auditRoute(audit),
    findAccountRoute(accounts),
    disableAccountRoute(accounts),
    enableAccountRoute(accounts),
    deleteAccountRoute(accounts),
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
data class DeleteAccountConfirmation(val confirmEmail: String)

private val accountBody = Body.auto<AdminAccountResponse>().toLens()
private val confirmationBody = Body.auto<DeleteAccountConfirmation>().toLens()
private val adminProblemBody = Body.auto<Problem>().toLens()
private val emailQuery = Query.required("email", "The address, whatever its case")
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

private fun findAccountRoute(accounts: AdminAccounts): ContractRoute = "$API_PREFIX/admin/accounts" meta {
    summary = "Find an account by its email"
    description = "The account's state: email, whether it is confirmed, role, creation date and whether it is disabled. " +
        "Never a workout or health data. 404 when no account has this address."
    operationId = "findAdminAccount"
    tags += adminTag
    queries += emailQuery
    security = requiresAdmin()
    returning(OK, accountBody to exampleAccount, "The account", definitionId = "AdminAccount")
    returning(NOT_FOUND, adminProblemBody to Problem("unknown-account"), "No account with this address", definitionId = "Problem")
    returning(FORBIDDEN to "Not an admin, or not signed in through the website")
} bindContract GET to { request ->
    accounts.find(emailQuery(request))?.let { Response(OK).with(accountBody of it.toResponse()) }
        ?: Response(NOT_FOUND).with(adminProblemBody of Problem("unknown-account"))
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
