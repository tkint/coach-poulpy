package ch.coachpoulpy.android.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R

/**
 * The one top bar of the app's screens: its row starts under the status bar at the height of the tab titles.
 * A pushed screen starts with [leading] (a back chevron or a close cross), then the [title], then the [actions].
 */
@Composable
internal fun AppBar(
    title: String?,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    // The bar sits on the screen's background, not on a surface: its text and icons take the background's text colour
    // (black text on a dark theme otherwise, since nothing above sets one).
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Row(
            modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = PoulpySpace.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()
            Text(
                title.orEmpty(),
                style = tabTitleStyle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            actions()
        }
    }
}

/** The close cross of a screen that ends a task. */
@Composable
internal fun CloseButton(onClose: () -> Unit) {
    IconButton(onClick = onClose) { Icon(Lucide.X, contentDescription = stringResource(R.string.action_close)) }
}
