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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R

/**
 * The filter row both Library lists share: the pill search, a row of [chips], then the
 * count line with [action] at its right ("Clear filters", or the workouts' sort).
 */
@Composable
fun ListFilters(
    query: String,
    onQuery: (String) -> Unit,
    placeholder: String,
    label: String,
    countLabel: String,
    chips: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(modifier) {
        PillSearchField(
            query = query,
            onQuery = onQuery,
            placeholder = placeholder,
            label = label,
            modifier = Modifier.padding(start = PoulpySpace.lg, end = PoulpySpace.lg, top = 8.dp, bottom = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = PoulpySpace.lg),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = chips,
        )
        Row(
            Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                countLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            )
            action()
        }
    }
}

/**
 * What a list or screen shows when nothing exists yet or nothing matches: Poulpy thinking beside a title and a line, in
 * a dashed outline with no fill, then one small secondary button (the `EmptyState` mockup).
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    button: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonIcon: ImageVector? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .dashedBorder(colors.outline, PoulpyRadius.lg)
            .padding(PoulpySpace.md),
        horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md),
    ) {
        Image(painterResource(R.drawable.poulpy_pose_thinking), contentDescription = null, modifier = Modifier.size(width = 64.dp, height = 58.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            PoulpyButton(onClick = onClick, kind = PoulpyButtonKind.Secondary, size = PoulpyButtonSize.Sm, modifier = Modifier.padding(top = PoulpySpace.sm)) {
                if (buttonIcon != null) Icon(buttonIcon, contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(16.dp))
                Text(button)
            }
        }
    }
}

/** A 2 dp dashed outline with rounded corners, drawn behind the content. */
private fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    val width = 2.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(width / 2, width / 2),
        size = Size(size.width - width, size.height - width),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = width, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}

/** The 44 high pill search with a 1.5 px outline, `primary` once something is typed, and a clear button. */
@Composable
fun PillSearchField(query: String, onQuery: (String) -> Unit, placeholder: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(
        modifier
            .fillMaxWidth()
            .height(PoulpySize.searchField)
            .background(colors.surface, pill)
            .border(1.5.dp, if (query.isEmpty()) colors.outline else colors.primary, pill)
            .padding(start = 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Lucide.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
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
                        Lucide.X,
                        contentDescription = stringResource(R.string.exercises_clearSearch),
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
        modifier = modifier.height(PoulpySize.chip),
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold)
        }
    }
}
