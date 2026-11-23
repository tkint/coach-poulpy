package ch.coachpoulpy.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.StatBlock
import kotlin.math.roundToInt

/** Edit statistics: a switch and a handle per block, to choose and order what You shows. Every change is saved as it is made. */
@Composable
fun EditStatisticsScreen(graph: AppGraph, language: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val model = remember(graph, language) { graph.you(language, AppPreferences(context)) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()

    var rowHeight by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf<StatBlock?>(null) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val gap = with(LocalDensity.current) { PoulpySpace.sm.toPx() }

    Column(modifier.fillMaxSize()) {
        AppBar(
            title = stringResource(R.string.you_editTitle),
            leading = { CloseButton(onClose) },
            actions = { PoulpyButton(onClick = onClose, size = PoulpyButtonSize.Sm) { Text(stringResource(R.string.action_done)) } },
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg, vertical = PoulpySpace.sm),
            verticalArrangement = Arrangement.spacedBy(PoulpySpace.sm),
        ) {
            Note(stringResource(R.string.you_editNote))
            state.blocks.forEachIndexed { index, choice ->
                key(choice.block) {
                    val position by rememberUpdatedState(index)
                    val name = stringResource(choice.block.label)
                    val handleDescription = stringResource(R.string.you_dragHandle, name)
                    val lifted = dragging == choice.block
                    OutlinedSurface(
                        modifier = Modifier
                            .onSizeChanged { rowHeight = it.height + gap }
                            .zIndex(if (lifted) 1f else 0f)
                            .graphicsLayer { translationY = if (lifted) dragY else 0f },
                    ) {
                        Row(Modifier.heightIn(min = PoulpySize.row).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(PoulpySize.touch)
                                    .semantics { contentDescription = handleDescription }
                                    .pointerInput(choice.block) {
                                        detectVerticalDragGestures(
                                            onDragStart = { dragging = choice.block; dragY = 0f },
                                            onVerticalDrag = { change, amount ->
                                                change.consume()
                                                dragY += amount
                                            },
                                            onDragEnd = {
                                                if (rowHeight > 0f) model.move(choice.block, position + (dragY / rowHeight).roundToInt())
                                                dragging = null
                                                dragY = 0f
                                            },
                                            onDragCancel = { dragging = null; dragY = 0f },
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Lucide.GripVertical, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                            Text(name, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f).padding(start = 4.dp))
                            Switch(checked = choice.shown, onCheckedChange = { model.setShown(choice.block, it) })
                        }
                    }
                }
            }
        }
    }
}

private val StatBlock.label: Int
    get() = when (this) {
        StatBlock.SESSIONS_AND_TIME -> R.string.you_blockSessions
        StatBlock.RECORDS_AND_WEIGHT -> R.string.you_blockRecords
    }
