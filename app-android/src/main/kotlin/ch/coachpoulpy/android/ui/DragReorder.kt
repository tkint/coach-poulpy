package ch.coachpoulpy.android.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Drags an item by its key: the item follows the finger, and once its middle passes over another
 * item that [canDropOn] accepts for it, [onMove] moves it there. [onMove] answers whether the order
 * changed, which plays a light tick. Each item records where it sits on screen, so an item can be a
 * list item or a step nested inside one, like a step of a repeat.
 */
@Stable
class DragReorder internal constructor(
    private val canDropOn: (key: Any, onto: Any) -> Boolean,
    private val onMove: (key: Any, toKey: Any) -> Boolean,
) {
    private class Frame(val top: Float, val height: Int)

    var key by mutableStateOf<Any?>(null)
        private set
    private val frames = mutableStateMapOf<Any, Frame>()
    private var start = 0f
    private var delta by mutableFloatStateOf(0f)
    private var viewportTop = 0f
    private var viewportBottom = 0f

    /** How far the dragged item is drawn from where its layout puts it. */
    val offset: Float get() = key?.let { frames[it] }?.let { start + delta - it.top } ?: 0f

    internal fun place(key: Any, top: Float, height: Int) {
        frames[key] = Frame(top, height)
    }

    internal fun forget(key: Any) {
        frames.remove(key)
    }

    internal fun start(key: Any) {
        val frame = frames[key] ?: return
        this.key = key
        start = frame.top
        delta = 0f
    }

    /** Follows the finger by [dy]; true when the item moved past another. */
    internal fun drag(dy: Float): Boolean {
        delta += dy
        return swap()
    }

    /** Where the scrolling list sits on screen, so a drag near its top or bottom edge can scroll it. */
    internal fun viewport(top: Float, bottom: Float) {
        viewportTop = top
        viewportBottom = bottom
    }

    /**
     * How far the list scrolls this frame: 0 while the dragged item is more than [edge] from the
     * list's top and bottom, then up to [maxStep] (negative toward the top) as it gets closer. Only
     * the first [grab] of the item counts, where the handle and the finger are: a tall item, like a
     * repeat, would otherwise reach the bottom edge while the finger is dragging it up.
     */
    internal fun scrollStep(edge: Float, maxStep: Float, grab: Float): Float {
        val dragged = key ?: return 0f
        val frame = frames[dragged] ?: return 0f
        val top = start + delta
        val bottom = top + minOf(frame.height.toFloat(), grab)
        val near = when {
            top < viewportTop + edge -> -((viewportTop + edge - top) / edge)
            bottom > viewportBottom - edge -> (bottom - (viewportBottom - edge)) / edge
            else -> 0f
        }
        return near.coerceIn(-1f, 1f) * maxStep
    }

    /** True when the dragged item's middle now sits over another item, which it takes the place of. */
    internal fun swap(): Boolean {
        val dragged = key ?: return false
        val frame = frames[dragged] ?: return false
        val middle = start + delta + frame.height / 2f
        val target = frames.entries.firstOrNull { (onto, it) ->
            onto != dragged && canDropOn(dragged, onto) && middle >= it.top && middle < it.top + it.height
        } ?: return false
        return onMove(dragged, target.key)
    }

    internal fun end() {
        key = null
        delta = 0f
    }
}

/** Marks the scrolling list a [drag] happens in. */
fun Modifier.dragViewport(drag: DragReorder): Modifier = onGloballyPositioned {
    val top = it.positionInRoot().y
    drag.viewport(top, top + it.size.height)
}

/**
 * While an item is dragged within about 64 dp of the list's top or bottom edge, scrolls [list]
 * toward that edge, faster the closer it gets, and swaps the item with the ones it passes. The item
 * keeps following the finger, since its offset is measured from where the layout puts it.
 */
@Composable
fun DragAutoScroll(drag: DragReorder, list: LazyListState) {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    LaunchedEffect(drag.key) {
        if (drag.key == null) return@LaunchedEffect
        val edge = with(density) { 64.dp.toPx() }
        val maxStep = with(density) { 18.dp.toPx() }
        val grab = with(density) { 72.dp.toPx() }
        while (true) {
            withFrameNanos { }
            val step = drag.scrollStep(edge, maxStep, grab)
            if (step == 0f || list.dispatchRawDelta(step) == 0f) continue
            // The items have moved with the scroll; wait for them to be measured again before looking for a swap.
            withFrameNanos { }
            if (drag.swap()) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
    }
}

@Composable
fun rememberDragReorder(canDropOn: (key: Any, onto: Any) -> Boolean, onMove: (key: Any, toKey: Any) -> Boolean): DragReorder {
    val accepts by rememberUpdatedState(canDropOn)
    val move by rememberUpdatedState(onMove)
    return remember { DragReorder({ key, onto -> accepts(key, onto) }, { key, toKey -> move(key, toKey) }) }
}

/** The handle that drags the item [key]: only it starts a drag, so the list still scrolls. */
@Composable
fun Modifier.dragHandle(drag: DragReorder, key: Any): Modifier {
    val haptics = LocalHapticFeedback.current
    return pointerInput(drag, key) {
        detectDragGestures(
            onDragStart = { drag.start(key) },
            onDragEnd = { drag.end() },
            onDragCancel = { drag.end() },
            onDrag = { change, amount ->
                change.consume()
                if (drag.drag(amount.y)) haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            },
        )
    }
}

/**
 * Records where the item [key] sits, and lifts it above the others with a shadow while it is
 * dragged. A list item passes its [scope], so the other items slide out of its way.
 */
@Composable
fun Modifier.draggedItem(drag: DragReorder, key: Any, scope: LazyItemScope? = null): Modifier {
    DisposableEffect(drag, key) { onDispose { drag.forget(key) } }
    // Measured before the lift, so the shadowed item's own offset doesn't move where it sits.
    val placed = onGloballyPositioned { drag.place(key, it.positionInRoot().y, it.size.height) }
    return when {
        drag.key == key -> placed.zIndex(1f).graphicsLayer {
            translationY = drag.offset
            shadowElevation = 8.dp.toPx()
            shape = RoundedCornerShape(PoulpyRadius.md)
        }
        scope != null -> with(scope) { placed.animateItem() }
        else -> placed
    }
}
