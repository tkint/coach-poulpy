package ch.coachpoulpy.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
    MovementPattern.SQUAT -> Lucide.PersonStanding
    MovementPattern.HINGE -> Lucide.HandGrab
    MovementPattern.LUNGE -> Lucide.Footprints
    MovementPattern.PUSH -> Lucide.Dumbbell
    MovementPattern.PULL -> Lucide.BicepsFlexed
    MovementPattern.CARRY -> Lucide.Luggage
    MovementPattern.CORE -> Lucide.Shield
    MovementPattern.ROTATION -> Lucide.RotateCw
    MovementPattern.LOCOMOTION -> Lucide.SportShoe
    MovementPattern.MOBILITY -> Lucide.Accessibility
    MovementPattern.STRETCH -> Lucide.StretchHorizontal
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
