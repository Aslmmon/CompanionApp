package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SettingsContent(
        uiState = uiState,
        onBackClick = viewModel::onBackClick,
        onUpdateReminderTime = viewModel::onUpdateReminderTime,
        onLanguageSelected = viewModel::onLanguageSelected,
        onThemeToggle = viewModel::onToggleTheme,
        onSimulateNextDay = viewModel::onSimulateNextDay,
        onTriggerNotification = viewModel::onTriggerNotification,
        modifier = modifier
    )
}
