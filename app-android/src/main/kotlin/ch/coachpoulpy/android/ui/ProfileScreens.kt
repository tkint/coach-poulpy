package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.BuildConfig
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.BodyRange
import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.TrainingGoal
import ch.coachpoulpy.core.model.Pronouns
import ch.coachpoulpy.core.model.SexAssignedAtBirth
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyMap
import ch.coachpoulpy.shared.BodyDisplay
import ch.coachpoulpy.shared.ChartAxis
import ch.coachpoulpy.shared.LanguageChoice
import ch.coachpoulpy.shared.ThemeChoice
import ch.coachpoulpy.shared.UnitSystem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.datetime.number
import kotlinx.datetime.LocalDate as KDate

/** The pages Settings opens inside its tab. Device pages open over the tabs instead, through [DeviceRoute]. */
enum class SettingsPage { HUB, PROFILE, WEIGHT, HEIGHT, PREFERENCES, DEVICES }

/** A Settings page: a back arrow, a title with an optional line under it, an optional action, then [content] scrolling. */
@Composable
internal fun SubPage(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.padding(start = 4.dp, end = 12.dp, top = PoulpySpace.md), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            action?.invoke()
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = PoulpySpace.lg, bottom = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

/** The Material date dialog on a java.time date. Dates after [latest] are brought back to it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateDialog(initial: LocalDate?, latest: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = (initial ?: latest).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onPick(if (picked.isAfter(latest)) latest else picked)
                }
                onDismiss()
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) { DatePicker(state = state) }
}

private fun KDate.toJava(): LocalDate = LocalDate.of(year, month.number, day)

private fun LocalDate.toShared(): KDate = KDate(year, monthValue, dayOfMonth)

private fun mediumDate(date: LocalDate) = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))

private fun shortDate(date: LocalDate) = date.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

/** Settings › hub: the profile card, Body, App and About. */
@Composable
internal fun SettingsHub(graph: AppGraph, language: String, onPage: (SettingsPage) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val memory = remember { AppPreferences(context) }
    val profileModel = remember(graph) { graph.profile(memory) }
    val profile by profileModel.profile.collectAsStateWithLifecycle()
    val units by graph.settings(memory).state.collectAsStateWithLifecycle()
    val weightModel = remember(graph) { graph.body(BodyMetric.WEIGHT) }
    val heightModel = remember(graph) { graph.body(BodyMetric.HEIGHT) }
    DisposableEffect(weightModel, heightModel) { onDispose { weightModel.close(); heightModel.close() } }
    val weight by weightModel.state.collectAsStateWithLifecycle()
    val height by heightModel.state.collectAsStateWithLifecycle()

    val age = profileModel.age(profile)
    val summary = listOfNotNull(
        age?.let { pluralStringResource(R.plurals.profile_years, it, it) },
        height.latest?.let { BodyDisplay.height(it.value, units.units, language) },
        weight.latest?.let { BodyDisplay.weight(it.value, units.units, language) },
    ).joinToString(" · ")

    Column(modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.tab_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = PoulpySpace.lg),
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = PoulpySpace.md, bottom = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                onClick = { onPage(SettingsPage.PROFILE) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(PoulpyRadius.lg),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.poulpy_mood_happy), contentDescription = null, modifier = Modifier.size(64.dp))
                    Column(Modifier.padding(start = 14.dp).weight(1f)) {
                        Text(profile.displayName ?: stringResource(R.string.settings_yourProfile), style = MaterialTheme.typography.titleLarge)
                        if (summary.isNotEmpty()) Text(summary, style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.settings_editProfile), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
            SectionLabel(R.string.settings_body)
            SettingsList {
                SettingsRow(
                    Icons.Filled.Height, stringResource(R.string.body_height),
                    height.latest?.let { BodyDisplay.height(it.value, units.units, language) } ?: stringResource(R.string.body_nothingLogged),
                ) { onPage(SettingsPage.HEIGHT) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(
                    Icons.Filled.MonitorWeight, stringResource(R.string.body_weight),
                    weight.latest?.let { BodyDisplay.weight(it.value, units.units, language) } ?: stringResource(R.string.body_nothingLogged),
                ) { onPage(SettingsPage.WEIGHT) }
            }
            SectionLabel(R.string.settings_app)
            SettingsList {
                val theme = stringResource(when (units.theme) { ThemeChoice.SYSTEM -> R.string.theme_system; ThemeChoice.LIGHT -> R.string.theme_light; ThemeChoice.DARK -> R.string.theme_dark })
                val languageName = when (units.language) {
                    LanguageChoice.SYSTEM -> stringResource(R.string.language_system)
                    LanguageChoice.ENGLISH -> "English"
                    LanguageChoice.FRENCH -> "Français"
                }
                val system = stringResource(if (units.units == UnitSystem.METRIC) R.string.units_metric else R.string.units_imperial)
                SettingsRow(Icons.Filled.Contrast, stringResource(R.string.settings_appearanceUnits), "$theme · $languageName · $system") { onPage(SettingsPage.PREFERENCES) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Filled.Bluetooth, stringResource(R.string.settings_devices), null) { onPage(SettingsPage.DEVICES) }
            }
            SectionLabel(R.string.settings_about)
            SettingsList {
                SettingsRow(Icons.Filled.Info, stringResource(R.string.settings_aboutApp), stringResource(R.string.settings_aboutLine, BuildConfig.VERSION_NAME), onClick = null)
            }
        }
    }
}

