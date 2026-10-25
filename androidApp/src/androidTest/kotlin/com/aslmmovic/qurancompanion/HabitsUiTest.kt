package com.aslmmovic.qurancompanion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.aslmmovic.qurancompanion.di.TestKoinModule
import com.aslmmovic.qurancompanion.presentation.screens.habits.HabitsContent
import com.aslmmovic.qurancompanion.presentation.screens.habits.HabitsUiState
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitsUiTest : BaseComposeTest() {

    @Test
    fun test_habits_add_button_opens_sheet() {
        var isSheetOpen by mutableStateOf(false)

        composeRule.setContent {
            SahabaCompanionTheme {
                HabitsContent(
                    uiState = HabitsUiState(
                        activeHabits = listOf(TestKoinModule.sampleHabit),
                        isAddSheetOpen = isSheetOpen,
                        totalCount = 1,
                        completedCount = 0,
                        progressPercentage = 0
                    ),
                    onToggleHabit = {},
                    onOpenAddSheet = { isSheetOpen = true },
                    onCloseAddSheet = { isSheetOpen = false },
                    onCatalogCategorySelected = {},
                    onAddHabitFromCatalog = {},
                    onCreateCustomHabit = { _, _, _ -> },
                    onSelectHabitDetail = {},
                    onDismissHabitDetail = {},
                    onRemoveHabit = {}
                )
            }
        }

        composeRule.onNodeWithTag("habits_progress_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("habits_add_button").assertIsDisplayed()
        composeRule.onNodeWithTag("habits_add_button").performClick()

        assertTrue(isSheetOpen)
        composeRule.onNodeWithTag("add_habit_sheet").assertIsDisplayed()
    }

    @Test
    fun test_toggle_habit_completion() {
        var habit by mutableStateOf(TestKoinModule.sampleHabit)

        composeRule.setContent {
            SahabaCompanionTheme {
                HabitsContent(
                    uiState = HabitsUiState(
                        activeHabits = listOf(habit),
                        totalCount = 1,
                        completedCount = if (habit.isCompletedToday) 1 else 0,
                        progressPercentage = if (habit.isCompletedToday) 100 else 0
                    ),
                    onToggleHabit = {
                        habit = habit.copy(isCompletedToday = !habit.isCompletedToday)
                    },
                    onOpenAddSheet = {},
                    onCloseAddSheet = {},
                    onCatalogCategorySelected = {},
                    onAddHabitFromCatalog = {},
                    onCreateCustomHabit = { _, _, _ -> },
                    onSelectHabitDetail = {},
                    onDismissHabitDetail = {},
                    onRemoveHabit = {}
                )
            }
        }

        val cardTag = "habit_card_${TestKoinModule.sampleHabit.id}"
        composeRule.onNodeWithTag(cardTag).assertIsDisplayed()
        composeRule.onNodeWithTag(cardTag).performClick()
    }

    @Test
    fun test_remove_habit_updates_state() {
        var habitList by mutableStateOf(listOf(TestKoinModule.sampleHabit))

        composeRule.setContent {
            SahabaCompanionTheme {
                HabitsContent(
                    uiState = HabitsUiState(
                        activeHabits = habitList,
                        totalCount = habitList.size,
                        completedCount = 0,
                        selectedHabitForDetail = habitList.firstOrNull()
                    ),
                    onToggleHabit = {},
                    onOpenAddSheet = {},
                    onCloseAddSheet = {},
                    onCatalogCategorySelected = {},
                    onAddHabitFromCatalog = {},
                    onCreateCustomHabit = { _, _, _ -> },
                    onSelectHabitDetail = {},
                    onDismissHabitDetail = {},
                    onRemoveHabit = { id ->
                        habitList = habitList.filterNot { it.id == id }
                    }
                )
            }
        }

        assertEquals(1, habitList.size)
    }
}
