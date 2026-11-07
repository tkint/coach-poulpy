package ch.coachpoulpy.server.api

import ch.coachpoulpy.catalog.Catalog
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.Query
import org.http4k.lens.int

private val newerThan = Query.int().optional("newerThan", "The catalog version the phone already has")

/**
 * The catalog the server was built with, the same document the app bundles. A guest's
 * phone asks for it on launch (#27), the only call a guest makes (ADR 0001, ADR 0008).
 */
fun catalogRoute(catalog: Catalog): ContractRoute {
    val document = catalog.toJson()
    return "/catalog" meta {
        summary = "The latest exercise catalog"
        description = "Answers 204 when the version given in newerThan is already this one or newer, so a phone downloads the catalog only when it has changed."
        operationId = "getCatalog"
        tags += Tag("catalog")
        security = Public
        queries += newerThan
        produces += APPLICATION_JSON
        returning(OK to "The catalog: its version, exercises and variation groups")
        returning(NO_CONTENT to "The phone's catalog is already this version or newer")
    } bindContract GET to { request ->
        val known = newerThan(request)
        if (known != null && known >= catalog.version) {
            Response(NO_CONTENT)
        } else {
            Response(OK).with(Header.CONTENT_TYPE of APPLICATION_JSON).body(document)
        }
    }
}
