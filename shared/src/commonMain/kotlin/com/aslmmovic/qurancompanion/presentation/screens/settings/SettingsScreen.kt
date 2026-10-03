package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
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
        onToggleTheme = viewModel::onToggleTheme,
        onLanguageSelected = viewModel::onLanguageSelected,
        onUpdateReminderTime = viewModel::onUpdateReminderTime,
        onToggleReminder = viewModel::onToggleReminder,
        onSimulateNextDay = viewModel::onSimulateNextDay,
        onTriggerNotification = viewModel::onTriggerNotification,
        onBackClick = viewModel::onBackClick,
        modifier = modifier
    )
}
