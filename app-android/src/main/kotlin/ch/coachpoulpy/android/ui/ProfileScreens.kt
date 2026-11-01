package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.BuildConfig
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyMeasurement
import ch.coachpoulpy.core.model.BodyMetric
import ch.coachpoulpy.core.model.BodyRange
import ch.coachpoulpy.core.model.Experience
import ch.coachpoulpy.core.model.MainGoal
import ch.coachpoulpy.core.model.Pronouns
import ch.coachpoulpy.core.model.SexAssignedAtBirth
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyDisplay
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

/** A labelled value that opens a sheet or dialog when pressed. */
@Composable
internal fun SelectField(label: String, value: String, onClick: () -> Unit) {
    OutlinedSurface(onClick = onClick) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.ExtraBold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
}

/** Pick one option in a sheet and save it. A null value is "not set". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : Any> OptionSheet(
    title: String,
    why: String?,
    options: List<Pair<T?, String>>,
    selected: T?,
    note: String?,
    onSave: (T?) -> Unit,
    onDismiss: () -> Unit,
) {
    var choice by remember { mutableStateOf(selected) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            why?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Column(Modifier.selectableGroup()) {
                options.forEach { (value, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = value == choice, role = Role.RadioButton, onClick = { choice = value })
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == choice, onClick = null, modifier = Modifier.padding(end = 12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            note?.let { Note(it) }
            Button(onClick = { onSave(choice); onDismiss() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold)
            }
        }
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
                }
            }
            SectionLabel(R.string.settings_body)
            SettingsRow(stringResource(R.string.body_weight), weight.latest?.let { BodyDisplay.weight(it.value, units.units, language) }) { onPage(SettingsPage.WEIGHT) }
            SettingsRow(stringResource(R.string.body_height), height.latest?.let { BodyDisplay.height(it.value, units.units, language) }) { onPage(SettingsPage.HEIGHT) }
            SectionLabel(R.string.settings_app)
            SettingsRow(stringResource(R.string.settings_appearanceUnits), null) { onPage(SettingsPage.PREFERENCES) }
            SettingsRow(stringResource(R.string.settings_devices), null) { onPage(SettingsPage.DEVICES) }
            SectionLabel(R.string.settings_about)
            SettingsRow(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), null, onClick = null)
        }
    }
}

@Composable
private fun SettingsRow(title: String, value: String?, onClick: (() -> Unit)?) {
    OutlinedSurface(onClick = onClick) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            value?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp)) }
            if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Settings › Your profile. Every field is optional; a change is saved as soon as it is made. */
