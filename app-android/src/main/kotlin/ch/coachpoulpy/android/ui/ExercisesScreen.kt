package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.BodyPosition
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.ExerciseFocus
import ch.coachpoulpy.core.model.ExerciseTag
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyView
import ch.coachpoulpy.shared.Zones
import ch.coachpoulpy.shared.ExerciseRow

/**
 * The searchable catalog, used by the library and by the builder's picker: a pill
 * search, five filter chips (equipment, focus, position, zones, tags) that each open a
 * sheet, the count line, then card rows grouped by movement pattern, or ranked results
 * while searching. In the picker ([adding]) each row ends with a round plus instead of
 * a chevron, and recently used exercises come first. With [onOpen] the row opens the
 * exercise's details and only the plus picks it ([onPick]).
 */
@Composable
fun ExerciseList(
    graph: AppGraph,
    language: String,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    adding: Boolean = false,
    onOpen: ((String) -> Unit)? = null,
) {
    val model = remember(graph, language) { graph.exerciseList(language) }
    val context = LocalContext.current
    val equipment = remember(graph, context) { graph.equipmentFilter(AppPreferences(context)) }
    val figure = remember(graph, context) { graph.profile(AppPreferences(context)).profile.value.bodyFigure ?: BodyFigure.SHOULDERS }
    var query by rememberSaveable { mutableStateOf("") }
    var ticked by remember(equipment) { mutableStateOf(equipment.ticked) }
    var focus by rememberSaveable(stateSaver = enumSetSaver<ExerciseFocus>()) { mutableStateOf(emptySet()) }
    var positions by rememberSaveable(stateSaver = enumSetSaver<BodyPosition>()) { mutableStateOf(emptySet()) }
    var tags by rememberSaveable(stateSaver = enumSetSaver<ExerciseTag>()) { mutableStateOf(emptySet()) }
    var picked by rememberSaveable(stateSaver = stringSetSaver) { mutableStateOf(emptySet()) }
    var sheet by rememberSaveable { mutableStateOf<FilterSheet?>(null) }
    val listing = remember(model, query, ticked, focus, positions, tags, picked) {
        model.filter(query, ticked, adding, focus, positions, tags, Zones.zones(picked))
    }
    val tick = { value: Set<Equipment>? ->
        equipment.ticked = value
        ticked = equipment.ticked
    }
    val filtering = ticked != null || focus.isNotEmpty() || positions.isNotEmpty() || tags.isNotEmpty() || picked.isNotEmpty()
    val clearFilters = {
        tick(null)
        focus = emptySet()
        positions = emptySet()
        tags = emptySet()
        picked = emptySet()
    }
    val clearAll = {
        query = ""
        clearFilters()
    }

    Column(modifier.fillMaxSize()) {
        ListFilters(
            query = query,
            onQuery = { query = it },
            placeholder = stringResource(R.string.exercises_search),
            label = stringResource(R.string.exercises_searchLabel),
            countLabel = listing.countLabel,
            chips = {
                item {
                    FilterChipButton(
                        Icons.Filled.FitnessCenter, ticked?.size ?: 0, stringResource(R.string.exercises_equipment),
                        pluralStringResource(R.plurals.exercises_equipmentTicked, ticked?.size ?: 0, ticked?.size ?: 0),
                    ) { sheet = FilterSheet.EQUIPMENT }
                }
                item {
                    FilterChipButton(
                        Icons.Filled.GpsFixed, focus.size, stringResource(R.string.exercises_focus),
                        pluralStringResource(R.plurals.exercises_focusTicked, focus.size, focus.size),
                    ) { sheet = FilterSheet.FOCUS }
                }
                item {
                    FilterChipButton(
                        Icons.Filled.SelfImprovement, positions.size, stringResource(R.string.exercises_position),
                        pluralStringResource(R.plurals.exercises_positionTicked, positions.size, positions.size),
                    ) { sheet = FilterSheet.POSITION }
                }
                item {
                    val zones = Zones.zones(picked).size
                    FilterChipButton(
                        Icons.Filled.Accessibility, zones, stringResource(R.string.exercises_zones),
                        pluralStringResource(R.plurals.exercises_zonesTicked, zones, zones),
                    ) { sheet = FilterSheet.ZONES }
                }
                item {
                    FilterChipButton(
                        Icons.Filled.Sell, tags.size, stringResource(R.string.exercises_tags),
                        pluralStringResource(R.plurals.exercises_tagsTicked, tags.size, tags.size),
                    ) { sheet = FilterSheet.TAGS }
                }
            },
            action = {
                if (filtering) {
                    TextButton(onClick = clearFilters) {
                        Text(stringResource(R.string.exercises_clearFilters), fontWeight = FontWeight.ExtraBold)
                    }
                }
            },
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            listing.sections.forEachIndexed { index, section ->
                section.title?.let { title ->
                    item(key = "section:$index") {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 10.dp).semantics { heading() },
                        )
                    }
                }
                items(section.rows, key = { "$index:${it.slug}" }) { row ->
                    if (adding && onOpen != null) {
                        PickerCard(row, onOpen = { onOpen(row.slug) }, onAdd = { onPick(row.slug) })
                    } else {
                        ExerciseCard(row, adding, onClick = { onPick(row.slug) })
                    }
                }
            }
            if (listing.count == 0) {
                item(key = "empty") { NoExercises(query.trim(), onClear = clearAll) }
            }
        }
    }
    val close = { sheet = null }
    when (sheet) {
        null -> Unit
        FilterSheet.EQUIPMENT -> OptionSheet(
            title = stringResource(R.string.exercises_equipment),
            hint = stringResource(R.string.exercises_equipmentHint),
            rows = model.equipmentOptions.map { OptionRow(it.equipment, it.label, it.countLabel) },
            ticked = ticked ?: emptySet(),
            fitting = { model.filter(query, it, false, focus, positions, tags, Zones.zones(picked)).count },
            onApply = {
                tick(it.takeIf { items -> items.isNotEmpty() })
                close()
            },
            onDismiss = close,
        )
        FilterSheet.FOCUS -> OptionSheet(
            title = stringResource(R.string.exercises_focus),
            hint = stringResource(R.string.exercises_focusHint),
            rows = model.focusOptions.map { OptionRow(it.value, it.label, it.countLabel) },
            ticked = focus,
            fitting = { model.filter(query, ticked, false, it, positions, tags, Zones.zones(picked)).count },
            onApply = {
                focus = it
                close()
            },
            onDismiss = close,
        )
        FilterSheet.POSITION -> OptionSheet(
            title = stringResource(R.string.exercises_position),
            hint = stringResource(R.string.exercises_positionHint),
            rows = model.positionOptions.map { OptionRow(it.value, it.label, it.countLabel) },
            ticked = positions,
            fitting = { model.filter(query, ticked, false, focus, it, tags, Zones.zones(picked)).count },
            onApply = {
                positions = it
                close()
            },
            onDismiss = close,
        )
        FilterSheet.TAGS -> OptionSheet(
            title = stringResource(R.string.exercises_tags),
            hint = stringResource(R.string.exercises_tagsHint),
            rows = model.tagOptions.map { OptionRow(it.value, it.label, it.countLabel) },
            ticked = tags,
            fitting = { model.filter(query, ticked, false, focus, positions, it, Zones.zones(picked)).count },
            onApply = {
                tags = it
                close()
            },
            onDismiss = close,
        )
        FilterSheet.ZONES -> ZonesSheet(
            figure = figure,
            language = language,
            picked = picked,
            fitting = { model.filter(query, ticked, false, focus, positions, tags, Zones.zones(it)).count },
            onApply = {
                picked = it
                close()
            },
            onDismiss = close,
        )
    }
}

