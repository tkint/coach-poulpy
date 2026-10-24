package ch.coachpoulpy.device.ftms

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CapabilitiesAndControlTest {
    @Test
    fun featuresListMetricsAndTargets() {
        val machine = (1 shl 2) or (1 shl 3) or (1 shl 10) or (1 shl 12)
        val targets = (1 shl 0) or (1 shl 1)
        val (metrics, settings) = CapabilitiesDecoder.features(packet(machine to 4, targets to 4))
        assertEquals(
            setOf(MachineMetric.TOTAL_DISTANCE, MachineMetric.INCLINATION, MachineMetric.HEART_RATE, MachineMetric.ELAPSED_TIME),
            metrics,
        )
        assertEquals(setOf(TargetSetting.SPEED, TargetSetting.INCLINATION), settings)
    }

    @Test
    fun rangesUseTheirResolutions() {
        assertEquals(SupportedRange(1.0, 16.0, 0.1), CapabilitiesDecoder.speedRange(packet(100 to 2, 1600 to 2, 10 to 2)))
        assertEquals(SupportedRange(-3.0, 15.0, 0.5), CapabilitiesDecoder.inclineRange(packet(-30 to 2, 150 to 2, 5 to 2)))
    }

    @Test
    fun controlCommandsEncode() {
        assertContentEquals(packet(0x02 to 1, 1050 to 2), ControlPoint.setTargetSpeed(10.5))
        assertContentEquals(packet(0x03 to 1, -15 to 2), ControlPoint.setTargetIncline(-1.5))
        assertContentEquals(byteArrayOf(0x00), ControlPoint.requestControl())
    }

    @Test
    fun controlResponsesDecode() {
        assertEquals(0x02 to ControlResult.SUCCESS, ControlPoint.response(packet(0x80 to 1, 0x02 to 1, 0x01 to 1)))
        assertEquals(0x00 to ControlResult.CONTROL_NOT_PERMITTED, ControlPoint.response(packet(0x80 to 1, 0x00 to 1, 0x05 to 1)))
        assertNull(ControlPoint.response(byteArrayOf(0x01)))
    }
}
