package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.presentation.screens.home.components.BismillahHeader
import com.aslmmovic.qurancompanion.presentation.screens.home.components.CompletedStateContent
import com.aslmmovic.qurancompanion.presentation.screens.home.components.HomeHeader
import com.aslmmovic.qurancompanion.presentation.screens.home.components.ReadyStateContent
import com.aslmmovic.qurancompanion.presentation.screens.home.components.SettingsBottomSheet
import com.aslmmovic.qurancompanion.presentation.screens.home.components.WeeklyProgressTracker
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_loading

@Composable
fun HomeContent(
    journey: Journey?,
    isCompleted: Boolean,
    userPreferences: UserPreferences,
    weeklyProgress: List<Boolean>,
    tomorrowJourney: Journey?,
    onBeginClick: () -> Unit,
    onResetClick: () -> Unit,
    onNextJourneyClick: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = if (isCompleted) 8.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isCompleted) {
                Spacer(modifier = Modifier.height(24.dp))
                BismillahHeader()
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            HomeHeader(
                isDarkMode = userPreferences.isDarkMode ?: isSystemInDarkTheme(),
                onThemeToggleClick = onThemeToggle,
                onSettingsClick = { showSettingsBottomSheet = true }
            )

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle — emotional hook for today's journey
                Text(
                    text = journey?.subtitle ?: stringResource(Res.string.home_loading),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            WeeklyProgressTracker(
                weeklyProgress = weeklyProgress,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(if (isCompleted) 12.dp else 24.dp))

            AnimatedContent(
                targetState = isCompleted,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 4 } togetherWith
                            fadeOut() + slideOutVertically { -it / 4 }
                },
                label = "home_state"
            ) { completed ->
                if (completed) {
                    CompletedStateContent(
                        journey = journey,
                        tomorrowJourney = tomorrowJourney,
                        onResetClick = onResetClick
                    )
                } else {
                    ReadyStateContent(
                        journey = journey,
                        onBeginClick = onBeginClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showSettingsBottomSheet) {
            SettingsBottomSheet(
                preferredLanguage = userPreferences.preferredLanguage,
                onLanguageSelected = onLanguageSelected,
                onSimulateNextDay = onNextJourneyClick,
                onDismissRequest = { showSettingsBottomSheet = false }
            )
        }
    }
}

@Preview
@Composable
fun HomeContentPreview() {
    HomeContent(
        journey = null,
        isCompleted = false,
        userPreferences = UserPreferences(),
        weeklyProgress = listOf(true, false, true, false, false, false, false),
        tomorrowJourney = null,
        onBeginClick = {},
        onResetClick = {},
        onNextJourneyClick = {},
        onLanguageSelected = {},
        onThemeToggle = {}
    )
}
