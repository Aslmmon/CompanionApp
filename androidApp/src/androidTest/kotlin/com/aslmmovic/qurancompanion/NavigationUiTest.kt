package com.aslmmovic.qurancompanion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aslmmovic.qurancompanion.presentation.navigation.MainNavigationScaffold
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test

class NavigationUiTest : BaseComposeTest() {

    @Test
    fun test_tabs_are_displayed() {
        composeRule.setContent {
            SahabaCompanionTheme {
                MainNavigationScaffold(
                    onNavigateToJourneyFlow = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeRule.onNodeWithTag("tab_today").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_habits").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_library").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_settings").assertIsDisplayed()
    }

    @Test
    fun test_switchTabs_today_to_habits() {
        composeRule.setContent {
            SahabaCompanionTheme {
                MainNavigationScaffold(
                    onNavigateToJourneyFlow = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeRule.onNodeWithTag("tab_habits").performClick()
        composeRule.onNodeWithTag("habits_add_button").assertIsDisplayed()
    }

    @Test
    fun test_switchTabs_habits_to_library() {
        composeRule.setContent {
            SahabaCompanionTheme {
                MainNavigationScaffold(
                    onNavigateToJourneyFlow = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeRule.onNodeWithTag("tab_library").performClick()
        composeRule.onNodeWithTag("library_search_field").assertIsDisplayed()
    }

    @Test
    fun test_switchTabs_library_to_settings() {
        composeRule.setContent {
            SahabaCompanionTheme {
                MainNavigationScaffold(
                    onNavigateToJourneyFlow = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeRule.onNodeWithTag("tab_settings").performClick()
        composeRule.onNodeWithTag("settings_reminder_toggle").assertIsDisplayed()
    }
}
