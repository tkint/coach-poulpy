package ch.coachpoulpy.android.ui

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R

/**
 * The loading indicator: Poulpy seen from above as a blue outline, turning clockwise (the top moves left to right) once every
 * 2.4 s, or every 10 s when the phone's animations are turned off. Below 32 dp the line is thicker.
 */
@Composable
fun PoulpyLoader(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val context = LocalContext.current
    // "Remove animations" sets the animator scale to 0. The frame clock below ignores that
    // scale, so Poulpy still turns, only slowly.
    val periodMillis = remember(context) {
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (scale == 0f) REDUCED_TURN_MILLIS else TURN_MILLIS
    }
    var degrees by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(periodMillis) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { now -> degrees = ((now - start) % periodMillis) * 360f / periodMillis }
        }
    }
    Image(
        painterResource(if (size < SMALL) R.drawable.poulpy_loader_small else R.drawable.poulpy_loader),
        contentDescription = stringResource(R.string.loading),
        modifier = modifier.size(size).graphicsLayer { rotationZ = degrees },
    )
}

private const val TURN_MILLIS = 2_400L
private const val REDUCED_TURN_MILLIS = 10_000L
private val SMALL = 32.dp
