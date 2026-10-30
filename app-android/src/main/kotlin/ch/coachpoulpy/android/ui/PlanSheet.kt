package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.PlanDate
import ch.coachpoulpy.shared.PlannedDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/**
 * Tick the days to train a workout, untick to take one off: a month calendar from
 * Monday, past days dimmed, today outlined, planned days filled. Saving hands back
 * every ticked day; none keeps the workout unplanned.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanSheet(name: String, planned: List<PlannedDay>, onSave: (List<PlanDate>) -> Unit, onDismiss: () -> Unit) {
    val today = remember { LocalDate.now() }
    var ticked by remember { mutableStateOf(planned.map { LocalDate.of(it.date.year, it.date.month, it.date.day) }.toSet()) }
    var month by remember { mutableStateOf(YearMonth.from(ticked.minOrNull() ?: today)) }
    val locale = Locale.getDefault()
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.plan_title, name), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.plan_hint), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = month.minusMonths(1) }, enabled = month > YearMonth.from(today)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.plan_previousMonth))
                }
                Text(
                    month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.plan_nextMonth))
                }
            }
            Row(Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW, locale),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            val lead = month.atDay(1).dayOfWeek.value - 1
            val cells = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        (0 until 7).forEach { index ->
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                week.getOrNull(index)?.let { day ->
                                    DayCell(day, today, ticked = day in ticked) {
                                        ticked = if (day in ticked) ticked - day else ticked + day
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = { onSave(ticked.filter { !it.isBefore(today) }.map { PlanDate(it.year, it.monthValue, it.dayOfMonth) }) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                val count = ticked.count { !it.isBefore(today) }
                Text(
                    if (count == 0) stringResource(R.string.plan_keepUnplanned) else pluralStringResource(R.plurals.plan_save, count, count),
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun DayCell(day: LocalDate, today: LocalDate, ticked: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val past = day.isBefore(today)
    val label = day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))
    Surface(
        selected = ticked,
        onClick = onToggle,
        enabled = !past,
        shape = CircleShape,
        color = if (ticked) colors.primary else colors.surface,
        contentColor = when {
            ticked -> colors.onPrimary
            past -> colors.onSurfaceVariant.copy(alpha = 0.5f)
            day == today -> colors.primary
            else -> colors.onSurface
        },
        border = if (day == today && !ticked) BorderStroke(1.5.dp, colors.primary) else null,
        modifier = Modifier.size(40.dp).semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (ticked || day == today) FontWeight.ExtraBold else FontWeight.Bold,
            )
        }
    }
}
