package ch.coachpoulpy.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeChoice { SYSTEM, LIGHT, DARK }

/** [code] is what the apps pass on as the language; null follows the phone. */
enum class LanguageChoice(val code: String?) { SYSTEM(null), ENGLISH("en"), FRENCH("fr") }

enum class UnitSystem { METRIC, IMPERIAL }

data class SettingsUiState(
    val theme: ThemeChoice = ThemeChoice.SYSTEM,
    val language: LanguageChoice = LanguageChoice.SYSTEM,
    val units: UnitSystem = UnitSystem.METRIC,
)

/** Settings › General: theme, language and units, kept on the phone. */
class SettingsModel(
    private val memory: KeyValueStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _state = MutableStateFlow(
        SettingsUiState(
            theme = read(THEME, ThemeChoice.SYSTEM),
            language = read(LANGUAGE, LanguageChoice.SYSTEM),
            units = read(UNITS, UnitSystem.METRIC),
        ),
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setTheme(theme: ThemeChoice) {
        memory.putString(THEME, theme.name)
        _state.update { it.copy(theme = theme) }
    }

    fun setLanguage(language: LanguageChoice) {
        memory.putString(LANGUAGE, language.name)
        _state.update { it.copy(language = language) }
    }

    fun setUnits(units: UnitSystem) {
        memory.putString(UNITS, units.name)
        _state.update { it.copy(units = units) }
    }

    fun watch(onChange: (SettingsUiState) -> Unit): Cancellable = state.watchIn(scope, onChange)

    private inline fun <reified T : Enum<T>> read(key: String, default: T): T =
        memory.getString(key)?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } } ?: default

    companion object {
        private const val THEME = "settings.theme"
        private const val LANGUAGE = "settings.language"
        private const val UNITS = "settings.units"
    }
}

/** A number in the user's units, with the unit to print after it. The apps format [value] in their locale. */
data class Measure(val value: Double, val decimals: Int, val unit: String)

/** A height in feet and inches, the inches in steps of a half (the input allows no finer). */
data class FeetInches(val feet: Int, val inches: Double)

/** Distances, speeds and weights in the unit system the user picked. Weights are stored in kilograms and converted to show. */
object Units {
    private const val METERS_PER_MILE = 1609.344
    private const val POUNDS_PER_KG = 2.2046226218

    fun poundsOf(kg: Double) = kg * POUNDS_PER_KG

    fun kilogramsOf(pounds: Double) = pounds / POUNDS_PER_KG

    /** A weight in the user's unit: kilograms as they are, pounds to one decimal. */
    fun weight(kg: Double, system: UnitSystem) = when (system) {
        UnitSystem.METRIC -> Measure(kg, 1, "kg")
        UnitSystem.IMPERIAL -> Measure(poundsOf(kg), 1, "lb")
    }

    private const val CM_PER_INCH = 2.54

    /** Centimetres as feet and inches, rounded to the nearest half inch; 11.75 in is 1 ft 0 in, never 0 ft 12 in. */
    fun feetAndInches(cm: Double): FeetInches {
        val halfInches = kotlin.math.round(cm / CM_PER_INCH * 2).toInt()
        return FeetInches(halfInches / 24, (halfInches % 24) / 2.0)
    }

    fun centimetresOf(feet: Int, inches: Double) = (feet * 12 + inches) * CM_PER_INCH

    /** A height in the user's unit. Metric shows whole centimetres; imperial uses [feetAndInches] instead. */
    fun height(cm: Double, system: UnitSystem) = when (system) {
        UnitSystem.METRIC -> Measure(cm, 0, "cm")
        UnitSystem.IMPERIAL -> Measure(cm / CM_PER_INCH, 1, "in")
    }

    fun distance(meters: Double, system: UnitSystem, decimals: Int = 2) = when (system) {
        UnitSystem.METRIC -> Measure(meters / 1000, decimals, "km")
        UnitSystem.IMPERIAL -> Measure(meters / METERS_PER_MILE, decimals, "mi")
    }

    fun speed(kmh: Double, system: UnitSystem) = when (system) {
        UnitSystem.METRIC -> Measure(kmh, 1, "km/h")
        UnitSystem.IMPERIAL -> Measure(kmh * 1000 / METERS_PER_MILE, 1, "mph")
    }

    /** Seconds per kilometre or per mile; null below 1 km/h, where a pace means nothing. */
    fun paceSeconds(kmh: Double, system: UnitSystem): Double? {
        if (kmh < 1.0) return null
        val perKm = 3600 / kmh
        return if (system == UnitSystem.METRIC) perKm else perKm * METERS_PER_MILE / 1000
    }

    fun paceUnit(system: UnitSystem) = if (system == UnitSystem.METRIC) "/km" else "/mi"
}
