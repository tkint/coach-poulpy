package ch.coachpoulpy.android.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Drags a [LazyListState] item by its key: the item follows the finger, and once its middle
 * passes over another item that [canDropOn] accepts for it, [onMove] moves it there. [onMove] answers
 * whether the order changed, which plays a light tick.
 */
@Stable
class DragReorder internal constructor(
    private val list: LazyListState,
    private val canDropOn: (key: Any, onto: Any) -> Boolean,
    private val onMove: (key: Any, toKey: Any) -> Boolean,
) {
    var key by mutableStateOf<Any?>(null)
        private set
    private var start = 0f
    private var delta by mutableFloatStateOf(0f)

    private fun info(key: Any?): LazyListItemInfo? = list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }

    /** How far the dragged item is drawn from where the list lays it out. */
    val offset: Float get() = info(key)?.let { start + delta - it.offset } ?: 0f

    internal fun start(key: Any) {
        val item = info(key) ?: return
        this.key = key
        start = item.offset.toFloat()
        delta = 0f
    }

    /** Follows the finger by [dy]; true when the item moved past another. */
    internal fun drag(dy: Float): Boolean {
        delta += dy
        val dragged = info(key) ?: return false
        val middle = (start + delta + dragged.size / 2f).toInt()
        val target = list.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key != dragged.key && canDropOn(dragged.key, it.key) && middle in it.offset until it.offset + it.size
        } ?: return false
        return onMove(dragged.key, target.key)
    }

    internal fun end() {
        key = null
        delta = 0f
    }
}

@Composable
fun rememberDragReorder(list: LazyListState, canDropOn: (key: Any, onto: Any) -> Boolean, onMove: (key: Any, toKey: Any) -> Boolean): DragReorder {
    val accepts by rememberUpdatedState(canDropOn)
    val move by rememberUpdatedState(onMove)
    return remember(list) { DragReorder(list, { key, onto -> accepts(key, onto) }, { key, toKey -> move(key, toKey) }) }
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

/** Lifts the dragged item above the others with a shadow; the others slide out of its way. */
fun Modifier.draggedItem(scope: LazyItemScope, drag: DragReorder, key: Any): Modifier =
    if (drag.key == key) {
        zIndex(1f).graphicsLayer {
            translationY = drag.offset
            shadowElevation = 8.dp.toPx()
            shape = RoundedCornerShape(PoulpyRadius.md)
        }
    } else {
        with(scope) { animateItem() }
    }
