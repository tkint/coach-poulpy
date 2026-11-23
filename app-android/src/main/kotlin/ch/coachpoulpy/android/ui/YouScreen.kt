package ch.coachpoulpy.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.R
import ch.coachpoulpy.core.model.TrainingGoal
import ch.coachpoulpy.shared.AccountState
import ch.coachpoulpy.shared.AppGraph
import ch.coachpoulpy.shared.BodyDisplay
import ch.coachpoulpy.shared.StatBlock
import ch.coachpoulpy.shared.StatsPeriod
import ch.coachpoulpy.shared.YouState
import java.util.Locale

/**
 * You: the profile card with its badge, then Statistics for the chosen period. The cog opens Settings, the card opens
 * the profile and Edit chooses and orders the blocks. Search and the other blocks (records, body map, focus split,
 * cardio, volume and load, time of day) wait for their own issues.
 */
@Composable
fun YouScreen(
    graph: AppGraph,
    language: String,
    onSettings: () -> Unit,
    onProfile: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val memory = remember { AppPreferences(context) }
    val model = remember(graph, language) { graph.you(language, memory) }
    DisposableEffect(model) { onDispose { model.close() } }
    val state by model.state.collectAsStateWithLifecycle()
    val profile by remember(graph) { graph.profile(memory) }.profile.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = PoulpySpace.lg, end = PoulpySpace.sm, top = PoulpySpace.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.tab_you), style = tabTitleStyle(), modifier = Modifier.weight(1f))
            IconButton(onClick = onSettings, modifier = Modifier.size(PoulpySize.touch)) {
                Icon(Lucide.Settings, contentDescription = stringResource(R.string.tab_settings), modifier = Modifier.size(22.dp))
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = PoulpySpace.lg).padding(top = PoulpySpace.sm, bottom = PoulpySpace.lg),
            verticalArrangement = Arrangement.spacedBy(PoulpySpace.md),
        ) {
            val summary = listOfNotNull(
                TrainingGoal.entries.filter { it in profile.goals }.map { stringResource(it.label) }.joinToString(", ").ifEmpty { null },
                profile.experience?.let { stringResource(it.label) },
            ).joinToString(" · ")
            ProfileCard(profile.displayName ?: stringResource(R.string.settings_yourProfile), summary, onProfile)

            Row(Modifier.fillMaxWidth().padding(top = PoulpySpace.xs), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.you_statistics).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                PoulpyButton(onClick = onEdit, kind = PoulpyButtonKind.Tonal, size = PoulpyButtonSize.Sm) {
                    Icon(Lucide.Pencil, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.action_edit), modifier = Modifier.padding(start = 6.dp))
                }
            }
            PeriodTabs(state.period, onSelect = model::selectPeriod)
            if (state.loaded) StatCards(state)
        }
    }
}

