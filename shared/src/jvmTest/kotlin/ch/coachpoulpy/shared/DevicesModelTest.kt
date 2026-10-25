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

    @Test
    fun aDevicePageChoosesAndReleasesTheSourceOfAMeasure() = runTest {
        val devices = DevicesModel(FakeCentral(), MemoryStore(), backgroundScope)
        devices.startScan()
        runCurrent()
        devices.add("AA:BB")
        devices.add("BAND")
        devices.stopScan()
        runCurrent()
        fun measures(key: String) = devices.state.value.known.single { it.key == key }.measures.associateBy { it.quantity }
        fun choice(quantity: Quantity) = devices.state.value.sources.single { it.quantity == quantity }.choiceKey

        // Only the treadmill measures speed: used, nothing to choose.
        assertEquals(MeasureUi(Quantity.SPEED, used = true, choosable = false), measures("AA:BB")[Quantity.SPEED])
        // Both measure heart rate; automatic picks the band, worn on the body.
        assertEquals(MeasureUi(Quantity.HEART_RATE, used = false, choosable = true), measures("AA:BB")[Quantity.HEART_RATE])
        assertEquals(MeasureUi(Quantity.HEART_RATE, used = true, choosable = true), measures("BAND")[Quantity.HEART_RATE])

        devices.useForMeasure("AA:BB", Quantity.HEART_RATE, on = true)
        assertEquals(true, measures("AA:BB").getValue(Quantity.HEART_RATE).used)
        assertEquals(false, measures("BAND").getValue(Quantity.HEART_RATE).used)
        assertEquals("AA:BB", choice(Quantity.HEART_RATE)) // Data sources shows the same choice
        assertEquals(listOf(Quantity.HEART_RATE), devices.state.value.known.single { it.key == "AA:BB" }.usedFor - Quantity.SPEED)

        // Released: back to automatic, so the band again.
        devices.useForMeasure("AA:BB", Quantity.HEART_RATE, on = false)
        assertEquals(null, choice(Quantity.HEART_RATE))
        assertEquals(false, measures("AA:BB").getValue(Quantity.HEART_RATE).used)
        assertEquals(true, measures("BAND").getValue(Quantity.HEART_RATE).used)

        // Released from the band, which automatic would pick anyway: the treadmill takes over.
        devices.useForMeasure("BAND", Quantity.HEART_RATE, on = false)
        assertEquals("AA:BB", choice(Quantity.HEART_RATE))
        assertEquals(false, measures("BAND").getValue(Quantity.HEART_RATE).used)
        assertEquals(true, measures("AA:BB").getValue(Quantity.HEART_RATE).used)
    }
}
