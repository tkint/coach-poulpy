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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ch.coachpoulpy.shared.PoulpyCheer
import ch.coachpoulpy.shared.PoulpyDrawing
import ch.coachpoulpy.shared.PoulpyParty
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private const val STILL_HEIGHT_PER_WIDTH = 678f / 744f

/** One sheet of the celebration (design/poulpy-3d/moves), decoded from the app's assets. They are big, so only the one playing and the next are held. */
private suspend fun decode(context: Context, name: String): ImageBitmap = withContext(Dispatchers.Default) {
    context.applicationContext.assets.open("$name.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
}

/**
 * Poulpy's still picture at [artSize], and when [drawing] celebrates (the champion), he jumps for joy three times with his medal and confetti, frame by frame
 * from the 3D model ([PoulpyCheer] in the shared code says which frame and when), and settles on the picture; a tap plays it again. The frames are bigger
 * than the picture so the confetti has room. With [autoplay] he starts when the screen opens, otherwise only at a tap (Home); with [loop] he never stops
 * celebrating, round after round with no rest ([PoulpyParty], a loop that ends where it starts), for as long as the screen is open (the finished screen). With the phone's animations turned off he just stands there.
 */
@Composable
fun PoulpyCheerArt(drawing: PoulpyDrawing, description: String, artSize: Dp, modifier: Modifier = Modifier, autoplay: Boolean = true, loop: Boolean = true) {
    val context = LocalContext.current
    // He celebrates when the screen opens if asked to, and again at each tap.
    var plays by remember { mutableStateOf(if (autoplay) 1 else 0) }
    var frame by remember { mutableStateOf<BitmapPainter?>(null) }
    val still = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    LaunchedEffect(plays) {
        if (still || plays == 0) return@LaunchedEffect
        runCatching {
            coroutineScope {
                // Once: the three jumps, then his picture. Looping: the party, over and over, from sheet to sheet and back to the first.
                val sheets = if (loop) PoulpyParty.SHEETS else PoulpyCheer.SHEETS
                val name = { index: Int -> if (loop) PoulpyParty.sheet(index) else PoulpyCheer.sheet(index) }
                var sheetIndex = 0
                var sheet = decode(context, name(0))
                // The next sheet is decoded while this one plays; if it is not ready in time, the last picture stays.
                var next: Deferred<ImageBitmap>? = async { decode(context, name(1)) }
                val start = withFrameMillis { it }
                while (true) {
                    val elapsed = withFrameMillis { it } - start
                    val index = (if (loop) PoulpyParty.frame(elapsed) else PoulpyCheer.frame(elapsed)) ?: break
                    val wanted = if (loop) PoulpyParty.sheetOf(index) else PoulpyCheer.sheetOf(index)
                    if (wanted != sheetIndex) {
                        val ready = next ?: continue
                        if (!ready.isCompleted) continue
                        sheet = ready.await()
                        sheetIndex = (sheetIndex + 1) % sheets
                        next = if (loop || sheetIndex + 1 < sheets) async { decode(context, name((sheetIndex + 1) % sheets)) } else null
                    }
                    val width = sheet.width / PoulpyCheer.COLUMNS
                    val height = sheet.height / (PoulpyCheer.SHEET_FRAMES / PoulpyCheer.COLUMNS)
                    val (column, row) = PoulpyCheer.cellOf(index)
                    frame = BitmapPainter(sheet, IntOffset(column * width, row * height), IntSize(width, height))
                }
            }
        }
        frame = null
    }
    Box(
        modifier.size(artSize).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { if (frame == null) plays += 1 },
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(drawing.drawable), contentDescription = description.ifEmpty { null }, modifier = Modifier.size(artSize).alpha(if (frame == null) 1f else 0f))
        frame?.let { picture ->
            // The frame shows him the size of the still picture, with room around him: it is that much bigger, on the same centre.
            Image(
                picture,
                contentDescription = null,
                modifier = Modifier.requiredSize(
                    artSize * (PoulpyCheer.FRAME_WIDTH.toFloat() / PoulpyCheer.PICTURE_WIDTH),
                    artSize * (PoulpyCheer.FRAME_HEIGHT.toFloat() / PoulpyCheer.PICTURE_HEIGHT * STILL_HEIGHT_PER_WIDTH),
                ),
            )
        }
    }
}