/** The profile card: Poulpy's face, the name, goals and experience, and one badge that is a label, not a button. */
@Composable
private fun ProfileCard(name: String, summary: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.lg), color = colors.primaryContainer) {
        Row(Modifier.padding(PoulpySpace.lg), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.poulpy_mood_happy), contentDescription = null, modifier = Modifier.size(64.dp))
            Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (summary.isNotEmpty()) Text(summary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                AccountBadge(Modifier.padding(top = 4.dp))
            }
            Icon(Lucide.ChevronRight, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

/** Guest, Confirm your email or Signed in. */
@Composable
private fun AccountBadge(modifier: Modifier = Modifier) {
    val state by rememberAccount().state.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme
    val signedIn = state is AccountState.SignedIn
    val text = stringResource(
        when (state) {
            AccountState.Guest -> R.string.you_badgeGuest
            is AccountState.Pending -> R.string.account_rowPending
            is AccountState.SignedIn -> R.string.you_badgeSignedIn
        },
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PoulpyRadius.pill),
        color = if (signedIn) colors.primary else colors.surface,
        contentColor = if (signedIn) colors.onPrimary else colors.onSurface,
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            if (signedIn) Icon(Lucide.Check, contentDescription = null, modifier = Modifier.size(14.dp).padding(end = 2.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

/** The period switch: 7 days, 30 days, Year, All time. It sets every block. */
@Composable
private fun PeriodTabs(selected: StatsPeriod, onSelect: (StatsPeriod) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(PoulpyRadius.md)).background(colors.surfaceVariant).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        StatsPeriod.entries.forEach { period ->
            val on = period == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = PoulpySize.chip)
                    .clip(RoundedCornerShape(PoulpyRadius.sm))
                    .background(if (on) colors.surface else colors.surfaceVariant)
                    .selectable(selected = on, onClick = { onSelect(period) }, role = Role.Tab)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(period.label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (on) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (on) colors.primary else colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

private val StatsPeriod.label: Int
    get() = when (this) {
        StatsPeriod.DAYS_7 -> R.string.you_period7
        StatsPeriod.DAYS_30 -> R.string.you_period30
        StatsPeriod.YEAR -> R.string.you_periodYear
        StatsPeriod.ALL_TIME -> R.string.you_periodAll
    }

/** The cards of the blocks that show, in the person's order, two to a row. */
@Composable
private fun StatCards(state: YouState) {
    val units = LocalUnitSystem.current
    val language = Locale.getDefault().language
    val cards = state.blocks.filter { it.shown }.flatMap { choice ->
        when (choice.block) {
            StatBlock.SESSIONS_AND_TIME -> listOf<@Composable (Modifier) -> Unit>(
                { modifier ->
                    val stats = state.stats
                    val line = listOfNotNull(
                        stringResource(R.string.history_minutes, stats.activeMinutes),
                        stats.distanceMeters.takeIf { it > 0 }?.let { distance(it, 1) },
                    ).joinToString(" · ")
                    StatCard(stringResource(R.string.you_sessions), "${stats.sessions}", line, highlighted = true, modifier = modifier)
                },
            )
            StatBlock.RECORDS_AND_WEIGHT -> listOf<@Composable (Modifier) -> Unit>(
                { modifier ->
                    val weight = state.weight
                    StatCard(
                        stringResource(R.string.body_weight),
                        weight?.let { BodyDisplay.weight(it.kilograms, units, language) } ?: "–",
                        when {
                            weight == null -> stringResource(R.string.body_nothingLogged)
                            weight.daysAgo == 0 -> stringResource(R.string.you_weightToday)
                            weight.daysAgo == 1 -> stringResource(R.string.you_weightYesterday)
                            else -> pluralStringResource(R.plurals.you_weightDaysAgo, weight.daysAgo, weight.daysAgo)
                        },
                        highlighted = false, labelPrimary = true, muted = true, modifier = modifier,
                    )
                },
                { modifier ->
                    StatCard(
                        stringResource(R.string.history_longestRun),
                        state.stats.longestRunMeters?.let { distance(it, 1) } ?: "–",
                        state.stats.longestRunMillis?.let { shortDayMonth(it) }.orEmpty(),
                        highlighted = false, modifier = modifier,
                    )
                },
                { modifier ->
                    StatCard(
                        stringResource(R.string.history_fullOctopus),
                        "×${state.fullOctopuses}",
                        stringResource(R.string.history_tentaclesLit, state.tentaclesLit),
                        highlighted = false, modifier = modifier,
                    )
                },
            )
        }
    }
    cards.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PoulpySpace.md)) {
            row.forEach { card -> card(Modifier.weight(1f)) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    line: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    labelPrimary: Boolean = false,
    muted: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val content = if (highlighted) colors.onPrimaryContainer else colors.onSurface
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(PoulpyRadius.lg),
        color = if (highlighted) colors.primaryContainer else colors.surface,
        border = BorderStroke(1.dp, if (highlighted) colors.primaryContainer else colors.outlineVariant),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = if (labelPrimary) colors.primary else if (highlighted) content else colors.onSurfaceVariant,
                maxLines = 1,
            )
            Text(value, style = MaterialTheme.typography.headlineMedium, color = content, maxLines = 1)
            if (line.isNotEmpty()) {
                Text(
                    line,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (muted) FontWeight.Normal else FontWeight.Bold,
                    color = if (muted) colors.onSurfaceVariant else content,
                )
            }
        }
    }
}
