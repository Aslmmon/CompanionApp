package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToJourneyFlow: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                HomeUiEffect.NavigateToJourneyFlow -> onNavigateToJourneyFlow()
                HomeUiEffect.NavigateToSettings -> onNavigateToSettings()
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
        onSettingsClick = viewModel::onSettingsClick,
        onThemeToggle = viewModel::onToggleTheme,
        modifier = modifier,
    )
}