/** Rows in one outlined card, a divider between them. */
@Composable
private fun SettingsList(content: @Composable ColumnScope.() -> Unit) {
    OutlinedSurface { Column(content = content) }
}

/** A leading icon, a bold title with a line under it, and a chevron when it opens something. */
@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String?, onClick: (() -> Unit)?) {
    val row = @Composable {
        Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (onClick != null) Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface, content = row) else row()
}

/** Settings › Your profile. Every field is optional; a change is saved as soon as it is made. */
@Composable
internal fun ProfileScreen(graph: AppGraph, language: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph) { graph.profile(AppPreferences(context)) }
    val profile by model.profile.collectAsStateWithLifecycle()
    val ui by model.ui.collectAsStateWithLifecycle()
    val notSet = stringResource(R.string.profile_notSet)
    var sheet by remember { mutableStateOf<ProfileSheet?>(null) }
    var name by remember { mutableStateOf(profile.displayName.orEmpty()) }
    var custom by remember { mutableStateOf(profile.customPronouns.orEmpty()) }
    var heartRate by remember { mutableStateOf(profile.restingHeartRate?.toString().orEmpty()) }
    val today = remember { LocalDate.now() }

    SubPage(stringResource(R.string.profile_title), stringResource(R.string.profile_subtitle), onBack, modifier) {
        SectionLabel(R.string.profile_aboutYou)
        PoulpyTextField(
            label = stringResource(R.string.profile_name),
            value = name,
            onValueChange = { name = it; model.setDisplayName(it) },
        )
        val born = profile.birthdate?.toJava()
        val age = model.age(profile)
        SelectField(
            stringResource(R.string.profile_birthdate),
            if (born == null) notSet else if (age == null) mediumDate(born) else pluralStringResource(R.plurals.profile_birthdateValue, age, age, mediumDate(born)),
        ) { sheet = ProfileSheet.BIRTHDATE }

        SelectField(
            stringResource(R.string.profile_pronouns),
            when (profile.pronouns) {
                null -> notSet
                Pronouns.CUSTOM -> profile.customPronouns ?: stringResource(Pronouns.CUSTOM.label)
                else -> stringResource(profile.pronouns!!.label)
            },
        ) { sheet = ProfileSheet.PRONOUNS }
        Note(stringResource(R.string.profile_pronounsNote))

        SelectField(stringResource(R.string.profile_sex), profile.sexAssignedAtBirth?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.SEX }
        Note(stringResource(R.string.profile_sexNote))

        SelectField(stringResource(R.string.profile_bodyFigure), profile.bodyFigure?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.FIGURE }
        Note(stringResource(R.string.profile_figureNote))

        SectionLabel(R.string.profile_training)
        SelectField(
            stringResource(R.string.profile_goal),
            if (profile.goals.isEmpty()) notSet else TrainingGoal.entries.filter { it in profile.goals }.map { stringResource(it.label) }.joinToString(", "),
        ) { sheet = ProfileSheet.GOALS }
        SelectField(stringResource(R.string.profile_experience), profile.experience?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.EXPERIENCE }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.profile_restingHr),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            NumberField(
                value = heartRate,
                onValueChange = {
                    heartRate = it
                    model.setRestingHeartRate(it.toIntOrNull())
                },
                description = stringResource(R.string.profile_restingHr),
                unit = stringResource(R.string.profile_bpm),
            )
            Note(stringResource(R.string.profile_restingHrHint))
        }
        val painAreas = ui.painAreas.joinToString(", ") { BodyMap.label(it, language) }
        val pain = ui.painUntil?.let { LocalDate.of(it.year, it.month, it.day) }
        SelectField(
            stringResource(R.string.profile_pain),
            when {
                !ui.painActive -> stringResource(R.string.profile_painNone)
                pain == null -> stringResource(R.string.profile_painUntilCleared, painAreas)
                else -> stringResource(R.string.profile_painUntil, painAreas, shortDate(pain))
            },
        ) { sheet = ProfileSheet.PAIN }
        Note(stringResource(R.string.profile_painNote))
    }

    when (sheet) {
        ProfileSheet.BIRTHDATE -> DateDialog(profile.birthdate?.toJava(), today, onPick = { model.setBirthdate(it.toShared()) }, onDismiss = { sheet = null })
        ProfileSheet.PRONOUNS -> OptionSheet(
            title = stringResource(R.string.profile_pronouns),
            why = stringResource(R.string.profile_pronounsSheetNote),
            options = listOf<Pair<Pronouns?, String>>(null to notSet) + Pronouns.entries.map { it to stringResource(it.label) },
            selected = profile.pronouns,
            note = null,
            onSave = { model.setPronouns(it, custom) },
            onDismiss = { sheet = null },
            extra = { choice ->
                if (choice == Pronouns.CUSTOM) {
                    PoulpyTextField(
                        label = stringResource(R.string.profile_customPronouns),
                        value = custom,
                        onValueChange = { custom = it },
                    )
                }
            },
        )
        ProfileSheet.SEX -> OptionSheet(
            title = stringResource(R.string.profile_sex),
            why = stringResource(R.string.profile_sexWhy),
            options = listOf<Pair<SexAssignedAtBirth?, String>>(null to notSet) + SexAssignedAtBirth.entries.map { it to stringResource(it.label) },
            selected = profile.sexAssignedAtBirth,
            note = stringResource(R.string.profile_sexNotGender),
            onSave = model::setSexAssignedAtBirth,
            onDismiss = { sheet = null },
        )
        ProfileSheet.GOALS -> MultiOptionSheet(
            title = stringResource(R.string.profile_goal),
            why = stringResource(R.string.profile_goalsNote),
            options = TrainingGoal.entries.map { it to stringResource(it.label) },
            selected = profile.goals,
            onSave = model::setGoals,
            onDismiss = { sheet = null },
        )
        ProfileSheet.EXPERIENCE -> StepSheet(
            title = stringResource(R.string.profile_experience),
            why = stringResource(R.string.profile_experienceNote),
            steps = Experience.entries.map { it to stringResource(it.label) },
            selected = profile.experience,
            onSave = model::setExperience,
            onDismiss = { sheet = null },
        )
        ProfileSheet.FIGURE -> BodyFigureSheet(
            figure = profile.bodyFigure,
            language = language,
            onSave = model::setBodyFigure,
            onDismiss = { sheet = null },
        )
        ProfileSheet.PAIN -> PainSheet(
            figure = profile.bodyFigure ?: BodyFigure.SHOULDERS,
            language = language,
            areas = if (ui.painActive) ui.painAreas else emptyList(),
            duration = ui.painDuration,
            onSave = { areas, duration -> model.setPain(areas, duration) },
            onClear = model::clearPain,
            onDismiss = { sheet = null },
        )
        null -> Unit
    }
}

