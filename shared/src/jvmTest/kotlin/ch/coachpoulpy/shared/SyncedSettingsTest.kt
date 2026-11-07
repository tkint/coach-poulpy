package ch.coachpoulpy.shared

import ch.coachpoulpy.core.model.SettingChange
import ch.coachpoulpy.data.local.DriverFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncedSettingsTest {
    private val graph = AppGraph(DriverFactory())
    private val memory = MemoryStore()
    private val synced = SyncedSettings(memory, graph.store)

    private fun recordedKeys() = graph.store.outbox(100).map { it.change as SettingChange }.map { it.key to it.value }

    @Test
    fun theProfileUnitsAndLanguageAreRecordedButNotTheTheme() {
        graph.store.startRecordingChanges(SyncedSettings.values(memory))
        synced.putString("profile.name", "Sam")
        synced.putString("profile.name", "Sam") // Unchanged: nothing more to send.
        synced.putString("settings.units", "IMPERIAL")
        synced.putString("settings.theme", "DARK")
        synced.putString("profile.pain.areas", null)
        synced.putString("profile.name", null)

        assertEquals(listOf("profile.name" to "Sam", "settings.units" to "IMPERIAL", "profile.name" to null), recordedKeys())
        assertEquals("IMPERIAL", memory.getString("settings.units"))
        assertEquals("DARK", memory.getString("settings.theme"))
    }

    @Test
    fun aGuestsSettingsAddNothing() {
        synced.putString("profile.name", "Sam")
        synced.putString("settings.units", "IMPERIAL")

        assertEquals(emptyList(), recordedKeys())
        assertEquals("Sam", memory.getString("profile.name"))
    }

    @Test
    fun everyKeyListedFollowsTheAccount() {
        assertEquals(SyncedSettings.KEYS, SyncedSettings.KEYS.filter(SettingChange::follows))
    }

    @Test
    fun startingSendsTheSettingsAlreadySet() {
        memory.putString("profile.name", "Sam")
        memory.putString("settings.language", "FR")
        memory.putString("settings.theme", "DARK")

        graph.store.startRecordingChanges(SyncedSettings.values(memory))

        assertEquals(listOf("profile.name" to "Sam", "settings.language" to "FR"), recordedKeys())
    }
}
