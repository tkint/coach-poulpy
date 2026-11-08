package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.admin.AdminAudit
import ch.coachpoulpy.server.auth.requiresAdmin
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class AuditEntryResponse(val actorEmail: String?, val action: String, val target: String, val at: String)

@Serializable
data class AuditResponse(val entries: List<AuditEntryResponse>)

private val auditBody = Body.auto<AuditResponse>().toLens()
private val adminTag = Tag("admin")

/** The admin area's routes (#335). Each admin write is recorded in the audit log. */
fun adminRoutes(audit: AdminAudit): List<ContractRoute> = listOf(auditRoute(audit))

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
