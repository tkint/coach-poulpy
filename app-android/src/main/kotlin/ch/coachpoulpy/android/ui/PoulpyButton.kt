package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Primary is one per screen, Secondary (outlined) and Tonal are the others, Grumpy only the catch-up action. */
enum class PoulpyButtonKind { Primary, Secondary, Tonal, Grumpy }

/** The size scale of the buttons, shared with the websites and drawn by the `Button` mockup (PoulpyControl, from design/tokens.json). */
enum class PoulpyButtonSize(val height: Dp, val horizontal: Dp, val text: TextUnit, val icon: Dp) {
    Xs(PoulpyControl.xs, PoulpySpace.sm, 12.sp, PoulpyControlIcon.xs),
    Sm(PoulpyControl.sm, PoulpySpace.md, 14.sp, PoulpyControlIcon.sm),
    Md(PoulpyControl.md, PoulpySpace.lg, 15.sp, PoulpyControlIcon.md),
    Lg(PoulpyControl.lg, PoulpySpace.lg, 16.sp, PoulpyControlIcon.md),
    Xl(PoulpyControl.xl, PoulpySpace.lg, 17.sp, PoulpyControlIcon.md),
    Xxl(PoulpyControl.xxl, PoulpySpace.lg, 18.sp, PoulpyControlIcon.lg),
}

/**
 * The apps' one button: a rounded square (`PoulpyRadius.md`), never a pill, in a size of the scale (`Md` unless told
 * otherwise). Mirrors `PoulpyButton` on iOS and the `Button` mockup.
 */
@Composable
fun PoulpyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: PoulpyButtonKind = PoulpyButtonKind.Primary,
    size: PoulpyButtonSize = PoulpyButtonSize.Md,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(PoulpyRadius.md)
    val padding = PaddingValues(horizontal = size.horizontal)
    val sized = modifier.height(size.height)
    val label: @Composable RowScope.() -> Unit = {
        ProvideTextStyle(LocalTextStyle.current.merge(TextStyle(fontSize = size.text, fontWeight = FontWeight.ExtraBold))) { content() }
    }
    when (kind) {
        PoulpyButtonKind.Primary -> Button(onClick, sized, enabled, shape = shape, colors = colors ?: ButtonDefaults.buttonColors(), contentPadding = padding, content = label)
        PoulpyButtonKind.Tonal -> FilledTonalButton(onClick, sized, enabled, shape = shape, colors = colors ?: ButtonDefaults.filledTonalButtonColors(), contentPadding = padding, content = label)
        PoulpyButtonKind.Secondary -> OutlinedButton(
            onClick,
            sized,
            enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.outlinedButtonColors(),
            border = border ?: BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            contentPadding = padding,
            content = label,
        )
        PoulpyButtonKind.Grumpy -> Button(
            onClick,
            sized,
            enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.buttonColors(containerColor = PoulpyTheme.extraColors.grumpy, contentColor = PoulpyTheme.extraColors.onGrumpy),
            contentPadding = padding,
            content = label,
        )
    }
}
