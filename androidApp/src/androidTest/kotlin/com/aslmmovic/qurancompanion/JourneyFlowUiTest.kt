package com.aslmmovic.qurancompanion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aslmmovic.qurancompanion.di.TestKoinModule
import com.aslmmovic.qurancompanion.presentation.screens.home.components.ReadyStateContent
import com.aslmmovic.qurancompanion.presentation.screens.journey.JourneyFlowContent
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JourneyFlowUiTest : BaseComposeTest() {

    @Test
    fun test_tap_begin_journey_button() {
        var beginClicked = false
        composeRule.setContent {
            SahabaCompanionTheme {
                ReadyStateContent(
                    journey = TestKoinModule.sampleJourney,
                    onBeginClick = { beginClicked = true }
                )
            }
        }

        composeRule.onNodeWithTag("home_today_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_begin_journey_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_begin_journey_button").performClick()
        assertTrue(beginClicked)
    }

    @Test
    fun test_journeyFlow_navigation_and_bookmark() {
        var currentStep by mutableIntStateOf(0)
        var isBookmarked by mutableStateOf(false)
        var navigatedBack = false

        composeRule.setContent {
            SahabaCompanionTheme {
                JourneyFlowContent(
                    journey = TestKoinModule.sampleJourney,
                    currentStepIndex = currentStep,
                    isBookmarked = isBookmarked,
                    isLoading = false,
                    onBackPressed = { navigatedBack = true },
                    onToggleBookmark = { isBookmarked = !isBookmarked },
                    onNextStep = { currentStep++ },
                    onFinish = {}
                )
            }
        }

        // Verify bookmark button
        composeRule.onNodeWithTag("journey_bookmark_button").assertIsDisplayed()
        composeRule.onNodeWithTag("journey_bookmark_button").performClick()
        assertTrue(isBookmarked)

        // Verify next button
        composeRule.onNodeWithTag("journey_next_button").assertIsDisplayed()
        composeRule.onNodeWithTag("journey_next_button").performClick()
        assertEquals(1, currentStep)

        // Verify back button
        composeRule.onNodeWithTag("journey_back_button").assertIsDisplayed()
        composeRule.onNodeWithTag("journey_back_button").performClick()
        assertTrue(navigatedBack)
    }
}
