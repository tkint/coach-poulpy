package ch.coachpoulpy.android.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ch.coachpoulpy.shared.PoulpyCheer
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.PoulpyMoves
import ch.coachpoulpy.shared.PoulpyTemper
import ch.coachpoulpy.shared.TemperLevel
import ch.coachpoulpy.shared.TemperReaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val STILL_HEIGHT_PER_WIDTH = 678f / 744f

/** The sheets of the moves, from the app's assets (design/poulpy-3d/moves), cut into their frames once for every Poulpy on screen. */
private object MovePictures {
    private val cache = HashMap<String, List<BitmapPainter>>()

    @Synchronized
    fun frames(context: Context, sheet: String): List<BitmapPainter> = cache.getOrPut(sheet) {
        val bitmap = context.applicationContext.assets.open("$sheet.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        val width = bitmap.width / PoulpyMoves.COLUMNS
        val height = bitmap.height / ((PoulpyMoves.FRAMES + PoulpyMoves.COLUMNS - 1) / PoulpyMoves.COLUMNS)
        List(PoulpyMoves.FRAMES) { index ->
            BitmapPainter(bitmap, IntOffset(index % PoulpyMoves.COLUMNS * width, index / PoulpyMoves.COLUMNS * height), IntSize(width, height))
        }
    }
}

/**
 * Poulpy's still picture at [artSize], and when [drawing] has a move, a tap on him plays it: his move (a jump leaning to one side or the other, or for the happy one sometimes a high jump with a full turn) at
 * random, frame by frame from the 3D model ([PoulpyMoves] in the shared code says which frame and when). The frames are bigger than the
 * picture, so he jumps out of its box and back into it. With the phone's animations turned off a tap does nothing.
 *
 * The happy Poulpy loses patience when tapped again and again ([PoulpyTemper]): the fifth tap in a row turns him round and he comes out annoyed, three more
 * and he comes out fuming, each tap in between answered with a move of his mood; left alone for 5 seconds he improves his mood a step, by a turn the other way.
 */
@Composable
fun PoulpyTapArt(drawing: PoulpyDrawing, description: String, artSize: Dp, modifier: Modifier = Modifier) {
    // The champion's tap is his celebration: the same three jumps of joy as at the end of a session, once.
    if (PoulpyCheer.celebrates(drawing)) return PoulpyCheerArt(drawing, description, artSize, modifier, autoplay = false, loop = false)
    val context = LocalContext.current
    var sheet by remember { mutableStateOf<String?>(null) }
    var elapsed by remember { mutableStateOf(0L) }
    LaunchedEffect(sheet) {
        if (sheet == null) return@LaunchedEffect
        val start = withFrameMillis { it }
        elapsed = 0
        while (PoulpyMoves.frame(elapsed) != null) {
            withFrameMillis { now -> elapsed = now - start }
        }
        sheet = null
    }
    val frame = sheet?.let { name -> PoulpyMoves.frame(elapsed)?.let { MovePictures.frames(context, name)[it] } }
    val canMove = PoulpyMoves.moves(drawing)

    // His temper: only the happy Poulpy gets angry. A sheet of a mood move is big, so it is decoded when it plays and let go after.
    val temper = remember { PoulpyTemper() }
    val angry = PoulpyTemper.canGetAngry(drawing)
    var level by remember { mutableStateOf(TemperLevel.CALM) }
    var reaction by remember { mutableStateOf<TemperReaction?>(null) }
    var reactionFrame by remember { mutableStateOf<BitmapPainter?>(null) }
    // Every tap and every end of a move starts the wait for him to calm down again.
    var calmKey by remember { mutableStateOf(0) }
    LaunchedEffect(reaction) {
        val playing = reaction ?: return@LaunchedEffect
        val bitmap: ImageBitmap = withContext(Dispatchers.Default) {
            context.applicationContext.assets.open("${playing.sheet}.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        }
        val width = bitmap.width / PoulpyMoves.COLUMNS
        val height = bitmap.height / playing.rows
        val start = withFrameMillis { it }
        while (true) {
            val index = playing.frame(withFrameMillis { it } - start) ?: break
            val (column, row) = playing.cellOf(index)
            reactionFrame = BitmapPainter(bitmap, IntOffset(column * width, row * height), IntSize(width, height))
        }
        reactionFrame = null
        temper.finished(playing)
        level = temper.level
        reaction = null
        calmKey += 1
    }
    LaunchedEffect(calmKey, angry) {
        if (!angry) return@LaunchedEffect
        delay(PoulpyTemper.CALM_MILLIS)
        if (reaction == null && sheet == null) temper.calm()?.let { reaction = it }
    }
    val still = if (angry) level.drawing else drawing
    val shown = frame ?: reactionFrame
    Box(
        modifier.size(artSize).then(
            if (canMove) {
                Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    val animationsOff = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
                    if (sheet != null || reaction != null || animationsOff) return@clickable
                    calmKey += 1
                    val answer = if (angry) temper.tap() else null
                    if (answer != null) reaction = answer else sheet = PoulpyMoves.sheet(drawing, PoulpyMoves.movesOf(drawing).random())
                }
            } else Modifier,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(still.drawable), contentDescription = description, modifier = Modifier.size(artSize).alpha(if (shown == null) 1f else 0f))
        if (shown != null) {
            // The frame shows him the size of the still picture, with room around him: it is that much bigger, on the same centre.
            Image(
                shown,
                contentDescription = null,
                modifier = Modifier.requiredSize(
                    artSize * (PoulpyMoves.FRAME_WIDTH.toFloat() / PoulpyMoves.PICTURE_WIDTH),
                    // The still picture is 744 by 678 pixels, so it is a little shorter than wide.
                    artSize * (PoulpyMoves.FRAME_HEIGHT.toFloat() / PoulpyMoves.PICTURE_HEIGHT * STILL_HEIGHT_PER_WIDTH),
                ),
            )
        }
    }
}
