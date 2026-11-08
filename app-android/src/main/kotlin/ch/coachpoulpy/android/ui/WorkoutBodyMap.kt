package ch.coachpoulpy.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.BodyFigure
import ch.coachpoulpy.shared.BodyMapBox
import ch.coachpoulpy.shared.BodyView

/** The fill and edge of a muscle at [heat] 1 to 3: a little, some, most. Heat 0 is a plain outline. */
private fun heatFill(extras: PoulpyExtraColors, heat: Int): Color? = when (heat) {
    1 -> extras.heat1
    2 -> extras.heat2
    3 -> extras.heat3
    else -> null
}

private fun heatEdge(extras: PoulpyExtraColors, heat: Int): Color? = when (heat) {
    1 -> extras.heat2
    2, 3 -> extras.heat3
    else -> null
}

/**
 * One side of the body with the muscles worked lit from light to dark ([heat], 1 to 3 per area key). Areas in [hurting]
 * keep their heat and get the purple pain outline, with a halo so it reads on any fill. Only a picture: [description]
 * is what TalkBack reads for the figure.
 */
@Composable
internal fun HeatMapFigure(
    figure: BodyFigure,
    view: BodyView,
    heat: Map<String, Int>,
    hurting: Set<String>,
    width: Dp,
    modifier: Modifier = Modifier,
) {
    val regions = remember(figure, view) { drawn(figure, view) }
    val height = width * (BodyMapBox.HEIGHT / BodyMapBox.WIDTH)
    val extras = PoulpyTheme.extraColors
    val outline = MaterialTheme.colorScheme.outline
    val halo = MaterialTheme.colorScheme.background
    Canvas(modifier.size(width, height).clearAndSetSemantics { }) {
        val unit = size.width / BodyMapBox.WIDTH
        scale(unit, unit, pivot = Offset.Zero) {
            regions.forEach { r ->
                val level = r.region.key?.let { heat[it] } ?: 0
                val fill = heatFill(extras, level)
                if (fill != null && !r.region.skin) drawPath(r.path, fill)
                drawPath(r.path, heatEdge(extras, level) ?: outline, style = Stroke(width = 1.2f))
            }
            regions.filter { it.region.key in hurting }.forEach { r ->
                drawPath(r.path, halo, style = Stroke(width = 5f, join = StrokeJoin.Round))
                drawPath(r.path, extras.pain, style = Stroke(width = 3f, join = StrokeJoin.Round))
            }
        }
    }
}

/** Front and back figures side by side, one picture for TalkBack named by [description]. */
@Composable
internal fun WorkoutBodyMap(
    figure: BodyFigure,
    heat: Map<String, Int>,
    hurting: Set<String>,
    description: String,
    figureWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Row(modifier.semantics(mergeDescendants = true) { contentDescription = description }, horizontalArrangement = Arrangement.spacedBy(PoulpySpace.sm)) {
        HeatMapFigure(figure, BodyView.FRONT, heat, hurting, figureWidth)
        HeatMapFigure(figure, BodyView.BACK, heat, hurting, figureWidth)
    }
}

/** "Less ▢ ▢ ▢ More", the three heats; with [showPain] also the outlined "Hurts" swatch. */
@Composable
internal fun HeatLegend(showPain: Boolean, modifier: Modifier = Modifier) {
    val extras = PoulpyTheme.extraColors
    val tone = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.clearAndSetSemantics { }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.workout_less), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = tone)
        (1..3).forEach { Swatch(fill = heatFill(extras, it)!!, edge = heatEdge(extras, it)!!) }
        Text(stringResource(R.string.workout_more), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = tone)
        if (showPain) {
            Swatch(fill = Color.Transparent, edge = extras.pain, strokeWidth = 2.dp)
            Text(stringResource(R.string.workout_hurts), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = tone)
        }
    }
}

@Composable
private fun Swatch(fill: Color, edge: Color, strokeWidth: Dp = 1.2.dp) {
    Canvas(Modifier.size(14.dp)) {
        val stroke = strokeWidth.toPx()
        val inset = 1.dp.toPx()
        val box = Size(size.width - 2 * inset, size.height - 2 * inset)
        val corner = CornerRadius(3.dp.toPx())
        drawRoundRect(fill, Offset(inset, inset), box, corner)
        drawRoundRect(edge, Offset(inset, inset), box, corner, style = Stroke(stroke))
    }
}
