package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.Equipment

/**
 * The equipment the user ticked in the Equipment sheet, kept on the phone between
 * launches and applied to the library list and the builder's picker alike. Null is no
 * filter; an empty set is a filter with nothing ticked, which leaves bodyweight and mat
 * exercises.
 */
class EquipmentFilter(private val memory: KeyValueStore) {
    var ticked: Set<Equipment>?
        get() = memory.getString(KEY)?.let { saved ->
            saved.split(',').mapNotNull { name -> Equipment.entries.firstOrNull { it.name == name } }.toSet()
        }
        set(value) = memory.putString(KEY, value?.sortedBy { it.ordinal }?.joinToString(",") { it.name })

    private companion object {
        const val KEY = "exercises.equipment"
    }
}
