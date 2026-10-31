package ch.coachpoulpy.android

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

    /** Set when the recording notification opened the app, until the Live tab shows. */
    private val openLive = mutableStateOf(false)

    /** The text of a workout file another app opened here, until the preview shows. Empty when it couldn't be read. */
    private val importFile = mutableStateOf<String?>(null)

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
        openLive.value = intent.getBooleanExtra(OPEN_LIVE, false)
        // Only on a fresh start: a rotation or language change recreates the activity with the same intent.
        if (savedInstanceState == null) readWorkoutFile(intent)
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
                    CoachPoulpyRoot(
                        graph,
                        openLive = openLive.value,
                        onLiveOpened = { openLive.value = false },
                        importFile = importFile.value,
                        onImportOpened = { importFile.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(OPEN_LIVE, false)) openLive.value = true
        readWorkoutFile(intent)
    }

    /** Reads a shared or opened `.poulpyworkout` into [importFile]; a file too big or unreadable becomes "", which the preview refuses. */
    private fun readWorkoutFile(intent: Intent) {
        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            else -> null
        } ?: return
        importFile.value = try {
            contentResolver.openInputStream(uri)?.use { input ->
                // One byte over the limit tells a file that is too big from one that just fits.
                val bytes = ByteArray(MAX_FILE_BYTES + 1)
                var length = 0
                while (length < bytes.size) {
                    val read = input.read(bytes, length, bytes.size - length)
                    if (read < 0) break
                    length += read
                }
                if (length > MAX_FILE_BYTES) "" else String(bytes, 0, length, Charsets.UTF_8)
            } ?: ""
        } catch (_: java.io.IOException) {
            ""
        } catch (_: SecurityException) {
            ""
        }
    }

    private fun savedLanguage(context: Context): LanguageChoice {
        val saved = context.getSharedPreferences(AppPreferences.FILE, Context.MODE_PRIVATE).getString(LANGUAGE_KEY, null)
        return LanguageChoice.entries.firstOrNull { it.name == saved } ?: LanguageChoice.SYSTEM
    }

    companion object {
        /** An extra that opens the Live tab, from the recording notification. */
        const val OPEN_LIVE = "openLive"

        /** A workout file is a few kilobytes; anything past this is not one. */
        private const val MAX_FILE_BYTES = 1_000_000

        /** The key SettingsModel saves the language under. */
        private const val LANGUAGE_KEY = "settings.language"
    }
}