private enum class ProfileSheet { BIRTHDATE, PRONOUNS, SEX, GOALS, EXPERIENCE, FIGURE, PAIN }

private val BodyFigure.label: Int
    get() = when (this) {
        BodyFigure.SHOULDERS -> R.string.profile_figureShoulders
        BodyFigure.HIPS -> R.string.profile_figureHips
    }

private val Pronouns.label: Int
    get() = when (this) {
        Pronouns.SHE_HER -> R.string.profile_pronounSheHer
        Pronouns.HE_HIM -> R.string.profile_pronounHeHim
        Pronouns.THEY_THEM -> R.string.profile_pronounTheyThem
        Pronouns.JUST_MY_NAME -> R.string.profile_pronounJustMyName
        Pronouns.CUSTOM -> R.string.profile_pronounCustom
        Pronouns.PREFER_NOT_TO_SAY -> R.string.profile_pronounPreferNotToSay
    }

private val SexAssignedAtBirth.label: Int
    get() = when (this) {
        SexAssignedAtBirth.FEMALE -> R.string.profile_sexFemale
        SexAssignedAtBirth.MALE -> R.string.profile_sexMale
        SexAssignedAtBirth.INTERSEX -> R.string.profile_sexIntersex
        SexAssignedAtBirth.PREFER_NOT_TO_SAY -> R.string.profile_sexPreferNotToSay
    }

