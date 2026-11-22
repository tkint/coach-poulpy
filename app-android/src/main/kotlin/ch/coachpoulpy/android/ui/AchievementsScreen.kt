package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.Badge
import ch.coachpoulpy.shared.PoulpyStatus
import java.util.Locale

@get:StringRes
internal val Badge.label: Int
    get() = when (this) {
        Badge.FIRST_RECORD -> R.string.achievements_firstRecord
        Badge.GOAL_MET -> R.string.achievements_goalMet
        Badge.FIRST_WALK -> R.string.achievements_firstWalk
    }

internal val Badge.icon: ImageVector
    get() = when (this) {
        Badge.FIRST_RECORD -> Lucide.Award
        Badge.GOAL_MET -> Lucide.Target
        Badge.FIRST_WALK -> Lucide.Footprints
    }

/** A badge as a square tile, as drawn: an earned one shows its icon and name; a locked one is only a "?", never named. */
@Composable
internal fun BadgeTile(badge: Badge?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val name = badge?.let { stringResource(it.label) } ?: stringResource(R.string.achievements_locked)
    Column(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(PoulpyRadius.lg))
            .background(if (badge != null) colors.primaryContainer else colors.surfaceVariant)
            .padding(PoulpySpace.xs)
            .semantics(mergeDescendants = true) { contentDescription = name },
        verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            badge?.icon ?: Lucide.CircleQuestionMark,
            contentDescription = null,
            tint = if (badge != null) colors.onPrimaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(30.dp),
        )
        if (badge != null) {
            Text(
                name,
                color = colors.onPrimaryContainer,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Pulls the content up by [amount], as the mockup's negative margin does: the drawing has room of its own around Poulpy. */
private fun Modifier.pullUp(amount: Dp) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val pull = amount.roundToPx()
    layout(placeable.width, placeable.height - pull) { placeable.place(0, -pull) }
}

/** Achievements, opened from the motivation row on Home (`achievements` mockup): Poulpy's tentacles, then the badges. */
@Composable
fun AchievementsScreen(graph: AppGraph, language: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph, language) { graph.achievements(language, AppPreferences(context)) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    SubPage(stringResource(R.string.achievements_title), null, onBack, modifier, topPadding = 0.dp, spacing = PoulpySpace.md) {
        state.poulpy?.let { poulpy ->
            Column(
                Modifier.fillMaxWidth().pullUp(18.dp).padding(bottom = PoulpySpace.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PoulpySpace.xs),
            ) {
                // Poulpy stays big and plays his move when tapped, as on Home.
                PoulpyTapArt(poulpy.drawing, poulpy.title, 200.dp)
                Text(poulpy.litCaption, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(
                    if (state.sessionsToFull == 0) stringResource(R.string.achievements_champion)
                    else pluralStringResource(R.plurals.achievements_toGo, state.sessionsToFull, state.sessionsToFull),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = PoulpySpace.sm).clearAndSetSemantics { }) {
                    repeat(PoulpyStatus.TENTACLES) { index ->
                        Box(
                            Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (index < poulpy.tentaclesLit) colors.primary else colors.onPrimaryContainer.copy(alpha = 0.18f)),
                        )
                    }
                }
            }
        }
        Text(
            stringResource(R.string.achievements_badges).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = PoulpySpace.xs, top = 6.dp),
        )
        // Earned badges first, then the locked ones as "?", as drawn.
        val earned = state.achievements.earnedInOrder
        val tiles = earned + state.achievements.locked
        tiles.chunked(3).forEach { rowTiles ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
                rowTiles.forEach { badge -> BadgeTile(badge.takeIf { it in earned }, Modifier.weight(1f)) }
                repeat(3 - rowTiles.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
