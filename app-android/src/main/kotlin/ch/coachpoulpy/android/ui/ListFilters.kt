package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R

/**
 * The filter row both Library lists share: the pill search, a row of [chips], then the
 * count line with [action] at its right ("Clear filters", or the workouts' sort).
 */
@Composable
fun ListFilters(
    query: String,
    onQuery: (String) -> Unit,
    placeholder: String,
    label: String,
    countLabel: String,
    chips: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Column(modifier) {
        PillSearchField(
            query = query,
            onQuery = onQuery,
            placeholder = placeholder,
            label = label,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = chips,
        )
        Row(
            Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                countLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            )
            action()
        }
    }
}

/**
 * Poulpy, happy on the blue side, in a `primaryContainer` card: what the lists show
 * when nothing matches or nothing exists yet, with one primary button.
 */
@Composable
fun PoulpyCard(
    title: String,
    body: String,
    button: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonIcon: ImageVector? = null,
    poulpySize: Dp = 96.dp,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.primaryContainer, RoundedCornerShape(PoulpyRadius.lg))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(painterResource(R.drawable.poulpy_mood_happy), contentDescription = null, modifier = Modifier.size(poulpySize))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onPrimaryContainer, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onClick, modifier = Modifier.padding(top = 8.dp).height(48.dp)) {
            if (buttonIcon != null) Icon(buttonIcon, contentDescription = null, modifier = Modifier.padding(end = 8.dp).size(20.dp))
            Text(button, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** The 48 high pill search with a 1.5 px outline, `primary` once something is typed, and a clear button. */
@Composable
fun PillSearchField(query: String, onQuery: (String) -> Unit, placeholder: String, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val pill = RoundedCornerShape(PoulpyRadius.pill)
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colors.surface, pill)
            .border(1.5.dp, if (query.isEmpty()) colors.outline else colors.primary, pill)
            .padding(start = 18.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, maxLines = 1)
            }
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQuery("") }, modifier = Modifier.size(44.dp)) {
                Box(Modifier.size(36.dp).background(colors.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.exercises_clear_search),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** The design-system chip: 36 high, pill; on = `primary`, off = 1.5 px `outline`. */
@Composable
fun PoulpyChip(selected: Boolean, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = if (selected) null else BorderStroke(1.5.dp, colors.outline),
        modifier = modifier.height(36.dp),
    ) {
        Box(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold)
        }
    }
}
