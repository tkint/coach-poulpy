package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.Equipment
import ch.coachpoulpy.core.model.WorkoutType
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.EquipmentOption
import ch.coachpoulpy.shared.ExerciseRow
import ch.coachpoulpy.shared.MuscleArea

/**
 * The searchable catalog, used by the library and by the builder's picker: a pill
 * search, muscle chips, the count line, then card rows grouped by movement pattern,
 * or ranked results while searching. In the picker ([adding]) each row ends with a
 * round plus instead of a chevron, and recently used exercises come first.
 */
@Composable
fun ExerciseList(
    graph: AppGraph,
    language: String,
    type: WorkoutType?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    adding: Boolean = false,
) {
    val model = remember(graph, language) { graph.exerciseList(language) }
    val context = LocalContext.current
    val equipment = remember(graph, context) { graph.equipmentFilter(AppPreferences(context)) }
    var query by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf<MuscleArea?>(null) }
    var ticked by remember(equipment) { mutableStateOf(equipment.ticked) }
    var choosing by rememberSaveable { mutableStateOf(false) }
    val listing = remember(model, query, area, type, ticked) { model.filter(query, area, type, ticked, withRecent = adding) }
    val tick = { value: Set<Equipment>? ->
        equipment.ticked = value
        ticked = equipment.ticked
    }
    val clearFilters = {
        area = null
        tick(null)
    }
    val clearAll = {
        query = ""
        clearFilters()
    }

    Column(modifier.fillMaxSize()) {
        PillSearchField(
            query = query,
            onQuery = { query = it },
            placeholder = stringResource(R.string.exercises_search),
            label = stringResource(R.string.exercises_search_label),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { EquipmentChip(ticked, onClick = { choosing = true }) }
            item { VerticalDivider(Modifier.padding(vertical = 6.dp).height(24.dp), color = MaterialTheme.colorScheme.outline) }
            item { PoulpyChip(selected = area == null, onClick = { area = null }, label = stringResource(R.string.exercises_all)) }
            items(model.muscleFilters, key = { it.area.name }) { filter ->
                PoulpyChip(
                    selected = area == filter.area,
                    onClick = { area = if (area == filter.area) null else filter.area },
                    label = filter.label,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                listing.countLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (area != null || ticked != null) {
                TextButton(onClick = clearFilters) {
                    Text(stringResource(R.string.exercises_clear_filters), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
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
                    ExerciseCard(row, adding, onClick = { onPick(row.slug) })
                }
            }
            if (listing.count == 0) {
                item(key = "empty") { NoExercises(query.trim(), onClear = clearAll) }
            }
        }
    }
    if (choosing) {
        EquipmentSheet(
            options = model.equipmentOptions,
            ticked = ticked,
            fitting = { model.filter(query, area, type, it).count },
            onApply = {
                tick(it)
                choosing = false
            },
            onDismiss = { choosing = false },
        )
    }
}

/** The icon-only Equipment chip; filled, with how many items are ticked, while the filter is on. */
@Composable
private fun EquipmentChip(ticked: Set<Equipment>?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val on = ticked != null
    val label = if (ticked == null) {
        stringResource(R.string.exercises_equipment)
    } else {
        pluralStringResource(R.plurals.exercises_equipment_ticked, ticked.size, ticked.size)
    }
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
            Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(20.dp))
            if (ticked != null) {
                Box(
                    Modifier.heightIn(min = 18.dp).background(colors.surface, CircleShape).padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ticked.size.toString(), style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
                }
            }
        }
    }
}

/**
 * Tick what you own: the list then only offers exercises it fits. "Show N exercises"
 * counts live and applies; "Clear" removes the filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentSheet(
    options: List<EquipmentOption>,
    ticked: Set<Equipment>?,
    fitting: (Set<Equipment>) -> Int,
    onApply: (Set<Equipment>?) -> Unit,
    onDismiss: () -> Unit,
) {
    var pending by remember { mutableStateOf(ticked ?: emptySet()) }
    val count = remember(pending) { fitting(pending) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.exercises_equipment), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.exercises_equipment_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                options.forEach { option ->
                    val on = option.equipment in pending
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .toggleable(value = on, role = Role.Checkbox) { pending = if (on) pending - option.equipment else pending + option.equipment },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Checkbox(checked = on, onCheckedChange = null)
                        Text(option.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(option.countLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onApply(null) },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Text(stringResource(R.string.exercises_equipment_clear), fontWeight = FontWeight.ExtraBold)
                }
                Button(onClick = { onApply(pending) }, modifier = Modifier.weight(1.6f).height(56.dp)) {
                    Text(pluralStringResource(R.plurals.exercises_equipment_show, count, count), fontWeight = FontWeight.ExtraBold)
                }
            }
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

/** Poulpy, happy on the blue side, when nothing matches: a search shows what was typed. */
@Composable
private fun NoExercises(query: String, onClear: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .background(colors.primaryContainer, RoundedCornerShape(PoulpyRadius.lg))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(painterResource(R.drawable.poulpy_mood_happy), contentDescription = null, modifier = Modifier.size(96.dp))
        Text(
            if (query.isEmpty()) stringResource(R.string.exercises_no_fit) else stringResource(R.string.exercises_nothing_for, query),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(if (query.isEmpty()) R.string.exercises_no_fit_hint else R.string.exercises_nothing_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onClear, modifier = Modifier.padding(top = 8.dp).height(48.dp)) {
            Text(stringResource(R.string.exercises_clear_all), fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** The 48 high pill search with a 1.5 px outline, `primary` once something is typed, and a clear button. */
@Composable
fun PillSearchField(query: String, onQuery: (String) -> Unit, placeholder: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colors.surface, pill)
            .border(1.5.dp, if (query.isEmpty()) colors.outline else colors.primary, pill)
            .padding(start = 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQuery("") }, modifier = Modifier.size(44.dp)) {
                Box(Modifier.size(36.dp).background(colors.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.exercises_clear_search),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** The design-system chip: 36 high, pill; on = `primary`, off = 1.5 px `outline`. */
@Composable
fun PoulpyChip(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = if (selected) null else BorderStroke(1.5.dp, colors.outline),
        modifier = modifier.height(36.dp),
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold)
        }
    }
}