private val TrainingGoal.label: Int
    get() = when (this) {
        TrainingGoal.STAY_ACTIVE -> R.string.profile_goalStayActive
        TrainingGoal.BUILD_STRENGTH -> R.string.profile_goalBuildStrength
        TrainingGoal.RUN_FURTHER -> R.string.profile_goalRunFurther
        TrainingGoal.REHAB -> R.string.profile_goalRehab
        TrainingGoal.SOMETHING_ELSE -> R.string.profile_goalSomethingElse
    }

private val Experience.label: Int
    get() = when (this) {
        Experience.JUST_STARTING -> R.string.profile_experienceJustStarting
        Experience.GETTING_GOING -> R.string.profile_experienceGettingGoing
        Experience.REGULAR -> R.string.profile_experienceRegular
        Experience.EXPERIENCED -> R.string.profile_experienceExperienced
        Experience.VERY_EXPERIENCED -> R.string.profile_experienceVeryExperienced
    }

/** Settings › Appearance and units: theme, language and units as selects. */
@Composable
internal fun PreferencesScreen(graph: AppGraph, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph) { graph.settings(AppPreferences(context)) }
    val state by model.state.collectAsStateWithLifecycle()
    var sheet by remember { mutableStateOf<PreferenceSheet?>(null) }

    val themes = listOf(ThemeChoice.SYSTEM to R.string.theme_system, ThemeChoice.LIGHT to R.string.theme_light, ThemeChoice.DARK to R.string.theme_dark)
    // Language names stay in their own language, so anyone can find theirs.
    val languages = listOf(LanguageChoice.SYSTEM to stringResource(R.string.language_system), LanguageChoice.ENGLISH to "English", LanguageChoice.FRENCH to "Français")
    val unitSystems = listOf(UnitSystem.METRIC to R.string.units_metric, UnitSystem.IMPERIAL to R.string.units_imperial)

    SubPage(stringResource(R.string.settings_appearanceUnits), null, onBack, modifier) {
        SectionLabel(R.string.settings_appearance)
        SelectField(stringResource(R.string.settings_theme), stringResource(themes.first { it.first == state.theme }.second)) { sheet = PreferenceSheet.THEME }
        Note(stringResource(R.string.settings_themeHint))
        SectionLabel(R.string.settings_region)
        SelectField(stringResource(R.string.settings_language), languages.first { it.first == state.language }.second) { sheet = PreferenceSheet.LANGUAGE }
        SelectField(stringResource(R.string.settings_units), stringResource(unitSystems.first { it.first == state.units }.second)) { sheet = PreferenceSheet.UNITS }
        Note(stringResource(R.string.settings_unitsHint))
    }

    when (sheet) {
        PreferenceSheet.THEME -> OptionSheet(
            title = stringResource(R.string.settings_theme), why = null,
            options = themes.map { (value, label) -> value to stringResource(label) },
            selected = state.theme, note = null,
            onSave = { it?.let(model::setTheme) }, onDismiss = { sheet = null },
        )
        PreferenceSheet.LANGUAGE -> OptionSheet(
            title = stringResource(R.string.settings_language), why = null,
            options = languages,
            selected = state.language, note = stringResource(R.string.settings_languageNextLaunch),
            onSave = { it?.let(model::setLanguage) }, onDismiss = { sheet = null },
        )
        PreferenceSheet.UNITS -> OptionSheet(
            title = stringResource(R.string.settings_units), why = null,
            options = unitSystems.map { (value, label) -> value to stringResource(label) },
            selected = state.units, note = null,
            onSave = { it?.let(model::setUnits) }, onDismiss = { sheet = null },
        )
        null -> Unit
    }
}

