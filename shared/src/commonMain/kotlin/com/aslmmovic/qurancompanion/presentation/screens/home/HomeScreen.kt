package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToJourneyFlow: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                HomeUiEffect.NavigateToJourneyFlow -> onNavigateToJourneyFlow()
            }
        }
    }

    HomeContent(
        journey = uiState.journey,
        isCompleted = uiState.isCompleted,
        userPreferences = uiState.userPreferences,
        weeklyProgress = uiState.weeklyProgress,
        tomorrowJourney = uiState.tomorrowJourney,
        onBeginClick = viewModel::onBeginJourneyClick,
        onResetClick = viewModel::onResetCompletionClick,
        onNextJourneyClick = viewModel::onNextJourneyClick,
        onLanguageSelected = viewModel::onLanguageSelected,
        onThemeToggle = viewModel::onToggleTheme,
        onToggleReminder = viewModel::onToggleReminder,
        onUpdateReminderTime = viewModel::onUpdateReminderTime,
    )
}