/** The sheet a filter chip opens. */
private enum class FilterSheet { EQUIPMENT, FOCUS, POSITION, ZONES, TAGS }

/** Saves a set of enum values as their names. */
private inline fun <reified T : Enum<T>> enumSetSaver() =
    listSaver<Set<T>, String>(save = { set -> set.map { it.name } }, restore = { names -> names.map { enumValueOf<T>(it) }.toSet() })

private val stringSetSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })

/** The icon-only filter chip; filled, with how many are ticked, while its filter is on. */
@Composable
private fun FilterChipButton(icon: ImageVector, count: Int, name: String, tickedLabel: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val on = count > 0
    val label = if (on) tickedLabel else name
    Surface(
        selected = on,
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = if (on) colors.onPrimaryContainer else colors.surface,
        contentColor = if (on) colors.primaryContainer else colors.onSurface,
        border = if (on) null else BorderStroke(1.5.dp, colors.outline),
        modifier = Modifier.height(36.dp).semantics { contentDescription = label },
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            if (on) {
                Box(
                    Modifier.heightIn(min = 18.dp).background(colors.surface, CircleShape).padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(count.toString(), style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
                }
            }
        }
    }
}

/** One line of an option sheet: what is ticked, its name, and how many exercises it lists. */
private class OptionRow<T>(val value: T, val label: String, val countLabel: String)

