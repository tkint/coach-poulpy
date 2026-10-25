package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Equipment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EquipmentFilterTest {
    @Test
    fun theTickedEquipmentSurvivesARestart() {
        val memory = MemoryStore()
        assertNull(EquipmentFilter(memory).ticked)
        EquipmentFilter(memory).ticked = setOf(Equipment.BENCH, Equipment.DUMBBELL)
        assertEquals(setOf(Equipment.DUMBBELL, Equipment.BENCH), EquipmentFilter(memory).ticked)
    }

    @Test
    fun tickingNothingOrClearingRemovesIt() {
        val memory = MemoryStore()
        EquipmentFilter(memory).ticked = setOf(Equipment.BAND)
        EquipmentFilter(memory).ticked = emptySet()
        assertNull(EquipmentFilter(memory).ticked)
        EquipmentFilter(memory).ticked = setOf(Equipment.BAND)
        EquipmentFilter(memory).ticked = null
        assertNull(EquipmentFilter(memory).ticked)
    }
}
