package com.aslmmovic.qurancompanion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aslmmovic.qurancompanion.di.TestKoinModule
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.presentation.screens.library.LibraryContent
import com.aslmmovic.qurancompanion.presentation.screens.library.LibraryUiState
import com.aslmmovic.qurancompanion.ui.theme.SahabaCompanionTheme
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LibraryUiTest : BaseComposeTest() {

    @Test
    fun test_search_field_and_sahaba_item_selection() {
        var query by mutableStateOf("")
        var selectedSahaba by mutableStateOf<Sahaba?>(null)
        val sahaba = TestKoinModule.sampleSahaba

        composeRule.setContent {
            SahabaCompanionTheme {
                LibraryContent(
                    uiState = LibraryUiState(
                        searchQuery = query,
                        selectedCategory = SahabaCategory.ALL,
                        sahabaList = listOf(sahaba),
                        selectedSahabaDetail = selectedSahaba
                    ),
                    onSearchQueryChanged = { query = it },
                    onCategorySelected = {},
                    onSahabaSelected = { selectedSahaba = it },
                    onDismissDetail = { selectedSahaba = null }
                )
            }
        }

        // Verify search field is displayed and accepts input
        composeRule.onNodeWithTag("library_search_field").assertIsDisplayed()
        composeRule.onNodeWithTag("library_search_field").performTextInput("Abu")
        assertEquals("Abu", query)

        // Verify category chips
        composeRule.onNodeWithTag("library_category_chip_${SahabaCategory.ALL.name}").assertIsDisplayed()

        // Verify sahaba item card is displayed and clickable
        val itemTag = "sahaba_item_${sahaba.id}"
        composeRule.onNodeWithTag(itemTag).assertIsDisplayed()
        composeRule.onNodeWithTag(itemTag).performClick()
        assertNotNull(selectedSahaba)
        assertEquals(sahaba.id, selectedSahaba?.id)
    }
}
