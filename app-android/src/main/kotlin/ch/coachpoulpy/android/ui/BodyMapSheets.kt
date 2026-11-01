package ch.coachpoulpy.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.PainDuration
import ch.coachpoulpy.shared.BodyMap
import ch.coachpoulpy.shared.BodyMapBox
import ch.coachpoulpy.shared.BodyMapShapes
import ch.coachpoulpy.shared.BodyRegion
import ch.coachpoulpy.shared.BodyView

/** The shapes of one figure and view, parsed once: an area's path in the 120 × 268 box, and its key. */
private class DrawnRegion(val region: BodyRegion, val path: Path)

private fun drawn(figure: BodyFigure, view: BodyView): List<DrawnRegion> =
    BodyMapShapes.regions(figure, view).map { DrawnRegion(it, PathParser().parsePathString(it.path).toPath()) }

/**
 * One side of the body as a muscle chart: every area is a shape, filled with the primary colour when it is in [selected].
 * Skin has no fill. With [onToggle] each area can be tapped, and has its own accessibility node named after it
 * ("Left knee"); without it the figure is only a picture.
 */
@Composable
internal fun BodyMapFigure(
    figure: BodyFigure,
    view: BodyView,
    selected: Set<String>,
    language: String,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 140.dp,
    onToggle: ((String) -> Unit)? = null,
) {
    val regions = remember(figure, view) { drawn(figure, view) }
    val height = width * (BodyMapBox.HEIGHT / BodyMapBox.WIDTH)
    val fill = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val lit = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val unit = with(density) { width.toPx() } / BodyMapBox.WIDTH

    Box(modifier.size(width, height)) {
        Canvas(
            Modifier
                .size(width, height)
                .pointerInput(regions, onToggle) {
                    if (onToggle != null) {
                        detectTapGestures { at ->
                            // The last area drawn is on top.
                            regions.lastOrNull { it.region.key != null && hit(it.path, at / unit) }?.region?.key?.let(onToggle)
                        }
                    }
                },
        ) {
            scale(unit, unit, pivot = Offset.Zero) {
                regions.forEach { r ->
                    val on = r.region.key in selected
                    when {
                        on -> drawPath(r.path, lit)
                        !r.region.skin -> drawPath(r.path, fill)
                    }
                    drawPath(r.path, if (on) lit else outline, style = Stroke(width = 1.2f))
                }
            }
        }
        if (onToggle != null) {
            // One node per area for TalkBack, over the area's bounds; sighted taps are handled by the canvas above.
            regions.filter { it.region.key != null }.forEach { r ->
                val key = r.region.key!!
                val isOn = key in selected
                val b = r.path.getBounds()
                Box(
                    Modifier
                        .offset((b.left * unit / density.density).dp, (b.top * unit / density.density).dp)
                        .size((b.width * unit / density.density).dp, (b.height * unit / density.density).dp)
                        .clearAndSetSemantics {
                            role = Role.Checkbox
                            contentDescription = BodyMap.label(key, language)
                            this.selected = isOn
                            onClick { onToggle(key); true }
                        },
                )
            }
        }
    }
}

/** Whether [point] (in the 120 × 268 box) is inside [path]. */
private fun hit(path: Path, point: Offset): Boolean {
    val native = path.asAndroidPath()
    val bounds = android.graphics.RectF()
    native.computeBounds(bounds, true)
    val clip = android.graphics.Region(bounds.left.toInt() - 1, bounds.top.toInt() - 1, bounds.right.toInt() + 1, bounds.bottom.toInt() + 1)
    val region = android.graphics.Region()
    region.setPath(native, clip)
    return region.contains(point.x.toInt(), point.y.toInt())
}

/** Front or back of the figure, as a two-way switch above it. */
@Composable
private fun SideSwitch(view: BodyView, onChange: (BodyView) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectableGroup()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(BodyView.FRONT to R.string.profile_figureFront, BodyView.BACK to R.string.profile_figureBack).forEach { (side, label) ->
            val on = side == view
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(50))
                    .selectable(selected = on, role = Role.RadioButton, onClick = { onChange(side) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(label),
                    fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Pain or rehab: where it hurts on the front and back figures, then for how long. */
@Composable
internal fun PainSheet(
    figure: BodyFigure,
    language: String,
    areas: List<String>,
    duration: PainDuration?,
    onSave: (List<String>, PainDuration) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var picked by remember { mutableStateOf(areas.toSet()) }
    var length by remember { mutableStateOf(duration ?: PainDuration.HOURS_72) }
    var view by remember { mutableStateOf(BodyView.FRONT) }
    val toggle = { key: String -> picked = if (key in picked) picked - key else picked + key }
    val summary = BodyMap.valid(picked).joinToString(", ") { BodyMap.label(it, language) }

    SheetFrame(
        title = stringResource(R.string.profile_pain),
        why = stringResource(R.string.profile_painWhy),
        onDismiss = onDismiss,
        onSave = { onSave(BodyMap.valid(picked), length) },
        clear = if (areas.isEmpty()) null else onClear,
    ) {
        Text(stringResource(R.string.profile_painWhere), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Note(stringResource(R.string.profile_painHint))
        SideSwitch(view, onChange = { view = it })
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BodyMapFigure(figure, view, picked, language, width = 220.dp, onToggle = toggle)
        }
        Text(
            summary.ifEmpty { stringResource(R.string.profile_painNothingPicked) },
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        FilterChip(
            selected = BodyMap.OTHER in picked,
            onClick = { toggle(BodyMap.OTHER) },
            label = { Text(BodyMap.label(BodyMap.OTHER, language), fontWeight = FontWeight.ExtraBold) },
        )
        Text(stringResource(R.string.profile_painFor), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.selectableGroup().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))) {
            listOf(
                Triple(PainDuration.HOURS_72, R.string.profile_painDuration72, R.string.profile_painDuration72Sub),
                Triple(PainDuration.ONE_WEEK, R.string.profile_painDurationWeek, null),
                Triple(PainDuration.TWO_WEEKS, R.string.profile_painDurationWeeks, null),
                Triple(PainDuration.UNTIL_CLEARED, R.string.profile_painDurationCleared, R.string.profile_painDurationClearedSub),
            ).forEachIndexed { index, (value, title, sub) ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .selectable(selected = value == length, role = Role.RadioButton, onClick = { length = value })
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        sub?.let { Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    RadioButton(selected = value == length, onClick = null)
                }
            }
        }
        Note(stringResource(R.string.profile_painAgain))
    }
}

/** The body figure: two builds side by side, drawn as the map will look. Only a drawing, so no skin tone, no sex. */
@Composable
internal fun BodyFigureSheet(
    figure: BodyFigure?,
    language: String,
    onSave: (BodyFigure?) -> Unit,
    onDismiss: () -> Unit,
) {
    var choice by remember { mutableStateOf(figure) }
    SheetFrame(
        title = stringResource(R.string.profile_bodyFigure),
        why = stringResource(R.string.profile_figureWhy),
        onDismiss = onDismiss,
        onSave = { onSave(choice) },
        clear = if (figure == null) null else ({ onSave(null) }),
    ) {
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(BodyFigure.HIPS to R.string.profile_figureHips, BodyFigure.SHOULDERS to R.string.profile_figureShoulders).forEach { (value, label) ->
                val on = choice == value
                Column(
                    Modifier
                        .weight(1f)
                        .border(if (on) 2.5.dp else 1.5.dp, if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .selectable(selected = on, role = Role.RadioButton, onClick = { choice = value })
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BodyMapFigure(value, BodyView.FRONT, emptySet(), language, width = 96.dp)
                    Text(stringResource(label), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
