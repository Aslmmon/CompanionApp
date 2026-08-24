package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CompletionScreen(
    onNavigateToHome: () -> Unit,
    viewModel: JourneyViewModel = koinViewModel(),
) {
    val journey by viewModel.journey.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                JourneyUiEffect.NavigateToHome       -> onNavigateToHome()
                JourneyUiEffect.NavigateToCompletion -> { /* already here */ }
            }
        }
    }

    CompletionContent(
        journey = journey,
        onReturnHome = viewModel::onReturnHome,
    )
}
