package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.TestServer
import ch.coachpoulpy.server.sync.Sync
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
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
        val undeclared = apiRoutes(catalog, test.accounts, test.signIns, test.passwordResets, test.accountDeletion, Sync(test.database), test.database).filter { it.meta.security == null }
        assertEquals(emptyList(), undeclared.map { it.toString() }, "Routes with no security: set Public or the scopes they need")
    }

    @Test
    fun theOpenApiDescriptionListsTheRoutes() {
        val response = server(Request(GET, "/api/v1/openapi.json"))
        assertEquals(OK, response.status)
        val paths = Json.parseToJsonElement(response.bodyString()).jsonObject.getValue("paths").jsonObject
        for (path in listOf("/api/v1/catalog/exercises", "/api/v1/accounts", "/api/v1/accounts/confirm", "/api/v1/me", "/oauth/token", "/oauth/revoke")) {
            assertTrue(path in paths, "paths were ${paths.keys}")
        }
    }

    @Test
    fun theCatalogNeedsNoAccount() {
        val response = server(Request(GET, "/api/v1/catalog/exercises"))
        assertEquals(OK, response.status)
        val body = Json.parseToJsonElement(response.bodyString()).jsonObject
        assertEquals(catalog.exercises.size, body.getValue("exercises").jsonArray.size)
        assertEquals(catalog.exercises.size.toString(), body.getValue("cursor").jsonPrimitive.content)
    }

    @Test
    fun aPhoneThatHasTheLatestCursorGetsNoContent() {
        val cursor = Json.parseToJsonElement(server(Request(GET, "/api/v1/catalog/exercises")).bodyString()).jsonObject.getValue("cursor").jsonPrimitive.content
        assertEquals(NO_CONTENT, server(Request(GET, "/api/v1/catalog/exercises?after=$cursor")).status)
    }

    @Test
    fun aCursorThatIsNotOneIsRefused() {
        assertEquals(BAD_REQUEST, server(Request(GET, "/api/v1/catalog/exercises?after=latest")).status)
    }

    @Test
    fun anUnknownTokenIsRefusedEvenOnAPublicRoute() {
        val response = server(Request(GET, "/api/v1/catalog/exercises").header("Authorization", "Bearer cp_at_anything"))
        assertEquals(UNAUTHORIZED, response.status)
    }
}
