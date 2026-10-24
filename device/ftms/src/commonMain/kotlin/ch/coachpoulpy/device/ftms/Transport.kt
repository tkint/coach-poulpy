package ch.coachpoulpy.device.ftms

import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

/**
 * The Bluetooth layer the treadmill code talks to. [KableCentral] implements it on
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
)

/** One device's connection. Characteristics are those of the FTMS service. */
interface BleLink : AutoCloseable {
    /** Connects, discovering services. Throws when the device cannot be reached. */
    suspend fun connect()

    /** Suspends until the connection drops, whoever dropped it. */
    suspend fun awaitDisconnected()

    suspend fun disconnect()

    suspend fun read(characteristic: Uuid): ByteArray

    /** Notifications or indications; subscribing enables them on the device. */
    fun observe(characteristic: Uuid): Flow<ByteArray>

    suspend fun write(characteristic: Uuid, value: ByteArray)
}
