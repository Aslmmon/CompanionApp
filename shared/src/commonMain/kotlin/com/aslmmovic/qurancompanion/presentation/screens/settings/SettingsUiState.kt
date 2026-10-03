package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

@Immutable
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val isDebug: Boolean = false,
    val appVersionName: String = "1.0.0",
    val buildNumber: Long = 1L,
    val buildType: String = "Release"
)
