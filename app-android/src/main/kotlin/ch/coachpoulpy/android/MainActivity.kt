package ch.coachpoulpy.android

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.ui.AppPreferences
import ch.coachpoulpy.android.ui.CoachPoulpyRoot
import ch.coachpoulpy.android.ui.CoachPoulpyTheme
import ch.coachpoulpy.android.ui.LocalUnitSystem
import ch.coachpoulpy.shared.LanguageChoice
import ch.coachpoulpy.shared.ThemeChoice
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var startLanguage: LanguageChoice? = null

    // The language picked in Settings wins over the phone's. "System" leaves the phone's,
    // including the per-app language Android 13 offers in its own settings.
    override fun attachBaseContext(newBase: Context) {
        val code = savedLanguage(newBase).code
        if (code == null) {
            super.attachBaseContext(newBase)
            return
        }
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(code))
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as CoachPoulpyApp).graph
        val settings = graph.settings(AppPreferences(this))
        startLanguage = settings.state.value.language
        setContent {
            val state by settings.state.collectAsStateWithLifecycle()
            // A new language needs the screens rebuilt with the other strings.
            LaunchedEffect(state.language) { if (state.language != startLanguage) recreate() }
            val dark = when (state.theme) {
                ThemeChoice.SYSTEM -> isSystemInDarkTheme()
                ThemeChoice.LIGHT -> false
                ThemeChoice.DARK -> true
            }
            CoachPoulpyTheme(dark = dark) {
                CompositionLocalProvider(LocalUnitSystem provides state.units) {
                    CoachPoulpyRoot(graph)
                }
            }
        }
    }

    private fun savedLanguage(context: Context): LanguageChoice {
        val saved = context.getSharedPreferences(AppPreferences.FILE, Context.MODE_PRIVATE).getString(LANGUAGE_KEY, null)
        return LanguageChoice.entries.firstOrNull { it.name == saved } ?: LanguageChoice.SYSTEM
    }

    private companion object {
        /** The key SettingsModel saves the language under. */
        const val LANGUAGE_KEY = "settings.language"
    }
}
