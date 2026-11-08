package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.core.model.PartKind

/**
 * A part as an outlined block, its icon, name and summary on the top border's left and its [time] on the right
 * (`session-recap-*` and `workout-page-*` mockups); [content] is its rows, which the caller separates.
 */
@Composable
internal fun PartBlock(kind: PartKind, name: String, summary: String, time: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    var headerWidth by remember { mutableIntStateOf(0) }
    var timeWidth by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxWidth().padding(top = 13.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                // The border breaks around the title and the time, so neither needs a background of its own.
                .drawBehind {
                    val stroke = 1.dp.toPx()
                    val gap = PoulpySpace.sm.toPx()
                    val top = -13.dp.toPx()
                    val bottom = 13.dp.toPx()
                    clipRect(gap, top, gap + headerWidth, bottom, ClipOp.Difference) {
                        clipRect(size.width - gap - timeWidth, top, size.width - gap, bottom, ClipOp.Difference) {
                            drawRoundRect(
                                color = colors.outline,
                                topLeft = Offset(stroke / 2, stroke / 2),
                                size = Size(size.width - stroke, size.height - stroke),
                                cornerRadius = CornerRadius(PoulpyRadius.sm.toPx()),
                                style = Stroke(stroke),
                            )
                        }
                    }
                }
                .padding(start = 10.dp, end = 10.dp, top = PoulpySpace.md),
        ) {
            content()
        }
        val work = kind == PartKind.WORK
        Row(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = PoulpySpace.sm, y = (-13).dp)
                .onSizeChanged { headerWidth = it.width }
                .padding(horizontal = 6.dp)
                .semantics(mergeDescendants = true) { heading() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(if (work) colors.primary else colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(kind.icon, contentDescription = null, tint = if (work) colors.onPrimary else colors.onPrimaryContainer, modifier = Modifier.size(14.dp))
            }
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = colors.primary, maxLines = 1)
            Text(
                summary,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (time.isNotEmpty()) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -PoulpySpace.sm, y = (-9).dp)
                    .onSizeChanged { timeWidth = it.width }
                    .padding(horizontal = 6.dp),
            )
        }
    }
}
