package ch.coachpoulpy.android.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AppIcon
import ch.coachpoulpy.shared.AppIcons

/**
 * The home screen icon is one launcher alias per [AppIcon] (see AndroidManifest.xml), exactly one of them enabled.
 * The phone keeps that state, so the app stores nothing of its own.
 */
internal object AppIconSwitch {
    private fun alias(context: Context, icon: AppIcon) =
        ComponentName(context, "ch.coachpoulpy.android.AppIcon${icon.id.replaceFirstChar { it.uppercase() }}")

    /** The enabled alias; a state the manifest has not changed yet counts as its own `android:enabled`, which is the standard icon. */
    fun current(context: Context): AppIcon {
        val packages = context.packageManager
        return AppIcons.all.firstOrNull { icon ->
            when (packages.getComponentEnabledSetting(alias(context, icon))) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
                else -> icon == AppIcons.standard
            }
        } ?: AppIcons.standard
    }

    /** Enables the new alias before disabling the others, so the launcher never loses the app. */
    fun apply(context: Context, icon: AppIcon) {
        val packages = context.packageManager
        packages.setComponentEnabledSetting(alias(context, icon), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        AppIcons.all.filter { it != icon }.forEach {
            packages.setComponentEnabledSetting(alias(context, it), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }
}

@get:StringRes
internal val AppIcon.nameRes: Int
    get() = when (this) {
        AppIcon.HAPPY -> R.string.appIcon_happy
        AppIcon.PROUD -> R.string.appIcon_proud
        AppIcon.ANNOYED -> R.string.appIcon_annoyed
        AppIcon.GRUMPY -> R.string.appIcon_grumpy
        AppIcon.CHAMPION -> R.string.appIcon_champion
        AppIcon.COCKY -> R.string.appIcon_cocky
    }

/** The drawing the app shows for this face, the same one the icon is made from. */
@get:DrawableRes
internal val AppIcon.drawingRes: Int
    get() = when (this) {
        AppIcon.HAPPY -> R.drawable.poulpy_mood_happy
        AppIcon.PROUD -> R.drawable.poulpy_mood_proud
        AppIcon.ANNOYED -> R.drawable.poulpy_mood_annoyed
        AppIcon.GRUMPY -> R.drawable.poulpy_mood_grumpy
        AppIcon.CHAMPION -> R.drawable.poulpy_pose_champion
        AppIcon.COCKY -> R.drawable.poulpy_pose_cocky
    }

/** "Happy · default" for the standard icon, the plain name for the others. */
@Composable
internal fun AppIcon.label(): String {
    val name = stringResource(nameRes)
    return if (this == AppIcons.standard) stringResource(R.string.appIcon_defaultName, name) else name
}

/** Settings › App › App icon: tap a face to apply it and close the sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppIconSheet(selected: AppIcon, onPick: (AppIcon) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = PoulpySpace.lg, end = PoulpySpace.lg, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_appIcon), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.appIcon_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.selectableGroup()) {
                    AppIcons.all.forEachIndexed { index, icon ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = PoulpySize.row)
                                .selectable(selected = icon == selected, role = Role.RadioButton, onClick = { onPick(icon); onDismiss() })
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(painterResource(icon.drawingRes), contentDescription = null, modifier = Modifier.size(48.dp))
                            Text(icon.label(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 12.dp))
                            RadioButton(selected = icon == selected, onClick = null)
                        }
                    }
                }
                Text(stringResource(R.string.appIcon_tapNote), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.appIcon_iosNote), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(PoulpySize.buttonLarge),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            ) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
