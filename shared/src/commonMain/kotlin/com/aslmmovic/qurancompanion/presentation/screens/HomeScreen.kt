package com.aslmmovic.qurancompanion.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeContent
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val journey by viewModel.journey.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isCompleted.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val weeklyProgress by viewModel.weeklyProgress.collectAsStateWithLifecycle()
    val tomorrowJourney by viewModel.tomorrowJourney.collectAsStateWithLifecycle()

    HomeContent(
        journey = journey,
        isCompleted = isCompleted,
        userPreferences = userPreferences,
        weeklyProgress = weeklyProgress,
        tomorrowJourney = tomorrowJourney,
        onBeginClick = viewModel::onBeginJourneyClick,
        onResetClick = viewModel::onResetCompletionClick,
        onLanguageSelected = viewModel::onLanguageSelected
    )
}
