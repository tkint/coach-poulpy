package ch.coachpoulpy.server.api

import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

/** `up` or `down`, and nothing else: the public status never tells how the server is configured (#340). */
@Serializable
data class StatusResponse(val status: String)

private val statusBody = Body.auto<StatusResponse>().toLens()

/**
 * The public status the web app's `/status` page shows: up while the database answers, down
 * otherwise. Whether mail is configured, the version and every other detail are the admin's
 * (`/api/v1/admin/status`).
 */
fun publicStatusRoute(databaseAnswers: () -> Boolean): ContractRoute = "$API_PREFIX/status" meta {
    summary = "Whether Coach Poulpy is up"
    description = "Up or down, nothing more: no version, no configuration. A server that can't be reached is down too."
    operationId = "getStatus"
    tags += Tag("status")
    security = Public
    returning(OK, statusBody to StatusResponse("up"), "Up", definitionId = "Status")
    returning(SERVICE_UNAVAILABLE, statusBody to StatusResponse("down"), "Down: the database doesn't answer", definitionId = "Status")
} bindContract GET to { _ ->
    if (databaseAnswers()) Response(OK).with(statusBody of StatusResponse("up")) else Response(SERVICE_UNAVAILABLE).with(statusBody of StatusResponse("down"))
}
