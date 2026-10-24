package com.aslmmovic.qurancompanion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aslmmovic.qurancompanion.presentation.screens.language.LanguageSelectionContent
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test
import kotlin.test.assertEquals

class LanguageOnboardingUiTest : BaseComposeTest() {

    @Test
    fun test_language_selection_buttons() {
        var selectedLang = ""

        composeRule.setContent {
            SahabaCompanionTheme {
                LanguageSelectionContent(
                    onLanguageSelected = { selectedLang = it }
                )
            }
        }

        // Verify Arabic button is displayed and selectable
        composeRule.onNodeWithTag("language_arabic_button").assertIsDisplayed()
        composeRule.onNodeWithTag("language_arabic_button").performClick()

        // Verify English button is displayed and selectable
        composeRule.onNodeWithTag("language_english_button").assertIsDisplayed()
        composeRule.onNodeWithTag("language_english_button").performClick()
    }
}
