package com.aslmmovic.qurancompanion.presentation.screens.journey.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold

@Composable
fun JourneySegmentedProgressBar(
    totalSteps: Int,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    if (totalSteps <= 0) return

    val completedColor = MaterialTheme.colorScheme.primary
    val activeColor = SahabaWarmGold
    val upcomingColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 0 until totalSteps) {
            val targetColor = when {
                i < currentStepIndex  -> completedColor
                i == currentStepIndex -> activeColor
                else                  -> upcomingColor
            }

            val animatedColor by animateColorAsState(
                targetValue = targetColor,
                animationSpec = tween(durationMillis = 300),
                label = "step_segment_color_$i"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
        }
    }
}
