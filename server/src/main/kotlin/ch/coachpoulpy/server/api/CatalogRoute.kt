package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.catalog.ServerCatalog
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SERVICE_UNAVAILABLE
import org.http4k.core.with
import org.http4k.lens.Header
import org.http4k.lens.Query
import org.http4k.lens.int

private val newerThan = Query.int().optional("newerThan", "The catalog version the phone already has")

/**
 * The latest published catalog version (#337, ADR 0026), in the format the app bundles. A guest's
 * phone asks for it on launch (#27), the only call a guest makes (ADR 0001, ADR 0008).
 */
fun catalogRoute(catalog: ServerCatalog): ContractRoute = "$API_PREFIX/catalog" meta {
    summary = "The latest exercise catalog"
    description = "The latest published catalog version. Answers 204 when the version given in newerThan is already this one or newer, " +
        "so a phone downloads the catalog only when it has changed."
    operationId = "getCatalog"
    tags += Tag("catalog")
    security = Public
    queries += newerThan
    produces += APPLICATION_JSON
    returning(OK to "The catalog: its version, exercises and variation groups")
    returning(NO_CONTENT to "The phone's catalog is already this version or newer")
} bindContract GET to { request ->
    val known = newerThan(request)
    val latest = catalog.latestVersion()
    when {
        // Only before the server's first start has seeded it, which happens before it listens.
        latest == null -> Response(SERVICE_UNAVAILABLE)
        known != null && known >= latest -> Response(NO_CONTENT)
        else -> catalog.latestDocument()?.let { (_, document) ->
            Response(OK).with(Header.CONTENT_TYPE of APPLICATION_JSON).body(document)
        } ?: Response(SERVICE_UNAVAILABLE)
    }
}
