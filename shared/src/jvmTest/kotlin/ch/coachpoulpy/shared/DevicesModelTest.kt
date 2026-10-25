package ch.coachpoulpy.shared

import ch.coachpoulpy.device.ftms.FoundDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class DevicesModelTest {
    @Test
    fun foundDevicesKeepTheirPlaceWhileTheScanRuns() = runTest {
        val central = FakeCentral()
        val advertisements = Channel<FoundDevice>(Channel.UNLIMITED)
        central.advertisements = advertisements.receiveAsFlow()
        val devices = DevicesModel(central, MemoryStore(), backgroundScope)
        devices.startScan()

        advertisements.send(central.band.device.copy(rssi = -80))
        advertisements.send(central.treadmill.device.copy(rssi = -70))
        runCurrent()
        assertEquals(listOf("BAND", "AA:BB"), devices.state.value.found.map { it.key })

        // The band comes closer: its signal updates in place, the row stays where it is.
        advertisements.send(central.band.device.copy(rssi = -40, name = "Band, renamed"))
        runCurrent()
        assertEquals(listOf("BAND", "AA:BB"), devices.state.value.found.map { it.key })
        assertEquals(-40, devices.state.value.found.first().rssi)
        assertEquals("Band, renamed", devices.state.value.found.first().name)

        // A device found later goes below the ones already shown, however strong its signal.
        advertisements.send(central.pod.device.copy(rssi = -30))
        runCurrent()
        assertEquals(listOf("BAND", "AA:BB", "POD"), devices.state.value.found.map { it.key })
    }

    @Test
    fun aNewScanMayOrderTheListAfresh() = runTest {
        val central = FakeCentral()
        val advertisements = Channel<FoundDevice>(Channel.UNLIMITED)
        central.advertisements = advertisements.receiveAsFlow()
        val devices = DevicesModel(central, MemoryStore(), backgroundScope)
        devices.startScan()
        advertisements.send(FoundDevice("TV", "Living room TV", -30, false))
        advertisements.send(central.treadmill.device.copy(rssi = -70))
        advertisements.send(central.band.device.copy(rssi = -50))
        runCurrent()
        assertEquals(listOf("TV", "AA:BB", "BAND"), devices.state.value.found.map { it.key })

        devices.stopScan()
        devices.startScan()
        runCurrent()
        assertEquals(listOf("BAND", "AA:BB", "TV"), devices.state.value.found.map { it.key })
    }
}
