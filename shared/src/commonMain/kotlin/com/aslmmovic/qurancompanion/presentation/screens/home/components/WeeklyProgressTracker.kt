package com.aslmmovic.qurancompanion.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.components.RubElHizbBadge
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_weekly_friday
import qurancompanion.shared.generated.resources.home_weekly_monday
import qurancompanion.shared.generated.resources.home_weekly_saturday
import qurancompanion.shared.generated.resources.home_weekly_sunday
import qurancompanion.shared.generated.resources.home_weekly_thursday
import qurancompanion.shared.generated.resources.home_weekly_tracker
import qurancompanion.shared.generated.resources.home_weekly_tuesday
import qurancompanion.shared.generated.resources.home_weekly_wednesday

@Composable
fun WeeklyProgressTracker(
    weeklyProgress: List<Boolean>,
    modifier: Modifier = Modifier
) {
    val dayLabels = remember {
        listOf(
            Res.string.home_weekly_monday,
            Res.string.home_weekly_tuesday,
            Res.string.home_weekly_wednesday,
            Res.string.home_weekly_thursday,
            Res.string.home_weekly_friday,
            Res.string.home_weekly_saturday,
            Res.string.home_weekly_sunday
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.home_weekly_tracker),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEachIndexed { index, labelRes ->
                    val isCompleted = weeklyProgress.getOrNull(index) ?: false
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        if (isCompleted) {
                            RubElHizbBadge(
                                number = "✓",
                                badgeSize = 30.dp,
                                starColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

