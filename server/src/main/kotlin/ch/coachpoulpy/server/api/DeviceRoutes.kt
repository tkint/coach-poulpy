package ch.coachpoulpy.server.api

import ch.coachpoulpy.server.auth.Caller
import ch.coachpoulpy.server.auth.SignIns
import ch.coachpoulpy.server.auth.caller
import ch.coachpoulpy.server.auth.requires
import kotlinx.serialization.Serializable
import org.http4k.contract.ContractRoute
import org.http4k.contract.Tag
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Path
import org.http4k.lens.uuid

@Serializable
data class DeviceResponse(
    val id: String,
    val name: String,
    val client: String,
    val signedInAt: String,
    val lastUsedAt: String,
    /** The device that sent the request. */
    val current: Boolean,
)

@Serializable
data class DevicesResponse(val devices: List<DeviceResponse>)

private val devicesBody = Body.auto<DevicesResponse>().toLens()
private val deviceProblemBody = Body.auto<Problem>().toLens()
private val deviceId = Path.uuid().of("id", "The device's id, from the list of devices")
private val devicesTag = Tag("accounts")

/** The devices signed in to the caller's account, the caller's own marked current, most recently used first. */
fun devicesRoute(signIns: SignIns): ContractRoute = "$API_PREFIX/me/devices" meta {
    summary = "Your signed-in devices"
    description = "Every phone and browser signed in to the account, most recently used first. The one sending the request " +
        "has current set. Signing a device out keeps the training on it, as a guest."
    operationId = "listDevices"
    tags += devicesTag
    security = requires()
    returning(
        OK,
        devicesBody to DevicesResponse(
            listOf(
                DeviceResponse("0190d4d2-0000-7000-8000-000000000002", "Pixel 8", "app", "2026-10-01T08:00:00Z", "2026-11-22T21:40:00Z", current = true),
                DeviceResponse("0190d4d2-0000-7000-8000-000000000003", "Ana's iPhone", "app", "2026-09-12T18:00:00Z", "2026-11-19T07:15:00Z", current = false),
            ),
        ),
        "The devices",
        definitionId = "Devices",
    )
} bindContract GET to { request ->
    val caller = request.caller as Caller.Signed
    val devices = signIns.devices(caller.accountId.value).map {
        DeviceResponse(it.id.toString(), it.name, it.client.value, it.signedInAt.toInstant().toString(), it.lastUsedAt.toInstant().toString(), it.id == caller.signInId)
    }
    Response(OK).with(devicesBody of DevicesResponse(devices))
}

/** Signs one of the caller's devices out. */
fun signOutDeviceRoute(signIns: SignIns): ContractRoute = "$API_PREFIX/me/devices" / deviceId meta {
    summary = "Sign a device out"
    description = "Every token of that device stops working; it keeps its training as a guest. The caller's own device can be " +
        "signed out this way too, which is the same as revoking its token."
    operationId = "signOutDevice"
    tags += devicesTag
    security = requires()
    returning(NO_CONTENT to "The device is signed out")
    returning(NOT_FOUND, deviceProblemBody to Problem("unknown-device"), "No such device on this account, or it is already signed out", definitionId = "Problem")
} bindContract DELETE to { id ->
    { request ->
        val caller = request.caller as Caller.Signed
        if (signIns.signOut(caller.accountId.value, id)) Response(NO_CONTENT)
        else Response(NOT_FOUND).with(deviceProblemBody of Problem("unknown-device"))
    }
}

/** Signs every device of the caller's account out but the caller's own. */
fun signOutOtherDevicesRoute(signIns: SignIns): ContractRoute = "$API_PREFIX/me/devices" meta {
    summary = "Sign every other device out"
    description = "Every device of the account but the one sending the request is signed out. Only a device signed in with " +
        "the account's password may, since it must stay signed in."
    operationId = "signOutOtherDevices"
    tags += devicesTag
    security = requires()
    returning(NO_CONTENT to "Every other device is signed out")
    returning(FORBIDDEN, deviceProblemBody to Problem("first-party-only"), "first-party-only: the credential is not a device's sign-in", definitionId = "Problem")
} bindContract DELETE to { request ->
    val caller = request.caller as Caller.Signed
    val own = caller.signInId
    if (own == null) {
        Response(FORBIDDEN).with(deviceProblemBody of Problem("first-party-only"))
    } else {
        signIns.signOutOthers(caller.accountId.value, own)
        Response(NO_CONTENT)
    }
}
