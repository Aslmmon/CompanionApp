package com.aslmmovic.qurancompanion.presentation.screens.library

import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.usecase.GetSahabaCatalogUseCase
import com.aslmmovic.qurancompanion.fakes.FakeSahabaRepository
import com.aslmmovic.qurancompanion.fakes.testSahaba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSahabaRepository
    private lateinit var getSahabaCatalogUseCase: GetSahabaCatalogUseCase

    private val testCatalog = listOf(
        testSahaba(id = "abu_bakr", name = "Abu Bakr as-Siddiq", epithet = "As-Siddiq", category = SahabaCategory.TEN_PROMISED),
        testSahaba(id = "umar", name = "Umar ibn al-Khattab", epithet = "Al-Faruq", category = SahabaCategory.TEN_PROMISED),
        testSahaba(id = "abu_ayyub", name = "Abu Ayyub al-Ansari", epithet = "Host of Prophet", category = SahabaCategory.ANSAR)
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSahabaRepository(testCatalog)
        getSahabaCatalogUseCase = GetSahabaCatalogUseCase(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = LibraryViewModel(
        getSahabaCatalogUseCase = getSahabaCatalogUseCase
    )

    @Test
    fun `test_AC05_givenViewModelInit_thenDefaultStateIsPopulated`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(SahabaCategory.ALL, viewModel.uiState.value.selectedCategory)
        assertTrue(viewModel.uiState.value.sahabaList.isNotEmpty())
        assertEquals(3, viewModel.uiState.value.sahabaList.size)

        collectJob.cancel()
    }

    @Test
    fun `test_AC06_givenCategorySelection_whenChanged_thenStateFiltersList`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onCategorySelected(SahabaCategory.ANSAR)
        advanceUntilIdle()

        assertEquals(SahabaCategory.ANSAR, viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.sahabaList.size)
        assertEquals("abu_ayyub", viewModel.uiState.value.sahabaList.first().id)

        collectJob.cancel()
    }

    @Test
    fun `test_AC07_givenSearchQuery_whenUpdated_thenStateFiltersList`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Umar")
        advanceUntilIdle()

        assertEquals("Umar", viewModel.uiState.value.searchQuery)
        assertEquals(1, viewModel.uiState.value.sahabaList.size)
        assertEquals("umar", viewModel.uiState.value.sahabaList.first().id)

        collectJob.cancel()
    }

    @Test
    fun `test_AC08_givenSahabaCardClicked_thenSelectedSahabaDetailSet`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val sahabaToSelect = testCatalog.first()
        viewModel.onSahabaSelected(sahabaToSelect)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.selectedSahabaDetail)
        assertEquals("abu_bakr", viewModel.uiState.value.selectedSahabaDetail?.id)

        collectJob.cancel()
    }

    @Test
    fun `test_AC09_givenDetailOpen_whenDismissed_thenSelectedSahabaDetailIsNull`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSahabaSelected(testCatalog.first())
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedSahabaDetail)

        viewModel.onDismissDetail()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedSahabaDetail)

        collectJob.cancel()
    }
}
