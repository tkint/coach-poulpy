package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Rowing
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.coachpoulpy.core.model.MovementPattern

/**
 * Stands in for the exercise picture until the catalog carries real images:
 * one illustration per movement pattern.
 */
fun MovementPattern.icon(): ImageVector = when (this) {
    MovementPattern.SQUAT -> Icons.Filled.AccessibilityNew
    MovementPattern.HINGE -> Icons.Filled.Accessibility
    MovementPattern.LUNGE -> Icons.AutoMirrored.Filled.DirectionsWalk
    MovementPattern.PUSH -> Icons.Filled.FitnessCenter
    MovementPattern.PULL -> Icons.Filled.Rowing
    MovementPattern.CARRY -> Icons.Filled.Luggage
    MovementPattern.CORE -> Icons.Filled.SportsGymnastics
    MovementPattern.ROTATION -> Icons.Filled.Sync
    MovementPattern.LOCOMOTION -> Icons.AutoMirrored.Filled.DirectionsRun
    MovementPattern.MOBILITY -> Icons.Filled.SelfImprovement
    MovementPattern.STRETCH -> Icons.Filled.SportsMartialArts
}

@Composable
fun PatternBadge(pattern: MovementPattern, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            pattern.icon(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}