private enum class PreferenceSheet { THEME, LANGUAGE, UNITS }

/** Settings › Weight or Height: the latest value, the chart, and the log. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BodyScreen(graph: AppGraph, metric: BodyMetric, language: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph, metric) { graph.body(metric) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val units by graph.settings(AppPreferences(context)).state.collectAsStateWithLifecycle()
    val system = units.units
    var logging by remember { mutableStateOf(false) }
    val isWeight = metric == BodyMetric.WEIGHT
    val colors = MaterialTheme.colorScheme

    fun show(value: Double) = BodyDisplay.value(metric, value, system, language)

    SubPage(
        title = stringResource(if (isWeight) R.string.body_weight else R.string.body_height),
        subtitle = state.latest?.let { stringResource(R.string.body_lastLogged, mediumDate(it.day.toJava())) } ?: stringResource(R.string.body_nothingLogged),
        onBack = onBack,
        modifier = modifier,
        action = { Button(onClick = { logging = true }, modifier = Modifier.height(40.dp)) { Text(stringResource(R.string.body_log), fontWeight = FontWeight.ExtraBold) } },
    ) {
        if (state.history.isEmpty()) {
            PoulpyCard(
                title = stringResource(if (isWeight) R.string.body_emptyWeightTitle else R.string.body_emptyHeightTitle),
                body = stringResource(if (isWeight) R.string.body_emptyWeightBody else R.string.body_emptyHeightBody),
                button = stringResource(if (isWeight) R.string.body_logWeight else R.string.body_logHeight),
                onClick = { logging = true },
                buttonIcon = Icons.Filled.Add,
            )
            return@SubPage
        }
        state.latest?.let { latest ->
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = colors.primaryContainer) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.poulpy_mood_happy), contentDescription = null, modifier = Modifier.size(56.dp))
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(show(latest.value), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        Text(pluralStringResource(R.plurals.body_entries, state.history.size, state.history.size), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        OutlinedSurface {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(if (isWeight) R.string.body_weight else R.string.body_height), fontWeight = FontWeight.ExtraBold)
                val entries = state.trend.entries
                val description = if (entries.isEmpty()) {
                    stringResource(R.string.body_chartEmpty)
                } else {
                    stringResource(
                        R.string.body_chart, show(entries.first().value), mediumDate(entries.first().day.toJava()),
                        show(entries.last().value), mediumDate(entries.last().day.toJava()),
                    )
                }
                // Always drawn: with nothing in the range it is the grid alone, around the latest value.
                LineChart(entries, state.latest?.value, metric, system, language, Modifier.fillMaxWidth().height(200.dp).semantics { contentDescription = description })
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BodyRange.entries.forEach { range ->
                        val on = state.range == range
                        Surface(
                            onClick = { model.selectRange(range) },
                            modifier = Modifier.weight(1f).height(36.dp).semantics { selected = on; role = Role.Tab },
                            shape = RoundedCornerShape(PoulpyRadius.pill),
                            color = if (on) colors.primary else Color.Transparent,
                            contentColor = if (on) colors.onPrimary else colors.onSurfaceVariant,
                            border = BorderStroke(1.5.dp, if (on) colors.primary else colors.outline),
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(stringResource(range.label), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge) }
                        }
                    }
                }
            }
        }

        if (isWeight && state.trend.change != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(R.string.body_change, BodyDisplay.weightChange(state.trend.change!!, system, language), stringResource(state.range.over), Modifier.weight(1f))
                state.trend.lowest?.let { Stat(R.string.body_lowest, show(it.value), shortDate(it.day.toJava()), Modifier.weight(1f)) }
                state.trend.highest?.let { Stat(R.string.body_highest, show(it.value), shortDate(it.day.toJava()), Modifier.weight(1f)) }
            }
        }
        if (!isWeight) Note(stringResource(R.string.body_heightNote))

        if (state.history.isNotEmpty()) {
            SectionLabel(R.string.body_history)
            state.history.forEach { entry ->
                OutlinedSurface {
                    Row(Modifier.padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(show(entry.value), fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text(mediumDate(entry.day.toJava()), color = colors.onSurfaceVariant)
                        IconButton(onClick = { model.delete(entry.day) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.body_deleteEntry, mediumDate(entry.day.toJava())))
                        }
                    }
                }
            }
        }
    }

    if (logging) {
        LogSheet(
            metric = metric,
            system = system,
            language = language,
            start = state.latest?.value ?: BodyDisplay.starting(metric),
            onSave = { value, day -> model.log(value, day.toShared()) },
            onDismiss = { logging = false },
        )
    }
}

private val BodyRange.label: Int
    get() = when (this) {
        BodyRange.MONTH -> R.string.body_range1m
        BodyRange.THREE_MONTHS -> R.string.body_range3m
        BodyRange.YEAR -> R.string.body_range1y
        BodyRange.ALL -> R.string.body_rangeAll
    }

private val BodyRange.over: Int
    get() = when (this) {
        BodyRange.MONTH -> R.string.body_overMonth
        BodyRange.THREE_MONTHS -> R.string.body_overThreeMonths
        BodyRange.YEAR -> R.string.body_overYear
        BodyRange.ALL -> R.string.body_overAll
    }

@Composable
private fun Stat(@StringRes name: Int, value: String, note: String, modifier: Modifier) {
    OutlinedSurface(modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Text(stringResource(name), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.ExtraBold)
            Text(note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * A line with a dot per entry in the one accent colour, over a grid of round values with the unit at the top of the
 * axis. A single entry is just its dot. The grid is neutral: a weight going up is never red.
 */
