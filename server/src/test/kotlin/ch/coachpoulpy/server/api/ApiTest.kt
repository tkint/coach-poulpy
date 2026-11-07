package ch.coachpoulpy.server.api

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.TestServer
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
    private val test = TestServer()
    private val catalog = test.catalog
    private val server = test.http

    @Test
    fun everyRouteIsPublicOrDeclaresItsScopes() {
        val undeclared = apiRoutes(catalog, test.accounts, test.signIns, test.database).filter { it.meta.security == null }
        assertEquals(emptyList(), undeclared.map { it.toString() }, "Routes with no security: set Public or the scopes they need")
    }

    @Test
    fun theOpenApiDescriptionListsTheRoutes() {
        val response = server(Request(GET, "/api/v1/openapi.json"))
        assertEquals(OK, response.status)
        val paths = Json.parseToJsonElement(response.bodyString()).jsonObject.getValue("paths").jsonObject
        for (path in listOf("/api/v1/catalog", "/api/v1/accounts", "/api/v1/accounts/confirm", "/api/v1/sign-ins", "/api/v1/sign-ins/refresh", "/api/v1/sign-ins/current", "/api/v1/me")) {
            assertTrue(path in paths, "paths were ${paths.keys}")
        }
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
    fun anUnknownTokenIsRefusedEvenOnAPublicRoute() {
        val response = server(Request(GET, "/api/v1/catalog").header("Authorization", "Bearer cp_at_anything"))
        assertEquals(UNAUTHORIZED, response.status)
    }
}
