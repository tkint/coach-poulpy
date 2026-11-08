package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.core.model.PartKind
import ch.coachpoulpy.shared.RecapPart
import ch.coachpoulpy.shared.RecapRow
import ch.coachpoulpy.shared.RecapTile
import ch.coachpoulpy.shared.SessionRecap
import java.util.Locale

/**
 * What opening a finished session shows (`session-recap-*` mockups): its name and date, the figures, then each part
 * with the exercises done in it. Everything is decided in the shared [SessionRecap]; this only draws it.
 * The body map half of the opening view is not drawn yet.
 */
@Composable
internal fun SessionRecapContent(recap: SessionRecap, dateLine: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.md),
        verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(recap.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
            Text(dateLine, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        recap.tiles.chunked(3).forEach { line ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
                line.forEach { Tile(it, Modifier.weight(1f)) }
                repeat(3 - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (recap.parts.isNotEmpty()) {
            Text(
                recap.listTitle.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = PoulpySpace.xs, top = PoulpySpace.sm),
            )
            recap.parts.forEach { PartBlock(it) }
        }
    }
}

@Composable
private fun Tile(tile: RecapTile, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.clip(RoundedCornerShape(PoulpyRadius.sm)).background(colors.surfaceVariant).padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(tile.label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(tile.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

/** A part as an outlined block, its icon and name sitting on the top border; the unnamed block of steps has no header. */
@Composable
private fun PartBlock(part: RecapPart) {
    val colors = MaterialTheme.colorScheme
    val named = part.kind != null
    Box(Modifier.fillMaxWidth().padding(top = if (named) 13.dp else 0.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .border(1.dp, colors.outline, RoundedCornerShape(PoulpyRadius.sm))
                .padding(start = 10.dp, end = 10.dp, top = if (named) PoulpySpace.md else 0.dp),
        ) {
            part.rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = colors.outline)
                RecapRowView(row)
            }
        }
        part.kind?.let { kind ->
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = PoulpySpace.sm, y = (-13).dp)
                    .background(colors.surface)
                    .padding(horizontal = 6.dp)
                    .semantics(mergeDescendants = true) { heading() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val work = kind == PartKind.WORK
                Box(
                    Modifier.size(24.dp).clip(CircleShape).background(if (work) colors.primary else colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(kind.icon, contentDescription = null, tint = if (work) colors.onPrimary else colors.onPrimaryContainer, modifier = Modifier.size(14.dp))
                }
                Text(part.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = colors.primary, maxLines = 1)
                Text(
                    part.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecapRowView(row: RecapRow) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PatternBadge(row.pattern ?: MovementPattern.CORE, size = PoulpySize.avatarSmall)
        Column(Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            if (row.detail.isNotEmpty()) {
                Text(row.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
