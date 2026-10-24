package ch.coachpoulpy.android.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalPoulpyExtraColors = staticCompositionLocalOf { PoulpyLightExtras }

// Brand colours always win over the wallpaper-based dynamic colours, so Poulpy looks the same on every phone.
// The palette, type scale and spacing come from PoulpyTokens.kt, generated from design/tokens.json.
@Composable
fun CoachPoulpyTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalPoulpyExtraColors provides if (dark) PoulpyDarkExtras else PoulpyLightExtras) {
        MaterialTheme(
            colorScheme = if (dark) PoulpyDarkColors else PoulpyLightColors,
            typography = PoulpyTypography,
            shapes = PoulpyShapes,
            content = content,
        )
    }
}

object PoulpyTheme {
    /** Grumpy (ember) and success colours, which Material 3 has no slot for. */
    val extraColors: PoulpyExtraColors
        @Composable @ReadOnlyComposable
        get() = LocalPoulpyExtraColors.current
}
