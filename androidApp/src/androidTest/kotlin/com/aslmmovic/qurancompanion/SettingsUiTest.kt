package com.aslmmovic.qurancompanion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsContent
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsUiState
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test
import kotlin.test.assertTrue

class SettingsUiTest : BaseComposeTest() {

    @Test
    fun test_settings_toggles_and_debug_notification_button() {
        var isDarkMode by mutableStateOf(false)
        var isReminderEnabled by mutableStateOf(true)
        var notificationTriggered = false

        composeRule.setContent {
            SahabaCompanionTheme {
                SettingsContent(
                    uiState = SettingsUiState(
                        userPreferences = UserPreferences(
                            isDarkMode = isDarkMode,
                            isReminderEnabled = isReminderEnabled
                        ),
                        isDebug = true
                    ),
                    onToggleTheme = { isDarkMode = it },
                    onLanguageSelected = {},
                    onUpdateReminderTime = { _, _ -> },
                    onToggleReminder = { isReminderEnabled = it },
                    onSimulateNextDay = {},
                    onTriggerNotification = { notificationTriggered = true }
                )
            }
        }

        // Verify dark mode toggle
        composeRule.onNodeWithTag("settings_dark_mode_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_dark_mode_toggle").performClick()
        assertTrue(isDarkMode)

        // Verify reminder toggle
        composeRule.onNodeWithTag("settings_reminder_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_reminder_toggle").performClick()

        // Verify language row
        composeRule.onNodeWithTag("settings_language_row").assertIsDisplayed()

        // Verify trigger notification button
        composeRule.onNodeWithTag("settings_trigger_notification_button").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_trigger_notification_button").performClick()
        assertTrue(notificationTriggered)
    }
}
