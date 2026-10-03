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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.AppearanceSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.AppInfoFooter
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.DebugSettingsSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.LanguageBottomSheet
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.NotificationSection
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.ReminderTimePickerDialog
import com.aslmmovic.qurancompanion.presentation.screens.settings.components.SettingsHeader

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onToggleTheme: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onUpdateReminderTime: (Int, Int) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSimulateNextDay: () -> Unit,
    onTriggerNotification: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsHeader()

            AppearanceSection(
                isDarkMode = uiState.userPreferences.isDarkMode ?: false,
                preferredLanguage = uiState.userPreferences.preferredLanguage,
                onThemeToggle = onToggleTheme,
                onOpenLanguageSheet = { showLanguageSheet = true }
            )

            NotificationSection(
                isReminderEnabled = uiState.userPreferences.isReminderEnabled,
                reminderHour = uiState.userPreferences.reminderHour,
                reminderMinute = uiState.userPreferences.reminderMinute,
                onToggleReminder = onToggleReminder,
                onOpenTimePicker = { showTimePickerDialog = true }
            )

            AppInfoFooter(
                versionName = uiState.appVersionName,
                buildNumber = uiState.buildNumber,
                buildType = uiState.buildType
            )

            if (uiState.isDebug) {
                DebugSettingsSection(
                    onSimulateNextDay = onSimulateNextDay,
                    onTriggerNotification = onTriggerNotification,
                    isNotificationEnabled = uiState.userPreferences.isReminderEnabled
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showLanguageSheet) {
            LanguageBottomSheet(
                currentLanguage = uiState.userPreferences.preferredLanguage,
                onLanguageSelected = onLanguageSelected,
                onDismissRequest = { showLanguageSheet = false }
            )
        }

        if (showTimePickerDialog) {
            ReminderTimePickerDialog(
                initialHour = uiState.userPreferences.reminderHour,
                initialMinute = uiState.userPreferences.reminderMinute,
                onConfirm = { hour, minute ->
                    onUpdateReminderTime(hour, minute)
                },
                onDismissRequest = { showTimePickerDialog = false }
            )
        }
    }
}
