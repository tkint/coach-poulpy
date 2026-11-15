package ch.coachpoulpy.android.ui

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.LoaderSwing

/**
 * The loading indicator: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other way,
 * forever, once every 3.6 s (12 s when the phone's animations are turned off). It is one tentacle drawn eight times, and each
 * tentacle hangs loose, bending about its base behind the turn. Where everything is at a given time is [LoaderSwing] in the
 * shared code.
 */
@Composable
fun PoulpyLoader(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val context = LocalContext.current
    // "Remove animations" sets the animator scale to 0. The frame clock below ignores that
    // scale, so Poulpy still turns, only slowly.
    val periodMillis = remember(context) {
        val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        if (scale == 0f) LoaderSwing.REDUCED_MILLIS else LoaderSwing.MILLIS
    }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(periodMillis) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { now -> elapsed = now - start }
        }
    }
    val label = stringResource(R.string.common_loading)
    Box(
        modifier
            .size(size)
            .graphicsLayer { rotationZ = LoaderSwing.degrees(elapsed, periodMillis) }
            .clearAndSetSemantics { contentDescription = label },
    ) {
        for (index in 0 until LoaderSwing.TENTACLES) {
            // Each tentacle is turned round to its place first, then bent about its base.
            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = index * 360f / LoaderSwing.TENTACLES }) {
                Image(
                    painterResource(R.drawable.poulpy_loader_tentacle),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        rotationZ = LoaderSwing.tentacleDegrees(elapsed, index, periodMillis)
                        transformOrigin = TransformOrigin(0.5f, LoaderSwing.BASE)
                    },
                )
            }
        }
    }
}
