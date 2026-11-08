package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.core.model.MovementPattern
import ch.coachpoulpy.shared.RecapRow
import ch.coachpoulpy.shared.RecapTile
import ch.coachpoulpy.shared.SessionRecap
import java.util.Locale

/**
 * What opening a finished session shows (`session-recap-*` mockups): its name and date, the figures, then each part
 * with the exercises done in it. Everything is decided in the shared [SessionRecap]; this only draws it.
 * A session with muscles worked puts this in a sheet over the body map ([SessionRecapSheet]); without, it is the page.
 */
@Composable
internal fun SessionRecapContent(recap: SessionRecap, dateLine: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.md),
        verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
    ) {
        RecapDetails(recap, dateLine)
    }
}

/** The muscles worked on a body map, and over it the sheet you drag up for the session's figures and exercises. */
@Composable
internal fun SessionRecapSheet(recap: SessionRecap, dateLine: String, figure: BodyFigure, onBack: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    DetailSheet(
        showBackgroundLabel = stringResource(R.string.workout_showMap),
        showDetailsLabel = stringResource(R.string.workout_showExercises),
        background = { room ->
            HeatMapPanel(
                room, figure, recap.muscles.heat, emptySet(),
                description = stringResource(R.string.workout_mapLabel, recap.muscles.loads.joinToString(", ") { it.label }),
            )
        },
        floating = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.sm), horizontalArrangement = Arrangement.SpaceBetween) {
                FloatingButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
                    FloatingButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.shareSession_title))
                    }
                    FloatingButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
        },
    ) {
        RecapDetails(recap, dateLine)
    }
}

@Composable
private fun ColumnScope.RecapDetails(recap: SessionRecap, dateLine: String) {
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
        recap.parts.forEach { part ->
            PartBlock(part.kind, part.name, part.summary, part.time) {
                part.rows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    RecapRowView(row)
                }
            }
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
        if (row.time.isNotEmpty()) {
            Text(row.time, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}
