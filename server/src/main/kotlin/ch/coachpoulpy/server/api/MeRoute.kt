package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import ch.coachpoulpy.server.db.Database
import java.util.UUID
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

@Serializable
data class MeResponse(val accountId: String, val email: String, val role: String, val client: String, val scopes: List<String>)

private val meBody = Body.auto<MeResponse>().toLens()

/** Who the caller is: the account and the client and scopes of the token used (ADR 0020). */
fun meRoute(database: Database): ContractRoute = "$API_PREFIX/me" meta {
    summary = "Who you are"
    description = "The account, and the client and scopes of the token sent."
    operationId = "getMe"
    tags += Tag("accounts")
    security = requires()
    returning(
        OK,
        meBody to MeResponse("0190d4d2-0000-7000-8000-000000000001", "ana@example.com", "member", "app", listOf("sync")),
        "The caller",
        definitionId = "Me",
    )
} bindContract GET to { request ->
    val caller = request.caller as Caller.Signed
    val account = database.queries.accountQueries.byId(caller.accountId.value).executeAsOne()
    Response(OK).with(
        meBody of MeResponse(
            accountId = caller.accountId.value.toString(),
            email = account.email,
            role = account.role,
            client = caller.client.value,
            scopes = caller.scopes.map { it.value }.sorted(),
        ),
    )
}
