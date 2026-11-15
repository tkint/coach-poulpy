package ch.coachpoulpy.android.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.LoaderSwing

/** The loader's pictures, from the app's assets (design/poulpy-3d/loader), decoded once for every loader on screen: the swing's sheet cut into its frames, or the one clockwise picture. */
private object LoaderPictures {
    private val cache = HashMap<Boolean, List<BitmapPainter>>()

    @Synchronized
    fun frames(context: Context, clockwise: Boolean): List<BitmapPainter> = cache.getOrPut(clockwise) {
        val sheet = context.applicationContext.assets.open(if (clockwise) "clockwise.png" else "swing.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
        if (clockwise) listOf(BitmapPainter(sheet))
        else {
            val side = sheet.width / LoaderSwing.COLUMNS
            List(LoaderSwing.FRAMES) { index -> BitmapPainter(sheet, IntOffset(index % LoaderSwing.COLUMNS * side, index / LoaderSwing.COLUMNS * side), IntSize(side, side)) }
        }
    }
}

/**
 * The loading indicator: Poulpy's outline seen from underneath, his tentacles loose, one cycle every 3.6 s (12 s when the phone's
 * animations are turned off). It swings half a turn one way and then half a turn the other way, or with `clockwise` only turns
 * clockwise. The picture of each moment is drawn frame by frame from the 3D model (design/build-poulpy-pictures.mjs) and shows how
 * his tentacles hang (clockwise, they trail the same all the time, so that is one picture); the turn is smooth. Which frame and how far it is turned is [LoaderSwing] in the shared code.
 */
@Composable
fun PoulpyLoader(modifier: Modifier = Modifier, size: Dp = 48.dp, clockwise: Boolean = false) {
    val context = LocalContext.current
    // "Remove animations" sets the animator scale to 0. The frame clock below ignores that
    // scale, so Poulpy still turns, only slowly.
    val periodMillis = remember(context) {
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (scale == 0f) LoaderSwing.REDUCED_MILLIS else LoaderSwing.MILLIS
    }
    val frames = remember(context, clockwise) { LoaderPictures.frames(context, clockwise) }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(periodMillis) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { now -> elapsed = now - start }
        }
    }
    val label = stringResource(R.string.common_loading)
    Image(
        frames[if (clockwise) 0 else LoaderSwing.frame(elapsed, periodMillis)],
        contentDescription = null,
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = if (clockwise) LoaderSwing.clockwiseDegrees(elapsed, periodMillis) else LoaderSwing.degrees(elapsed, periodMillis) }
            .clearAndSetSemantics { contentDescription = label },
    )
}
