package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JourneyFlowScreen(
    onNavigateToCompletion: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: JourneyViewModel = koinViewModel(),
) {
    val journey by viewModel.journey.collectAsStateWithLifecycle()
    val currentStepIndex by viewModel.currentStepIndex.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                JourneyUiEffect.NavigateToCompletion -> onNavigateToCompletion()
                JourneyUiEffect.NavigateToHome       -> onNavigateToHome()
            }
        }
    }

    JourneyFlowContent(
        journey = journey,
        currentStepIndex = currentStepIndex,
        onPreviousStep = viewModel::onPreviousStep,
        onNextStep = viewModel::onNextStep,
        onFinish = viewModel::onFinish,
    )
}