@Composable
private fun LineChart(entries: List<BodyMeasurement>, latest: Double?, metric: BodyMetric, system: UnitSystem, language: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val line = colors.primary
    val grid = colors.outlineVariant
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant)
    val shown = entries.map { BodyDisplay.chartValue(metric, it.value, system) }
    val ticks = if (entries.isEmpty()) {
        val centre = BodyDisplay.chartValue(metric, latest ?: BodyDisplay.starting(metric), system)
        ChartAxis.ticks(centre, centre, BodyDisplay.chartMinSpan(metric, system) * 3)
    } else {
        ChartAxis.ticks(shown.min(), shown.max(), BodyDisplay.chartMinSpan(metric, system))
    }
    val labels = ticks.map { BodyDisplay.axisLabel(metric, it, system, language) }
    val unit = BodyDisplay.axisUnit(metric, system)
    val firstDay = entries.firstOrNull()?.day?.toJava()
    val lastDay = entries.lastOrNull()?.day?.toJava()
    Canvas(modifier) {
        val labelWidth = (labels + unit).maxOf { measurer.measure(it, style).size.width } + 8.dp.toPx()
        val textHeight = measurer.measure("0", style).size.height
        val left = labelWidth + 6.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val top = textHeight + 8.dp.toPx()
        val bottom = size.height - textHeight - 8.dp.toPx()
        val low = ticks.first()
        val span = (ticks.last() - low).coerceAtLeast(0.001)
        fun yOf(value: Double) = (bottom - (bottom - top) * ((value - low) / span)).toFloat()

        drawText(measurer, unit, Offset(labelWidth - measurer.measure(unit, style).size.width, 0f), style)
        ticks.forEachIndexed { i, tick ->
            val y = yOf(tick)
            drawLine(grid, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
            val text = measurer.measure(labels[i], style)
            drawText(text, topLeft = Offset(labelWidth - text.size.width, y - text.size.height / 2f))
        }

        if (firstDay == null || lastDay == null) return@Canvas
        val firstEpoch = firstDay.toEpochDay()
        val days = (lastDay.toEpochDay() - firstEpoch).coerceAtLeast(1)
        val points = entries.mapIndexed { i, entry ->
            val x = if (entries.size == 1) (left + right) / 2 else left + (right - left) * (entry.day.toJava().toEpochDay() - firstEpoch) / days
            Offset(x.toFloat(), yOf(shown[i]))
        }
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, line, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        points.forEach { drawCircle(line, radius = 4.5.dp.toPx(), center = it) }

        val dateY = size.height - textHeight
        val first = measurer.measure(shortDate(firstDay), style)
        drawText(first, topLeft = Offset(left, dateY))
        if (entries.size > 1) {
            val last = measurer.measure(shortDate(lastDay), style)
            drawText(last, topLeft = Offset(right - last.size.width, dateY))
        }
    }
}

