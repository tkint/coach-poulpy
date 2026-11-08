package ch.coachpoulpy.android.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

private const val STOP_HALF = 1
private const val STOP_FULL = 2

/** The height of the sheet at its lowest stop: the handle, a title and the action bar. */
private val SHEET_PEEK = 196.dp

/** How far a pull down from the top of the content goes before the sheet starts to follow it. */
private val PULL_DEAD_ZONE = 56.dp

/** How much of a fling's speed counts when picking where the sheet rests: little, so only a wide drag changes the stop. */
private const val FLING_SECONDS = 0.04f

/**
 * A sheet over a full-height background (the body map of the workout page and the session recap), resting at three stops: peek shows its top, half
 * ([halfShare] of the screen left to the background) is the default, full covers the background and scrolls.
 * The handle drags or taps between stops; below full, dragging anywhere on the sheet moves it; at full, scrolling down
 * past the top of the content drags the sheet back down. [background] gets the room above the sheet, never less than
 * at the half stop, so what it draws does not shrink when the sheet goes up.
 */
@Composable
internal fun DetailSheet(
    showBackgroundLabel: String,
    showDetailsLabel: String,
    background: @Composable BoxScope.(room: Dp) -> Unit,
    floating: @Composable BoxScope.() -> Unit,
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    halfShare: Float = 0.46f,
    content: @Composable ColumnScope.() -> Unit,
) {
    var stop by rememberSaveable { mutableStateOf(STOP_HALF) }
    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val density = LocalDensity.current
        val total = with(density) { maxHeight.toPx() }
        val halfTop = total * halfShare
        val peekTop = total - with(density) { (SHEET_PEEK + WindowInsets.navigationBars.getBottom(density).toDp()).toPx() }
        val stops = listOf(peekTop, halfTop, 0f)
        val top = remember { Animatable(halfTop) }
        val scope = rememberCoroutineScope()
        val scroll = rememberScrollState()
        val expanded = stop == STOP_FULL
        LaunchedEffect(stop, total) { top.animateTo(stops[stop]) }

        fun moveBy(delta: Float): Float {
            val target = (top.value + delta).coerceIn(0f, peekTop)
            val moved = target - top.value
            scope.launch(start = CoroutineStart.UNDISPATCHED) { top.snapTo(target) }
            return moved
        }

        /** Rests at the stop nearest to where the sheet was heading; the same halfway rule in both directions. */
        fun settle(velocity: Float) {
            val heading = top.value + velocity * FLING_SECONDS
            stop = stops.indices.minBy { abs(stops[it] - heading) }
            scope.launch { top.animateTo(stops[stop]) }
        }

        val drag = rememberDraggableState { delta -> moveBy(delta) }
        val dragging = Modifier.draggable(drag, Orientation.Vertical, onDragStopped = { settle(it) })

        // At full height the content scrolls; a pull down once it is at the top lowers the sheet, and a pull up
        // raises it again before the content scrolls.
        val deadZone = with(density) { PULL_DEAD_ZONE.toPx() }
        val connection = remember(scroll, deadZone) {
            object : NestedScrollConnection {
                // How far the content has been pulled down past its top in this gesture.
                private var pulled = 0f

                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y >= 0f) return Offset.Zero
                    pulled = (pulled + available.y).coerceAtLeast(0f)
                    return if (top.value > 0f) Offset(0f, moveBy(available.y)) else Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (available.y <= 0f) return Offset.Zero
                    pulled += available.y
                    // The sheet only follows once the pull is past the dead zone.
                    val excess = (pulled - deadZone).coerceAtLeast(0f)
                    return Offset(0f, moveBy(minOf(available.y, excess)))
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    pulled = 0f
                    if (top.value > 0f && stop == STOP_FULL) settle(available.y)
                    return Velocity.Zero
                }
            }
        }

        val statusBar = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
        // The background keeps the room it has at the half stop once the sheet goes higher.
        background(with(density) { maxOf(top.value, halfTop).toDp() })

        val full = (1f - top.value / halfTop).coerceIn(0f, 1f)
        val corner = PoulpyRadius.lg * (1f - full)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = corner, topEnd = corner),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.layout { measurable, constraints ->
                val height = (total - top.value).roundToInt().coerceAtLeast(0)
                val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                layout(placeable.width, constraints.maxHeight) { placeable.place(0, top.value.roundToInt()) }
            },
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .then(dragging)
                        .clickable(onClickLabel = if (expanded) showBackgroundLabel else showDetailsLabel) { stop = if (expanded) STOP_HALF else STOP_FULL }
                        .padding(top = statusBar * full)
                        .height(PoulpySize.touch),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(width = 40.dp, height = 5.dp).clip(RoundedCornerShape(PoulpyRadius.pill)).background(MaterialTheme.colorScheme.outline))
                }
                Column(
                    Modifier
                        .weight(1f)
                        .clipToBounds()
                        // Always measured at its natural height, so its items never squash while the sheet moves.
                        .then(if (expanded) Modifier.nestedScroll(connection) else dragging)
                        .verticalScroll(scroll, enabled = expanded)
                        .padding(horizontal = PoulpySpace.lg),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
                if (actions != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    actions()
                }
            }
        }
        floating()
    }
}
