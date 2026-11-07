package ch.coachpoulpy.server.api

import ch.coachpoulpy.catalog.Catalog
import ch.coachpoulpy.server.accounts.Accounts
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.db.Database
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.contract.security.NoSecurity
import org.http4k.contract.security.Security
import org.http4k.format.KotlinxSerialization
import org.http4k.routing.RoutingHttpHandler

/** The path every API route lives under, on `api.coach-poulpy.com` (ADR 0021). */
const val API_PREFIX = "/api/v1"

/**
 * Marks a route anyone may call, signed in or not. Every route sets its `security` to this
 * or to the scopes it needs, never leaving it out ([ADR 0020]); a test checks it.
 *
 * [ADR 0020]: https://github.com/tkint/coach-poulpy/blob/main/docs/adr/0020-every-credential-resolves-to-one-caller.md
 */
val Public: Security = NoSecurity

/** Every route: those under [API_PREFIX], and OAuth's under `/oauth`. */
fun apiRoutes(catalog: Catalog, accounts: Accounts, signIns: SignIns, database: Database): List<ContractRoute> = listOf(
    catalogRoute(catalog),
    signUpRoute(accounts),
    confirmEmailRoute(accounts),
    meRoute(database),
    tokenRoute(signIns),
    revokeRoute(signIns),
)

/** The API, described in OpenAPI at `/api/v1/openapi.json`, OAuth routes included. */
fun api(version: String, catalog: Catalog, accounts: Accounts, signIns: SignIns, database: Database): RoutingHttpHandler = contract {
    // The description is built as JSON nodes: kotlinx.serialization has no serializer for
    // http4k's own description classes, which the auto-marshalling renderer would need.
    renderer = OpenApi3(
        ApiInfo("Coach Poulpy API", version),
        KotlinxSerialization,
        apiRenderer = OpenApi3ApiRenderer(KotlinxSerialization),
    )
    descriptionPath = "$API_PREFIX/openapi.json"
    routes += apiRoutes(catalog, accounts, signIns, database)
}
