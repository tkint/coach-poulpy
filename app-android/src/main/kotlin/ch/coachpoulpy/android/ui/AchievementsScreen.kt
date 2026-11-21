package ch.coachpoulpy.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.Badge
import ch.coachpoulpy.shared.PoulpyStatus

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

/** A badge as a square tile: earned ones show their icon and name; a locked one is only a "?", never named. */
@Composable
internal fun BadgeTile(badge: Badge?, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val name = badge?.let { stringResource(it.label) } ?: stringResource(R.string.achievements_locked)
    Column(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(PoulpyRadius.lg))
            .background(if (badge != null) colors.primaryContainer else colors.surface)
            .border(1.dp, if (badge != null) colors.primary else colors.outline, RoundedCornerShape(PoulpyRadius.lg))
            .padding(PoulpySpace.sm)
            .semantics(mergeDescendants = true) { contentDescription = name },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            badge?.icon ?: Lucide.CircleQuestionMark,
            contentDescription = null,
            tint = if (badge != null) colors.onPrimaryContainer else colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        if (badge != null) {
            Text(name, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Achievements, opened from the motivation row on Home: Poulpy's tentacles, then the badges. */
@Composable
fun AchievementsScreen(graph: AppGraph, language: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph, language) { graph.achievements(language, AppPreferences(context)) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    SubPage(stringResource(R.string.achievements_title), null, onBack, modifier) {
        state.poulpy?.let { poulpy ->
            Column(Modifier.fillMaxWidth().padding(vertical = PoulpySpace.sm), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
                Image(painterResource(poulpy.drawing.drawable), contentDescription = null, modifier = Modifier.size(200.dp))
                Text(poulpy.tentaclesCaption, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(
                    if (state.sessionsToFull == 0) stringResource(R.string.achievements_champion)
                    else pluralStringResource(R.plurals.achievements_toGo, state.sessionsToFull, state.sessionsToFull),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    repeat(PoulpyStatus.TENTACLES) { index ->
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (index < poulpy.tentaclesLit) colors.primary else colors.onPrimaryContainer.copy(alpha = 0.18f)),
                        )
                    }
                }
            }
        }
        Text(
            stringResource(R.string.achievements_badges),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = PoulpySpace.sm),
        )
        val tiles = state.achievements.earned.reversed() + state.achievements.locked
        tiles.chunked(3).forEach { rowTiles ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTiles.forEach { badge ->
                    // A locked tile is drawn without a name: the badge itself stays hidden.
                    BadgeTile(badge.takeIf { it in state.achievements.earned }, Modifier.weight(1f))
                }
                repeat(3 - rowTiles.size) { Box(Modifier.weight(1f).height(0.dp)) }
            }
        }
    }
}
