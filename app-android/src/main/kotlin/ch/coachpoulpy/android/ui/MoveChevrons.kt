package ch.coachpoulpy.android.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

/** Which step or repeat the chevrons moved last, so it can scroll into view once it has its new place. */
@Stable
class MoveTracker {
    var key by mutableStateOf<String?>(null)
        private set
    var count by mutableIntStateOf(0)
        private set

    fun moved(key: String) {
        this.key = key
        count++
    }
}

@Composable
fun rememberMoveTracker() = remember { MoveTracker() }

/** After the item [key] moved, scrolls the list just far enough to show it in full; nothing if it already is. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.keepInViewWhenMoved(tracker: MoveTracker, key: String): Modifier {
    val requester = remember { BringIntoViewRequester() }
    if (tracker.key == key) {
        LaunchedEffect(tracker.count) { requester.bringIntoView() }
    }
    return bringIntoViewRequester(requester)
}

/**
 * Slides the item to its new place when its layout changes, for items that are not lazy list items and
 * so can't use animateItem(), like the steps of a repeat. The animation follows the system's animation scale.
 */
@Composable
fun Modifier.slideIntoPlace(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var target by remember { mutableStateOf(IntOffset.Zero) }
    var slide by remember { mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null) }
    onPlaced { target = it.positionInParent().round() }.offset {
        val animation = slide ?: Animatable(target, IntOffset.VectorConverter).also { slide = it }
        if (animation.targetValue != target) {
            scope.launch { animation.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow)) }
        }
        animation.value - target
    }
}

/**
 * The up and down chevrons that move a step or a repeat among its siblings. A chevron at the end of
 * the list stays in place, dimmed and disabled, so nothing jumps. Each is 44dp wide and 32dp tall, as in the agreed mockup.
 */
@Composable
fun MoveChevrons(
    upLabel: String,
    downLabel: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier) {
        Chevron(upLabel, canMoveUp, up = true) { onMoveUp(); haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
        Chevron(downLabel, canMoveDown, up = false) { onMoveDown(); haptics.performHapticFeedback(HapticFeedbackType.SegmentTick) }
    }
}

@Composable
private fun Chevron(label: String, enabled: Boolean, up: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(44.dp, 32.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (up) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = if (enabled) colors.primary else colors.outline,
            modifier = Modifier.size(24.dp),
        )
    }
}
