package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.journey_bookmarked
import qurancompanion.shared.generated.resources.journey_unbookmarked

@Composable
fun JourneyFlowScreen(
    onNavigateToCompletion: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: JourneyViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val bookmarkedMessage = stringResource(Res.string.journey_bookmarked)
    val unbookmarkedMessage = stringResource(Res.string.journey_unbookmarked)

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                JourneyUiEffect.NavigateToCompletion -> onNavigateToCompletion()
                JourneyUiEffect.NavigateToHome       -> onNavigateToHome()
                is JourneyUiEffect.ShowBookmarkFeedback -> {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = if (effect.isBookmarked) bookmarkedMessage else unbookmarkedMessage,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        JourneyFlowContent(
            journey = uiState.journey,
            currentStepIndex = uiState.currentStepIndex,
            isBookmarked = uiState.isBookmarked,
            isLoading = uiState.isLoading,
            onBackPressed = viewModel::onBackPressed,
            onToggleBookmark = viewModel::onToggleBookmark,
            onNextStep = viewModel::onNextStep,
            onFinish = viewModel::onFinish,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
