// Generated from design/tokens.json by design/build-tokens.mjs. Do not edit by hand.
package ch.coachpoulpy.android.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ch.coachpoulpy.android.R

/** Fixed palette. The poulpy colours are the mascot itself and never change with the theme. */
object PoulpyPalette {
    val PoulpyBlue = Color(0xFF3AA6E0)
    val PoulpyEmber = Color(0xFFD9622B)
    val PoulpyInk = Color(0xFF141A26)
    val Lagoon50 = Color(0xFFEAF6FD)
    val Lagoon100 = Color(0xFFCDEBFA)
    val Lagoon300 = Color(0xFF7FCBF0)
    val Lagoon400 = Color(0xFF3AA6E0)
    val Lagoon600 = Color(0xFF1574B0)
    val Lagoon800 = Color(0xFF0C4A73)
    val Lagoon900 = Color(0xFF082F4A)
    val Ember50 = Color(0xFFFFF1E8)
    val Ember100 = Color(0xFFFFDCC7)
    val Ember300 = Color(0xFFF4A473)
    val Ember400 = Color(0xFFE8873F)
    val Ember500 = Color(0xFFD9622B)
    val Ember600 = Color(0xFFB4471C)
    val Ember800 = Color(0xFF7A2C12)
    val Neutral0 = Color(0xFFFFFFFF)
    val Neutral50 = Color(0xFFF4F9FC)
    val Neutral100 = Color(0xFFE6EFF5)
    val Neutral300 = Color(0xFFB6C4D0)
    val Neutral500 = Color(0xFF6B7C8C)
    val Neutral700 = Color(0xFF374555)
    val Neutral900 = Color(0xFF141A26)
}

/** Poulpy's mood shades, from the blue side to the ember side. */
enum class PoulpyMood(val tone: Color) {
    Proud(Color(0xFF2C9CE0)),
    Happy(Color(0xFF3AA6E0)),
    Recovering(Color(0xFF5DB4E6)),
    Annoyed(Color(0xFFE8873F)),
    Grumpy(Color(0xFFD9622B)),
    Fuming(Color(0xFFC0352B)),
}

internal val PoulpyLightColors = lightColorScheme(
    primary = Color(0xFF1574B0), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDF0FB), onPrimaryContainer = Color(0xFF082F4A),
    secondary = Color(0xFF1574B0), onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB4471C), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE6D6), onTertiaryContainer = Color(0xFF4A1A0B),
    background = Color(0xFFF4F9FC), onBackground = Color(0xFF141A26),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF141A26),
    surfaceVariant = Color(0xFFE6EFF5), onSurfaceVariant = Color(0xFF56687A),
    outline = Color(0xFFD3E0E9), outlineVariant = Color(0xFFD3E0E9),
    error = Color(0xFFC8264A), onError = Color(0xFFFFFFFF),
)

internal val PoulpyDarkColors = darkColorScheme(
    primary = Color(0xFF5BBEF0), onPrimary = Color(0xFF06202F),
    primaryContainer = Color(0xFF123A52), onPrimaryContainer = Color(0xFFCDEBFA),
    secondary = Color(0xFF5BBEF0), onSecondary = Color(0xFF06202F),
    tertiary = Color(0xFFFF9A66), onTertiary = Color(0xFF2A1006),
    tertiaryContainer = Color(0xFF3A1E12), onTertiaryContainer = Color(0xFFFFDCC7),
    background = Color(0xFF0A1520), onBackground = Color(0xFFE8F1F7),
    surface = Color(0xFF112230), onSurface = Color(0xFFE8F1F7),
    surfaceVariant = Color(0xFF17303F), onSurfaceVariant = Color(0xFF95AABB),
    outline = Color(0xFF21394A), outlineVariant = Color(0xFF21394A),
    error = Color(0xFFFF6B85), onError = Color(0xFF2A0610),
)

/** Brand colours that Material 3 has no slot for. */
data class PoulpyExtraColors(
    val grumpy: Color,
    val onGrumpy: Color,
    val grumpyContainer: Color,
    val onGrumpyContainer: Color,
    val success: Color,
)

