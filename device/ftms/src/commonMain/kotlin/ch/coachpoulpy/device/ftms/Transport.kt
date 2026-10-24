package ch.coachpoulpy.device.ftms

import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

/**
 * The Bluetooth layer the device code talks to. [KableCentral] implements it on
 * Android and iOS; tests use a fake. Keeping it this small means a native transport
 * can replace Kable on one platform without touching the FTMS code above it.
 */
interface BleCentral {
    /** Advertisements, repeated as they arrive; callers group them by [FoundDevice.id]. */
    fun scan(): Flow<FoundDevice>

    fun open(device: FoundDevice): BleLink
}

data class FoundDevice(
    val id: String,
    val name: String?,
    val rssi: Int,
    /** False does not rule a machine out: some only list FTMS once connected. */
    val advertisesFtms: Boolean,
    /** The transport's own handle for this device, opaque to everything else. */
    val handle: Any? = null,
    /** Every service the advertisement lists, used to tell a foot pod from a heart rate band. */
    val services: Set<Uuid> = emptySet(),
) {
    /** What the advertisement says this is, or null for a device that lists none of the services the app knows. */
    val kind: DeviceKind?
        get() = when {
            advertisesFtms || FtmsUuids.service in services -> DeviceKind.TREADMILL
            SensorUuids.runningSpeedCadence in services -> DeviceKind.FOOT_POD
            SensorUuids.heartRate in services -> DeviceKind.HEART_RATE
            else -> null
        }
}

/** The kinds of device the app can connect to, each through its standard Bluetooth profile. */
enum class DeviceKind {
    /** Fitness Machine Service (FTMS). */
    TREADMILL,

    /** Running Speed and Cadence (RSC), like the pods Zwift pairs with. */
    FOOT_POD,

    /** Heart Rate profile: chest straps and arm bands. */
    HEART_RATE,
}

/** One device's connection. Characteristics belong to [FtmsUuids.service] unless another service is given. */
interface BleLink : AutoCloseable {
    /** Connects, discovering services. Throws when the device cannot be reached. */
    suspend fun connect()

    /** Suspends until the connection drops, whoever dropped it. */
    suspend fun awaitDisconnected()

    suspend fun disconnect()

    suspend fun read(characteristic: Uuid, service: Uuid = FtmsUuids.service): ByteArray

    /** Notifications or indications; subscribing enables them on the device. */
    fun observe(characteristic: Uuid, service: Uuid = FtmsUuids.service): Flow<ByteArray>

    suspend fun write(characteristic: Uuid, value: ByteArray, service: Uuid = FtmsUuids.service)
}
