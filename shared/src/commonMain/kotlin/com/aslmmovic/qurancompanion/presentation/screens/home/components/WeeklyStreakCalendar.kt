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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.ui.theme.SahabaEvergreen
import com.aslmmovic.qurancompanion.ui.theme.SahabaTextMuted
import com.aslmmovic.qurancompanion.ui.theme.SahabaWarmGold
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_streak_counter_format
import qurancompanion.shared.generated.resources.home_weekly_journey_subtitle
import qurancompanion.shared.generated.resources.home_weekly_journey_title

@Composable
fun WeeklyStreakCalendar(
    weeklyProgress: List<Boolean>,
    streakCount: Int,
    currentDayOfWeek: Int, // 1 = Monday, 7 = Sunday
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: title + subtitle on left, streak counter on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.home_weekly_journey_title),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.home_weekly_journey_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SahabaTextMuted
                    )
                }

                Text(
                    text = stringResource(Res.string.home_streak_counter_format, streakCount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = SahabaWarmGold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7-day indicators row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..7).forEach { dayIndex ->
                    val isCompleted = weeklyProgress.getOrElse(dayIndex - 1) { false }
                    val isToday = dayIndex == currentDayOfWeek
                    val isSunday = dayIndex == 7
                    val label = dayLabels[dayIndex - 1]

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isCompleted -> Modifier.background(SahabaEvergreen)
                                        isToday -> Modifier
                                            .background(SahabaWarmGold.copy(alpha = 0.12f))
                                            .border(2.dp, SahabaWarmGold, CircleShape)
                                        else -> Modifier
                                            .background(Color.Transparent)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> {
                                    Text(
                                        text = "✓",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color.White
                                    )
                                }
                                isToday -> {
                                    Text(
                                        text = "◉",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = SahabaWarmGold
                                    )
                                }
                                isSunday -> {
                                    Text(
                                        text = "✧",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = SahabaWarmGold.copy(alpha = 0.6f)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = SahabaTextMuted.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isToday) SahabaWarmGold else SahabaTextMuted
                        )
                    }
                }
            }
        }
    }
}
