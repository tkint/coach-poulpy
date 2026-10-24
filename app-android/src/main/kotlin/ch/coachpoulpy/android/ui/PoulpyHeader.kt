package ch.coachpoulpy.android.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.PoulpyStatus

/**
 * Poulpy in the middle of his eight tentacles, with what he has to say. [compact] puts
 * the ring beside the text, as on Home, where the day's workouts need the room.
 */
@Composable
fun PoulpyHeader(status: PoulpyStatus, modifier: Modifier = Modifier, compact: Boolean = false) {
    val ember = !status.blueSide
    val container by animateColorAsState(
        if (ember) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
        tween(PoulpyMotion.moodMillis),
        label = "header",
    )
    val ring: @Composable (Dp, Dp) -> Unit = { side, artSize ->
        Box(Modifier.size(side), contentAlignment = Alignment.Center) {
            TentacleRing(
                lit = status.tentaclesLit,
                on = if (ember) PoulpyTheme.extraColors.grumpy else MaterialTheme.colorScheme.primary,
                off = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxSize().semantics { contentDescription = status.tentaclesCaption },
            )
            Crossfade(status.drawing, animationSpec = tween(PoulpyMotion.moodMillis), label = "poulpy") { drawing ->
                Image(painterResource(drawing.drawable), contentDescription = status.title, modifier = Modifier.size(artSize))
            }
        }
    }
    Card(modifier, colors = CardDefaults.cardColors(containerColor = container)) {
        if (compact) {
            Row(
                Modifier.fillMaxWidth().padding(start = PoulpySpace.md, end = PoulpySpace.lg, top = PoulpySpace.md, bottom = PoulpySpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ring(104.dp, 72.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(status.title, style = MaterialTheme.typography.titleLarge)
                    Text(status.line, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(status.tentaclesCaption, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 2.dp))
                }
            }
        } else {
            Column(
                Modifier.fillMaxWidth().padding(PoulpySpace.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
            ) {
                ring(208.dp, 150.dp)
                Text(status.title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(status.line, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Text(
                    status.tentaclesCaption,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Eight rounded segments clockwise from the top, with clear gaps so each tentacle reads on its own. */
@Composable
private fun TentacleRing(lit: Int, on: Color, off: Color, modifier: Modifier = Modifier) {
    val litColor by animateColorAsState(on, tween(PoulpyMotion.moodMillis), label = "ring")
    Canvas(modifier) {
        val width = size.minDimension * 15f / 200f
        val inset = width / 2
        for (i in 0 until PoulpyStatus.TENTACLES) {
            drawArc(
                color = if (i < lit) litColor else off,
                // Compose measures from 3 o'clock; the ring starts at 12.
                startAngle = i * 45f + 11f - 90f,
                sweepAngle = 23f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - width, size.height - width),
                style = Stroke(width = width, cap = StrokeCap.Round),
            )
        }
    }
}

private val PoulpyDrawing.drawable: Int
    @DrawableRes get() = when (this) {
        PoulpyDrawing.MOOD_PROUD -> R.drawable.poulpy_mood_proud
        PoulpyDrawing.MOOD_HAPPY -> R.drawable.poulpy_mood_happy
        PoulpyDrawing.MOOD_RECOVERING -> R.drawable.poulpy_mood_recovering
        PoulpyDrawing.MOOD_ANNOYED -> R.drawable.poulpy_mood_annoyed
        PoulpyDrawing.MOOD_GRUMPY -> R.drawable.poulpy_mood_grumpy
        PoulpyDrawing.MOOD_FUMING -> R.drawable.poulpy_mood_fuming
        PoulpyDrawing.COCKY -> R.drawable.poulpy_pose_cocky
        PoulpyDrawing.STRAINING -> R.drawable.poulpy_pose_straining
        PoulpyDrawing.CHAMPION -> R.drawable.poulpy_pose_champion
        PoulpyDrawing.STANDING_TALL -> R.drawable.poulpy_pose_standing_tall
        PoulpyDrawing.EXHAUSTED -> R.drawable.poulpy_pose_exhausted
        PoulpyDrawing.SIDE_GLANCE -> R.drawable.poulpy_pose_side_glance
        PoulpyDrawing.SLEEPING -> R.drawable.poulpy_pose_sleeping
        PoulpyDrawing.PATCHED_UP -> R.drawable.poulpy_pose_patched_up
        PoulpyDrawing.SHY -> R.drawable.poulpy_pose_shy
        PoulpyDrawing.THINKING -> R.drawable.poulpy_pose_thinking
        PoulpyDrawing.POUTING -> R.drawable.poulpy_pose_pouting
        PoulpyDrawing.FUMING -> R.drawable.poulpy_pose_fuming
    }
