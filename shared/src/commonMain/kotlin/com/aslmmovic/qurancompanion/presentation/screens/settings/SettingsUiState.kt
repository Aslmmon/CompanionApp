package com.aslmmovic.qurancompanion.presentation.screens.settings

import com.aslmmovic.qurancompanion.domain.model.UserPreferences

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isPatron: Boolean = false,
    val isSupporter: Boolean = false,
    val isLoading: Boolean = false,
    val isDebug: Boolean = false,
    val appVersionName: String = "1.0.0",
    val buildNumber: Long = 1L,
    val buildType: String = "Release"
)