/** Log a weight or height: type the number (the keyboard opens with it selected), nudge with − and +, pick the date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogSheet(metric: BodyMetric, system: UnitSystem, language: String, start: Double, onSave: (Double, LocalDate) -> Unit, onDismiss: () -> Unit) {
    val today = remember { LocalDate.now() }
    val isWeight = metric == BodyMetric.WEIGHT
    val feetAndInches = !isWeight && system == UnitSystem.IMPERIAL
    var text by remember { mutableStateOf(if (isWeight) BodyDisplay.weightText(start, system, language) else BodyDisplay.centimetresText(start)) }
    var feet by remember { mutableStateOf(BodyDisplay.feetText(start)) }
    var inches by remember { mutableStateOf(BodyDisplay.inchesText(start, language)) }
    var day by remember { mutableStateOf(today) }
    var pickingDate by remember { mutableStateOf(false) }

    // What the fields hold now, in kilograms or centimetres; null while they are empty or out of range.
    val typed: Double? = when {
        isWeight -> BodyDisplay.weightFrom(text, system)
        feetAndInches -> BodyDisplay.centimetresFrom(feet, inches)
        else -> BodyDisplay.centimetresFrom(text)
    }
    fun nudge(direction: Int) {
        val next = BodyDisplay.step(metric, typed ?: start, system, direction)
        if (isWeight) text = BodyDisplay.weightText(next, system, language)
        else if (feetAndInches) { feet = BodyDisplay.feetText(next); inches = BodyDisplay.inchesText(next, language) }
        else text = BodyDisplay.centimetresText(next)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(if (isWeight) R.string.body_logWeight else R.string.body_logHeight), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(if (isWeight) R.string.body_weightStored else R.string.body_heightStored),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            NumberEntry(onLess = { nudge(-1) }, onMore = { nudge(+1) }) { fieldModifier ->
                if (feetAndInches) {
                    Row(fieldModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(feet, { feet = it }, stringResource(R.string.body_feet), Modifier.weight(1f), unit = stringResource(R.string.body_feet), large = true)
                        NumberField(inches, { inches = it }, stringResource(R.string.body_inches), Modifier.weight(1f), unit = stringResource(R.string.body_inches), decimal = true, large = true)
                    }
                } else {
                    NumberField(
                        text, { text = it },
                        stringResource(if (isWeight) R.string.body_weight else R.string.body_height),
                        fieldModifier,
                        unit = BodyDisplay.axisUnit(metric, system),
                        decimal = isWeight,
                        large = true,
                    )
                }
            }
            SelectField(
                stringResource(R.string.body_date),
                if (day == today) stringResource(R.string.body_today, mediumDate(day)) else mediumDate(day),
            ) { pickingDate = true }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
                Button(onClick = { typed?.let { onSave(it, day); onDismiss() } }, enabled = typed != null, modifier = Modifier.weight(1.6f).height(56.dp)) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
    if (pickingDate) DateDialog(day, today, onPick = { day = it }, onDismiss = { pickingDate = false })
}

/** Settings › Devices, now a page of its own with a back arrow. */
@Composable
internal fun DevicesPage(graph: AppGraph, onBack: () -> Unit, onOpen: (DeviceRoute) -> Unit, modifier: Modifier = Modifier) {
    SubPage(
        title = stringResource(R.string.settings_devices),
        subtitle = null,
        onBack = onBack,
        modifier = modifier,
        action = {
            FilledTonalIconButton(onClick = { onOpen(DeviceRoute.Add) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.devices_add))
            }
        },
    ) { Devices(graph, onOpen) }
}
