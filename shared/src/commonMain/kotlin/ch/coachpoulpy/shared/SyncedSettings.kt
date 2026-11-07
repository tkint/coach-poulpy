package ch.coachpoulpy.shared

import ch.coachpoulpy.data.local.LocalStore

/**
 * The phone's small values, adding each change to a setting that follows the account (the profile,
 * units and language) to the outbox while the phone records changes (docs/accounts.md, Sync).
 * Theme, app icon and paired devices stay on the phone.
 */
internal class SyncedSettings(private val memory: KeyValueStore, private val store: LocalStore) : KeyValueStore {
    override fun getString(key: String): String? = memory.getString(key)

    override fun putString(key: String, value: String?) {
        if (memory.getString(key) == value) return
        memory.putString(key, value)
        store.recordSetting(key, value)
    }

    companion object {
        /** Every key that follows the account, for filling the outbox with the whole phone. */
        val KEYS = listOf(
            ProfileModel.NAME, ProfileModel.BIRTHDATE, ProfileModel.PRONOUNS, ProfileModel.CUSTOM_PRONOUNS,
            ProfileModel.SEX, ProfileModel.GOALS, ProfileModel.EXPERIENCE, ProfileModel.RESTING_HEART_RATE,
            ProfileModel.BODY_FIGURE, ProfileModel.PAIN_AREAS, ProfileModel.PAIN_SINCE, ProfileModel.PAIN_DURATION,
            SettingsModel.UNITS, SettingsModel.LANGUAGE,
        )

        /** The values set for [KEYS] in [memory]. */
        fun values(memory: KeyValueStore): Map<String, String> =
            KEYS.mapNotNull { key -> memory.getString(key)?.let { key to it } }.toMap()
    }
}
