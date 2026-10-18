package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.presentation.screens.library.components.SahabaDetailBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToJourneyFlow: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLibrary: ((SahabaCategory?) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                HomeUiEffect.NavigateToJourneyFlow -> onNavigateToJourneyFlow()
                HomeUiEffect.NavigateToSettings -> onNavigateToSettings()
                is HomeUiEffect.NavigateToLibrary -> onNavigateToLibrary?.invoke(effect.category)
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onBeginClick = viewModel::onBeginJourneyClick,
        onBookmarkClick = viewModel::onBookmarkClick,
        onNotificationClick = viewModel::onNotificationClick,
        onSahabaClick = viewModel::onSahabaCardClick,
        onViewAllTenPromisedClick = viewModel::onViewAllTenPromisedClick,
        onToggleHabitClick = viewModel::onToggleHabitClick,
        modifier = modifier
    )

    uiState.selectedSahabaDetail?.let { sahaba ->
        SahabaDetailBottomSheet(
            sahaba = sahaba,
            onDismissRequest = viewModel::onDismissSahabaDetail
        )
    }
}
