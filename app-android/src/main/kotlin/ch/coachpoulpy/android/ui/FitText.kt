package ch.coachpoulpy.android.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * Text in a space of fixed width, like a tab label: it stays on one line and shrinks to fit, so a
 * long word in another language never breaks in the middle (design system, "Words never break").
 * It takes the style and colour of where it sits, as `Text` does.
 */
@Composable
fun FitText(text: String, modifier: Modifier = Modifier) {
    val style = LocalTextStyle.current
    val size = if (style.fontSize.isSpecified) style.fontSize else 12.sp
    BasicText(
        text,
        modifier = modifier,
        style = style.copy(color = LocalContentColor.current),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = size),
    )
}
