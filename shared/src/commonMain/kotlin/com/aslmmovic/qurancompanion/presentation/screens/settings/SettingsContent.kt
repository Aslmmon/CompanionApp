package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.DebugSettingsSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.LanguageSettingsSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.ReminderSettingsSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.SettingsTopBar
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.ThemeSettingsSection

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onUpdateReminderTime: (Int, Int) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onSimulateNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsTopBar(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(8.dp))

            ReminderSettingsSection(
                isReminderEnabled = uiState.userPreferences.isReminderEnabled,
                reminderHour = uiState.userPreferences.reminderHour,
                reminderMinute = uiState.userPreferences.reminderMinute,
                onToggleReminder = onToggleReminder,
                onUpdateReminderTime = onUpdateReminderTime
            )

            LanguageSettingsSection(
                preferredLanguage = uiState.userPreferences.preferredLanguage,
                onLanguageSelected = onLanguageSelected
            )

            ThemeSettingsSection(
                isDarkMode = uiState.userPreferences.isDarkMode,
                onThemeToggle = onThemeToggle
            )

            DebugSettingsSection(
                onSimulateNextDay = onSimulateNextDay
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    SettingsContent(
        uiState = SettingsUiState(
            userPreferences = UserPreferences(
                isReminderEnabled = true,
                reminderHour = 8,
                reminderMinute = 0,
                preferredLanguage = "en",
                isDarkMode = false
            )
        ),
        onBackClick = {},
        onToggleReminder = {},
        onUpdateReminderTime = { _, _ -> },
        onLanguageSelected = {},
        onThemeToggle = {},
        onSimulateNextDay = {}
    )
}
