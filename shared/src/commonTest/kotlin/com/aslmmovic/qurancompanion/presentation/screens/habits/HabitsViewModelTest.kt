package com.aslmmovic.qurancompanion.presentation.screens.habits

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.usecase.AddHabitFromCatalogUseCase
import com.aslmmovic.qurancompanion.domain.usecase.CreateCustomHabitUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetActiveHabitsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetCuratedHabitsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RemoveHabitUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ToggleHabitCompletionUseCase
import com.aslmmovic.qurancompanion.fakes.FakeHabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeHabitRepository
    private lateinit var getActiveHabitsUseCase: GetActiveHabitsUseCase
    private lateinit var getCuratedHabitsUseCase: GetCuratedHabitsUseCase
    private lateinit var toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
    private lateinit var addHabitFromCatalogUseCase: AddHabitFromCatalogUseCase
    private lateinit var createCustomHabitUseCase: CreateCustomHabitUseCase
    private lateinit var removeHabitUseCase: RemoveHabitUseCase

    private val habit1 = Habit("1", "Morning Adhkar", "Desc 1", HabitCategory.ADHKAR)
    private val habit2 = Habit("2", "Duha Prayer", "Desc 2", HabitCategory.DAILY_PRAYERS)
    private val habit3 = Habit("3", "Tahajjud", "Desc 3", HabitCategory.DAILY_PRAYERS)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeHabitRepository(
            initialActive = listOf(habit1, habit2),
            initialCatalog = listOf(habit1, habit2, habit3)
        )
        getActiveHabitsUseCase = GetActiveHabitsUseCase(fakeRepository)
        getCuratedHabitsUseCase = GetCuratedHabitsUseCase(fakeRepository)
        toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(fakeRepository)
        addHabitFromCatalogUseCase = AddHabitFromCatalogUseCase(fakeRepository)
        createCustomHabitUseCase = CreateCustomHabitUseCase(fakeRepository)
        removeHabitUseCase = RemoveHabitUseCase(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HabitsViewModel(
        getActiveHabitsUseCase = getActiveHabitsUseCase,
        getCuratedHabitsUseCase = getCuratedHabitsUseCase,
        toggleHabitCompletionUseCase = toggleHabitCompletionUseCase,
        addHabitFromCatalogUseCase = addHabitFromCatalogUseCase,
        createCustomHabitUseCase = createCustomHabitUseCase,
        removeHabitUseCase = removeHabitUseCase
    )

    @Test
    fun `test_AC06_progressCalculation_updatesAccurately`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalCount)
        assertEquals(0, viewModel.uiState.value.completedCount)
        assertEquals(0, viewModel.uiState.value.progressPercentage)

        viewModel.onToggleHabit("1")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.completedCount)
        assertEquals(50, viewModel.uiState.value.progressPercentage)

        collectJob.cancel()
    }

    @Test
    fun `test_AC07_whenAllHabitsCompleted_emitsCelebrationEffect`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        var celebrationReceived = false
        val effectJob = launch {
            viewModel.uiEffect.collect { effect ->
                if (effect is HabitsUiEffect.ShowCelebration) {
                    celebrationReceived = true
                }
            }
        }
        advanceUntilIdle()

        viewModel.onToggleHabit("1")
        viewModel.onToggleHabit("2")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.completedCount)
        assertEquals(100, viewModel.uiState.value.progressPercentage)
        assertTrue(viewModel.uiState.value.isAllCompleted)
        assertTrue(celebrationReceived)

        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `test_AC08_openAndCloseAddSheet`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAddSheetOpen)

        viewModel.onOpenAddSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAddSheetOpen)

        viewModel.onCloseAddSheet()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isAddSheetOpen)

        collectJob.cancel()
    }

    @Test
    fun `test_detailBottomSheet_selectionAndDismissal`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSelectHabitForDetail(habit1)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedHabitForDetail)
        assertEquals("1", viewModel.uiState.value.selectedHabitForDetail?.id)

        viewModel.onDismissHabitDetail()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.selectedHabitForDetail)

        collectJob.cancel()
    }

    @Test
    fun onAddHabitFromCatalog_addsHabitAndClosesSheet() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onOpenAddSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAddSheetOpen)

        viewModel.onAddHabitFromCatalog("3")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAddSheetOpen)
        assertTrue(viewModel.uiState.value.activeHabits.any { it.id == "3" })

        collectJob.cancel()
    }

    @Test
    fun onCreateCustomHabit_addsHabitAndClosesSheet() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onOpenAddSheet()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isAddSheetOpen)

        viewModel.onCreateCustomHabit("Daily Sadaqah", "Give charity", HabitCategory.CHARACTER)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAddSheetOpen)
        assertTrue(viewModel.uiState.value.activeHabits.any { it.title == "Daily Sadaqah" })

        collectJob.cancel()
    }

    @Test
    fun onRemoveHabit_removesHabitAndDismissesDetail() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSelectHabitForDetail(habit1)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.selectedHabitForDetail)

        viewModel.onRemoveHabit("1")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedHabitForDetail)
        assertFalse(viewModel.uiState.value.activeHabits.any { it.id == "1" })

        collectJob.cancel()
    }

    @Test
    fun onCatalogCategorySelected_filtersCatalogByCategory() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onCatalogCategorySelected(HabitCategory.DAILY_PRAYERS)
        advanceUntilIdle()

        assertEquals(HabitCategory.DAILY_PRAYERS, viewModel.uiState.value.selectedCatalogCategory)
        assertTrue(viewModel.uiState.value.catalogHabits.all { it.category == HabitCategory.DAILY_PRAYERS })

        collectJob.cancel()
    }

    @Test
    fun onCatalogSearchQueryChanged_filtersCatalogByQuery() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onCatalogSearchQueryChanged("Tahajjud")
        advanceUntilIdle()

        assertEquals("Tahajjud", viewModel.uiState.value.catalogSearchQuery)
        assertEquals(1, viewModel.uiState.value.catalogHabits.size)
        assertEquals("3", viewModel.uiState.value.catalogHabits.first().id)

        collectJob.cancel()
    }

    @Test
    fun onCloseAddSheet_resetsSearchQueryAndCategoryToAll() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onCatalogSearchQueryChanged("query")
        viewModel.onCatalogCategorySelected(HabitCategory.ADHKAR)
        advanceUntilIdle()

        viewModel.onCloseAddSheet()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.catalogSearchQuery)
        assertEquals(HabitCategory.ALL, viewModel.uiState.value.selectedCatalogCategory)
        assertFalse(viewModel.uiState.value.isAddSheetOpen)

        collectJob.cancel()
    }

    @Test
    fun catalogHabits_excludesAlreadyActiveHabits() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // Active habits: habit1, habit2. Catalog: habit1, habit2, habit3.
        assertEquals(1, viewModel.uiState.value.catalogHabits.size)
        assertEquals("3", viewModel.uiState.value.catalogHabits.first().id)

        collectJob.cancel()
    }

    @Test
    fun showCelebration_firesExactlyOnce_notOnResubscription() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        var celebrationCount = 0
        val effectJob = launch {
            viewModel.uiEffect.collect { effect ->
                if (effect is HabitsUiEffect.ShowCelebration) {
                    celebrationCount++
                }
            }
        }
        advanceUntilIdle()

        viewModel.onToggleHabit("1")
        viewModel.onToggleHabit("2")
        advanceUntilIdle()

        assertEquals(1, celebrationCount)

        // Collecting another action shouldn't trigger celebration again if already completed
        viewModel.onOpenAddSheet()
        advanceUntilIdle()
        assertEquals(1, celebrationCount)

        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun progressCalculation_noHabits_percentageIsZero() = runTest {
        val emptyRepo = FakeHabitRepository(initialActive = emptyList(), initialCatalog = emptyList())
        val viewModel = HabitsViewModel(
            getActiveHabitsUseCase = GetActiveHabitsUseCase(emptyRepo),
            getCuratedHabitsUseCase = GetCuratedHabitsUseCase(emptyRepo),
            toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(emptyRepo),
            addHabitFromCatalogUseCase = AddHabitFromCatalogUseCase(emptyRepo),
            createCustomHabitUseCase = CreateCustomHabitUseCase(emptyRepo),
            removeHabitUseCase = RemoveHabitUseCase(emptyRepo)
        )
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totalCount)
        assertEquals(0, viewModel.uiState.value.progressPercentage)

        collectJob.cancel()
    }

    @Test
    fun habitsViewModel_initialState_isLoading() {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }
}