@Composable
internal fun ProfileScreen(graph: AppGraph, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph) { graph.profile(AppPreferences(context)) }
    val profile by model.profile.collectAsStateWithLifecycle()
    val notSet = stringResource(R.string.profile_notSet)
    var sheet by remember { mutableStateOf<ProfileSheet?>(null) }
    var name by remember { mutableStateOf(profile.displayName.orEmpty()) }
    var custom by remember { mutableStateOf(profile.customPronouns.orEmpty()) }
    var heartRate by remember { mutableStateOf(profile.restingHeartRate?.toString().orEmpty()) }
    val today = remember { LocalDate.now() }

    SubPage(stringResource(R.string.profile_title), stringResource(R.string.profile_subtitle), onBack, modifier) {
        SectionLabel(R.string.profile_aboutYou)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; model.setDisplayName(it) },
            label = { Text(stringResource(R.string.profile_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val born = profile.birthdate?.toJava()
        val age = model.age(profile)
        SelectField(
            stringResource(R.string.profile_birthdate),
            if (born == null) notSet else if (age == null) mediumDate(born) else pluralStringResource(R.plurals.profile_birthdateValue, age, age, mediumDate(born)),
        ) { sheet = ProfileSheet.BIRTHDATE }

        SelectField(stringResource(R.string.profile_pronouns), profile.pronouns?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.PRONOUNS }
        if (profile.pronouns == Pronouns.CUSTOM) {
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it; model.setPronouns(Pronouns.CUSTOM, it) },
                label = { Text(stringResource(R.string.profile_customPronouns)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Note(stringResource(R.string.profile_pronounsNote))

        SelectField(stringResource(R.string.profile_sex), profile.sexAssignedAtBirth?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.SEX }
        Note(stringResource(R.string.profile_sexNote))

        SectionLabel(R.string.profile_training)
        SelectField(stringResource(R.string.profile_goal), profile.mainGoal?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.GOAL }
        SelectField(stringResource(R.string.profile_experience), profile.experience?.let { stringResource(it.label) } ?: notSet) { sheet = ProfileSheet.EXPERIENCE }
        OutlinedTextField(
            value = heartRate,
            onValueChange = { text ->
                heartRate = text.filter { it.isDigit() }.take(3)
                model.setRestingHeartRate(heartRate.toIntOrNull())
            },
            label = { Text(stringResource(R.string.profile_restingHr)) },
            supportingText = { Text(stringResource(R.string.profile_restingHrHint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    when (sheet) {
        ProfileSheet.BIRTHDATE -> DateDialog(profile.birthdate?.toJava(), today, onPick = { model.setBirthdate(it.toShared()) }, onDismiss = { sheet = null })
        ProfileSheet.PRONOUNS -> OptionSheet(
            title = stringResource(R.string.profile_pronouns),
            why = stringResource(R.string.profile_pronounsNote),
            options = listOf<Pair<Pronouns?, String>>(null to notSet) + Pronouns.entries.map { it to stringResource(it.label) },
            selected = profile.pronouns,
            note = null,
            onSave = { model.setPronouns(it, custom) },
            onDismiss = { sheet = null },
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
        ProfileSheet.GOAL -> OptionSheet(
            title = stringResource(R.string.profile_goal),
            why = null,
            options = listOf<Pair<MainGoal?, String>>(null to notSet) + MainGoal.entries.map { it to stringResource(it.label) },
            selected = profile.mainGoal,
            note = null,
            onSave = model::setMainGoal,
            onDismiss = { sheet = null },
        )
        ProfileSheet.EXPERIENCE -> OptionSheet(
            title = stringResource(R.string.profile_experience),
            why = null,
            options = listOf<Pair<Experience?, String>>(null to notSet) + Experience.entries.map { it to stringResource(it.label) },
            selected = profile.experience,
            note = null,
            onSave = model::setExperience,
            onDismiss = { sheet = null },
        )
        null -> Unit
    }
}

private enum class ProfileSheet { BIRTHDATE, PRONOUNS, SEX, GOAL, EXPERIENCE }

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

private val MainGoal.label: Int
    get() = when (this) {
        MainGoal.STAY_ACTIVE -> R.string.profile_goalStayActive
        MainGoal.BUILD_STRENGTH -> R.string.profile_goalBuildStrength
        MainGoal.RUN_FURTHER -> R.string.profile_goalRunFurther
        MainGoal.REHAB -> R.string.profile_goalRehab
        MainGoal.SOMETHING_ELSE -> R.string.profile_goalSomethingElse
    }

private val Experience.label: Int
    get() = when (this) {
        Experience.JUST_STARTING -> R.string.profile_experienceJustStarting
        Experience.REGULAR -> R.string.profile_experienceRegular
        Experience.EXPERIENCED -> R.string.profile_experienceExperienced
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
        action = { FilledTonalButton(onClick = { logging = true }) { Text(stringResource(R.string.body_log)) } },
    ) {
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
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BodyRange.entries.forEach { range ->
                        FilterChip(
                            selected = state.range == range,
                            onClick = { model.selectRange(range) },
                            label = { Text(stringResource(range.label), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                val entries = state.trend.entries
                if (entries.isEmpty()) {
                    Note(stringResource(R.string.body_chartEmpty))
                } else {
                    val description = stringResource(
                        R.string.body_chart, show(entries.first().value), mediumDate(entries.first().day.toJava()),
                        show(entries.last().value), mediumDate(entries.last().day.toJava()),
                    )
                    LineChart(entries, Modifier.fillMaxWidth().height(160.dp).semantics { contentDescription = description })
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

/** A line with a dot per entry, in the one accent colour. A single entry is just its dot. */
@Composable
private fun LineChart(entries: List<BodyMeasurement>, modifier: Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val pad = 8.dp.toPx()
        val width = size.width - 2 * pad
        val heightPx = size.height - 2 * pad
        val first = entries.first().day.toJava().toEpochDay()
        val span = (entries.last().day.toJava().toEpochDay() - first).coerceAtLeast(1)
        val low = entries.minOf { it.value }
        val range = (entries.maxOf { it.value } - low).coerceAtLeast(0.001)
        val points = entries.map {
            val x = if (entries.size == 1) size.width / 2 else pad + width * (it.day.toJava().toEpochDay() - first) / span
            val y = if (entries.size == 1 || range <= 0.001) size.height / 2 else pad + heightPx * (1f - ((it.value - low) / range).toFloat())
            Offset(x.toFloat(), y.toFloat())
        }
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        points.forEach { drawCircle(color, radius = 4.dp.toPx(), center = it) }
    }
}

/** Log a weight or height: a stepper in the person's units and a date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogSheet(metric: BodyMetric, system: UnitSystem, language: String, start: Double, onSave: (Double, LocalDate) -> Unit, onDismiss: () -> Unit) {
    val today = remember { LocalDate.now() }
    var value by remember { mutableStateOf(start) }
    var day by remember { mutableStateOf(today) }
    var pickingDate by remember { mutableStateOf(false) }
    val isWeight = metric == BodyMetric.WEIGHT

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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { value = BodyDisplay.step(metric, value, system, -1) }, modifier = Modifier.size(56.dp), contentPadding = PaddingValues(0.dp)) {
                    Text("−", style = MaterialTheme.typography.headlineSmall)
                }
                Text(BodyDisplay.value(metric, value, system, language), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                OutlinedButton(onClick = { value = BodyDisplay.step(metric, value, system, +1) }, modifier = Modifier.size(56.dp), contentPadding = PaddingValues(0.dp)) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
            SelectField(
                stringResource(R.string.body_date),
                if (day == today) stringResource(R.string.body_today, mediumDate(day)) else mediumDate(day),
            ) { pickingDate = true }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.height(52.dp)) { Text(stringResource(R.string.action_cancel)) }
                Button(onClick = { onSave(value, day); onDismiss() }, modifier = Modifier.weight(1f).height(52.dp)) {
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
