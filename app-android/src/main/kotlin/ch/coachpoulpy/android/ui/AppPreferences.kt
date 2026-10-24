package ch.coachpoulpy.android.ui

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import ch.coachpoulpy.shared.KeyValueStore
import ch.coachpoulpy.shared.UnitSystem

/** The units picked in Settings, for screens that format distances and speeds. */
val LocalUnitSystem = staticCompositionLocalOf { UnitSystem.METRIC }

/**
 * The app's small values between launches: settings, added devices, data sources. The file
 * keeps the name it had when it only held the treadmill, so that treadmill carries over.
 */
class AppPreferences(context: Context) : KeyValueStore {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()

    companion object {
        const val FILE = "treadmill"
    }
}
