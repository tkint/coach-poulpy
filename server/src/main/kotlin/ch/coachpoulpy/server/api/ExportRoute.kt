package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.accounts.AccountExport
import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.Scope
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import java.time.Clock
import java.time.LocalDate
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK

/** Export my data: a .zip of the account, as JSON and each finished session as a .FIT file. */
fun exportRoute(export: AccountExport, clock: Clock = Clock.systemUTC()): ContractRoute = "$API_PREFIX/me/export" meta {
    summary = "Export your data"
    description = "One .zip: ${AccountExport.JSON_NAME} with every change the account stores (workouts, plans, sessions, " +
        "profile, body log, units and language), as sync pulls them, and sessions/ with each finished session as a .FIT file."
    operationId = "exportMe"
    tags += Tag("accounts")
    security = requires(Scope.Sync)
    returning(OK to "The .zip")
} bindContract GET to { request ->
    val caller = request.caller as Caller.Signed
    Response(OK)
        .header("Content-Type", "application/zip")
        .header("Content-Disposition", "attachment; filename=\"coach-poulpy-${LocalDate.now(clock)}.zip\"")
        .body(export.zip(caller.accountId.value).inputStream())
}
