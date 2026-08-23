package com.aslmmovic.qurancompanion.presentation.screens.settings

/**
 * One-shot side effects emitted by [SettingsViewModel].
 */
sealed interface SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect
}
