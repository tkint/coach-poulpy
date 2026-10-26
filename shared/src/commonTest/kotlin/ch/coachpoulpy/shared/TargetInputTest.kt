package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TargetInputTest {
    @Test
    fun durationsReadMinutesOrClockTimes() {
        assertEquals(20 * 60, TargetInput.parseDuration("20"))
        assertEquals(5 * 60 + 30, TargetInput.parseDuration(" 5:30 "))
        assertEquals(3900, TargetInput.parseDuration("1:05:00"))
        assertNull(TargetInput.parseDuration("5:75"))
        assertNull(TargetInput.parseDuration("0"))
        assertNull(TargetInput.parseDuration("ten"))
        assertEquals("20:00", TargetInput.formatDuration(1200))
        assertEquals("1:05:00", TargetInput.formatDuration(3900))
    }

    @Test
    fun distancesReadEitherDecimalMark() {
        assertEquals(10_500.0, TargetInput.parseDistance("10,5", DistanceUnit.KM))
        assertEquals(1609.344, TargetInput.parseDistance("1", DistanceUnit.MI))
        assertEquals(400.0, TargetInput.parseDistance("400", DistanceUnit.M))
        assertNull(TargetInput.parseDistance("-1", DistanceUnit.KM))
        assertEquals("5.0 km", TargetInput.distanceLabel(5000.0, DistanceUnit.KM))
        assertEquals("21.1 km", TargetInput.distanceLabel(21_097.5, DistanceUnit.KM))
        assertEquals("3.11 mi", TargetInput.distanceLabel(5000.0, DistanceUnit.MI))
        assertEquals("400 m", TargetInput.distanceLabel(400.0, DistanceUnit.M))
    }

    @Test
    fun pacesAreTimePerUnitOrSpeed() {
        assertEquals(10.0, TargetInput.parsePace("6:00", PaceUnit.PER_KM)!!, 0.0001)
        assertEquals(12.0, TargetInput.parsePace("5", PaceUnit.PER_KM)!!, 0.0001)
        assertEquals(10.5, TargetInput.parsePace("10,5", PaceUnit.KMH)!!, 0.0001)
        assertEquals(9.656, TargetInput.parsePace("6", PaceUnit.MPH)!!, 0.001)
        assertNull(TargetInput.parsePace("6:60", PaceUnit.PER_KM))
        assertEquals("6:00 /km", TargetInput.paceLabel(10.0, PaceUnit.PER_KM))
        assertEquals("10.0 km/h", TargetInput.paceLabel(10.0, PaceUnit.KMH))
        assertEquals("9:39 /mi", TargetInput.paceLabel(10.0, PaceUnit.PER_MI))
    }
}