/**
 * Tick what you want: the list then only offers exercises that fit. "Show N exercises"
 * counts live and applies; "Clear" removes the filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionSheet(
    title: String,
    hint: String,
    rows: List<OptionRow<T>>,
    ticked: Set<T>,
    fitting: (Set<T>) -> Int,
    onApply: (Set<T>) -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by remember { mutableStateOf(ticked) }
    val count = remember(pending) { fitting(pending) }
    // Opened in full, so "Show N exercises" is in view without scrolling.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeading(title, hint)
            Column {
                rows.forEach { row ->
                    val on = row.value in pending
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(value = on, role = Role.Checkbox) { pending = if (on) pending - row.value else pending + row.value },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Checkbox(checked = on, onCheckedChange = null)
                        Text(row.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(row.countLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            SheetActions(count, onClear = { onApply(emptySet()) }, onShow = { onApply(pending) })
        }
    }
}

/** Zones: tap the parts of the body to work on the front and back figures; left and right count together. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZonesSheet(
    figure: BodyFigure,
    language: String,
    picked: Set<String>,
    fitting: (Set<String>) -> Int,
    onApply: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by remember { mutableStateOf(picked) }
    var view by remember { mutableStateOf(BodyView.FRONT) }
    val count = remember(pending) { fitting(pending) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeading(stringResource(R.string.exercises_zones), stringResource(R.string.exercises_zonesHint))
            SideSwitch(view, onChange = { view = it })
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                BodyMapFigure(figure, view, pending, language, width = 200.dp, onToggle = { pending = Zones.toggle(pending, it) })
            }
            Text(
                Zones.summary(pending, language).ifEmpty { stringResource(R.string.exercises_zonesNone) },
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            SheetActions(count, onClear = { onApply(emptySet()) }, onShow = { onApply(pending) })
        }
    }
}

@Composable
private fun SheetHeading(title: String, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SheetActions(count: Int, onClear: () -> Unit, onShow: () -> Unit) {
    Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onClear,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Text(stringResource(R.string.exercises_filterClear), fontWeight = FontWeight.ExtraBold)
        }
        Button(onClick = onShow, modifier = Modifier.weight(1.6f).height(56.dp)) {
            Text(pluralStringResource(R.plurals.exercises_filterShow, count, count), fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ExerciseCard(row: ExerciseRow, adding: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PatternBadge(row.pattern, size = 44.dp)
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(row.details, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            if (adding) {
                Box(Modifier.size(36.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
                }
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
            }
        }
    }
}

/** A picker row: the card opens the details, and the round plus inside it adds the exercise. */
@Composable
private fun PickerCard(row: ExerciseRow, onOpen: () -> Unit, onAdd: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline),
        shape = RoundedCornerShape(PoulpyRadius.md),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .clickable(onClickLabel = stringResource(R.string.exercise_details, row.name), role = Role.Button, onClick = onOpen)
                    .padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PatternBadge(row.pattern, size = 44.dp)
                Column(Modifier.weight(1f)) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(row.details, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            // A 48 dp touch target around the 36 dp plus.
            Box(
                Modifier
                    .size(56.dp)
                    .clickable(onClickLabel = stringResource(R.string.exercise_addNamed, row.name), role = Role.Button, onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(36.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/** Poulpy's card when nothing matches: a search shows what was typed. */
@Composable
private fun NoExercises(query: String, onClear: () -> Unit) {
    PoulpyCard(
        title = if (query.isEmpty()) stringResource(R.string.exercises_noFit) else stringResource(R.string.exercises_nothingFor, query),
        body = stringResource(if (query.isEmpty()) R.string.exercises_noFitHint else R.string.exercises_nothingHint),
        button = stringResource(R.string.exercises_clearAll),
        onClick = onClear,
        modifier = Modifier.padding(top = 16.dp),
    )
}