internal val PoulpyLightExtras = PoulpyExtraColors(Color(0xFFB4471C), Color(0xFFFFFFFF), Color(0xFFFFE6D6), Color(0xFF4A1A0B), Color(0xFF2E9E6A))
internal val PoulpyDarkExtras = PoulpyExtraColors(Color(0xFFFF9A66), Color(0xFF2A1006), Color(0xFF3A1E12), Color(0xFFFFDCC7), Color(0xFF4CC38A))

/** Baloo 2 (display) and Nunito (body), bundled in res/font. */
object PoulpyFonts {
    val Display = FontFamily(Font(R.font.baloo2_bold, FontWeight.W700), Font(R.font.baloo2_extrabold, FontWeight.W800))
    val Body = FontFamily(Font(R.font.nunito_regular, FontWeight.W400), Font(R.font.nunito_medium, FontWeight.W500), Font(R.font.nunito_bold, FontWeight.W700), Font(R.font.nunito_extrabold, FontWeight.W800))
}

/** Type scale. */
object PoulpyTextStyles {
    val Display = TextStyle(fontFamily = PoulpyFonts.Display, fontWeight = FontWeight.W800, fontSize = 40.sp, lineHeight = 44.sp)
    val Title = TextStyle(fontFamily = PoulpyFonts.Display, fontWeight = FontWeight.W700, fontSize = 28.sp, lineHeight = 32.sp)
    val Headline = TextStyle(fontFamily = PoulpyFonts.Display, fontWeight = FontWeight.W700, fontSize = 21.sp, lineHeight = 25.sp)
    val Body = TextStyle(fontFamily = PoulpyFonts.Body, fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 24.sp)
    val Label = TextStyle(fontFamily = PoulpyFonts.Body, fontWeight = FontWeight.W800, fontSize = 12.5.sp, lineHeight = 16.sp, letterSpacing = 0.08.em)
    val Timer = TextStyle(fontFamily = PoulpyFonts.Body, fontWeight = FontWeight.W800, fontSize = 64.sp, lineHeight = 64.sp, fontFeatureSettings = "tnum")
}

private val MaterialDefaults = Typography()

internal val PoulpyTypography = Typography(
    displayLarge = MaterialDefaults.displayLarge.copy(fontFamily = PoulpyFonts.Display),
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = PoulpyFonts.Display),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = PoulpyFonts.Display),
    headlineLarge = PoulpyTextStyles.Display,
    headlineMedium = PoulpyTextStyles.Title,
    headlineSmall = MaterialDefaults.headlineSmall.copy(fontFamily = PoulpyFonts.Display),
    titleLarge = PoulpyTextStyles.Headline,
    titleMedium = MaterialDefaults.titleMedium.copy(fontFamily = PoulpyFonts.Body),
    titleSmall = MaterialDefaults.titleSmall.copy(fontFamily = PoulpyFonts.Body),
    bodyLarge = PoulpyTextStyles.Body,
    bodyMedium = MaterialDefaults.bodyMedium.copy(fontFamily = PoulpyFonts.Body),
    bodySmall = MaterialDefaults.bodySmall.copy(fontFamily = PoulpyFonts.Body),
    labelLarge = MaterialDefaults.labelLarge.copy(fontFamily = PoulpyFonts.Body),
    labelMedium = PoulpyTextStyles.Label,
    labelSmall = MaterialDefaults.labelSmall.copy(fontFamily = PoulpyFonts.Body),
)

object PoulpySpace {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object PoulpyRadius {
    val sm = 10.dp
    val md = 16.dp
    val lg = 24.dp
    val pill = 999.dp
}

internal val PoulpyShapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp))

object PoulpyMotion {
    const val flipMillis = 700
    val flipEasing = CubicBezierEasing(0.5f, -0.35f, 0.35f, 1.4f)
    const val moodMillis = 300
    const val quickMillis = 200
}
