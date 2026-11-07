package ch.coachpoulpy.server.api

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.app
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiTest {
    private val catalog = Catalog.bundled
    private val server = app("0.7.0", catalog)

    @Test
    fun everyRouteIsPublicOrDeclaresItsScopes() {
        val undeclared = apiRoutes(catalog).filter { it.meta.security == null }
        assertEquals(emptyList(), undeclared.map { it.toString() }, "Routes with no security: set Public or the scopes they need")
    }

    @Test
    fun theOpenApiDescriptionListsTheRoutes() {
        val response = server(Request(GET, "/api/v1/openapi.json"))
        assertEquals(OK, response.status)
        val paths = Json.parseToJsonElement(response.bodyString()).jsonObject.getValue("paths").jsonObject
        assertTrue("/api/v1/catalog" in paths, "paths were ${paths.keys}")
    }

    @Test
    fun theCatalogNeedsNoAccount() {
        val response = server(Request(GET, "/api/v1/catalog"))
        assertEquals(OK, response.status)
        assertEquals(catalog.version, Catalog.fromJson(response.bodyString()).version)
        assertEquals(catalog.exercises.size, Catalog.fromJson(response.bodyString()).exercises.size)
    }

    @Test
    fun theCatalogIsSentToAnOlderPhone() {
        val response = server(Request(GET, "/api/v1/catalog?newerThan=${catalog.version - 1}"))
        assertEquals(OK, response.status)
    }

    @Test
    fun aPhoneWithThisVersionOrNewerGetsNoContent() {
        assertEquals(NO_CONTENT, server(Request(GET, "/api/v1/catalog?newerThan=${catalog.version}")).status)
        assertEquals(NO_CONTENT, server(Request(GET, "/api/v1/catalog?newerThan=${catalog.version + 1}")).status)
    }

    @Test
    fun aTokenIsRefusedUntilAccountsExist() {
        val response = server(Request(GET, "/api/v1/catalog").header("Authorization", "Bearer cp_at_anything"))
        assertEquals(UNAUTHORIZED, response.status)
    }
}
