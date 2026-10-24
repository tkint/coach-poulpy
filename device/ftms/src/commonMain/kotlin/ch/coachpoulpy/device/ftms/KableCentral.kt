package ch.coachpoulpy.device.ftms

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [BleCentral] over the Kable library: Android's Bluetooth stack on Android,
 * CoreBluetooth on iOS. On Android the caller must hold the scan and connect
 * permissions first; Kable throws otherwise.
 */
class KableCentral : BleCentral {
    // No service filter: several treadmills only list the FTMS service once connected,
    // so filtering would hide them. The list puts the ones that do advertise it first.
    override fun scan(): Flow<FoundDevice> = Scanner().advertisements.map { ad ->
        FoundDevice(
            id = ad.identifier.toString(),
            name = ad.name ?: ad.peripheralName,
            rssi = ad.rssi,
            advertisesFtms = FtmsUuids.service in ad.uuids,
            handle = ad,
        )
    }

    override fun open(device: FoundDevice): BleLink {
        val advertisement = device.handle as? Advertisement
            ?: error("Device ${device.id} was not found by this central's scan")
        return KableLink(Peripheral(advertisement))
    }
}

private class KableLink(private val peripheral: Peripheral) : BleLink {
    override suspend fun connect() {
        peripheral.connect()
    }

    override suspend fun awaitDisconnected() {
        peripheral.state.first { it is State.Disconnected }
    }

    override suspend fun disconnect() = peripheral.disconnect()

    override suspend fun read(characteristic: Uuid): ByteArray = peripheral.read(ftms(characteristic))

    override fun observe(characteristic: Uuid): Flow<ByteArray> = peripheral.observe(ftms(characteristic))

    override suspend fun write(characteristic: Uuid, value: ByteArray) =
        peripheral.write(ftms(characteristic), value, WriteType.WithResponse)

    override fun close() = peripheral.close()

    private fun ftms(characteristic: Uuid) = characteristicOf(FtmsUuids.service, characteristic)
}
