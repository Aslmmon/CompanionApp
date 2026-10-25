package com.aslmmovic.qurancompanion.presentation.screens.home

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.usecase.GetActiveHabitsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetBookmarkedJourneyIdsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetJourneyStreakUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetSahabaCatalogUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ToggleHabitCompletionUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ToggleJourneyBookmarkUseCase
import com.aslmmovic.qurancompanion.fakes.FakeDateTimeProvider
import com.aslmmovic.qurancompanion.fakes.FakeHabitRepository
import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.FakeSahabaRepository
import com.aslmmovic.qurancompanion.fakes.FakeUserPreferencesRepository
import com.aslmmovic.qurancompanion.fakes.testJourney
import com.aslmmovic.qurancompanion.fakes.testSahaba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val journeyRepo = FakeJourneyRepository()
    private val prefsRepo = FakeUserPreferencesRepository()
    private val sahabaRepo = FakeSahabaRepository()
    private val habitRepo = FakeHabitRepository()
    private val dateTimeProvider = FakeDateTimeProvider(
        dayOfWeek = 5,
        hijriDateString = "14 RAMADAN 1447 AH"
    )

    private fun createViewModel() = HomeViewModel(
        getTodayJourneyUseCase = GetTodayJourneyUseCase(journeyRepo),
        getTomorrowJourneyUseCase = GetTomorrowJourneyUseCase(journeyRepo),
        getWeeklyProgressUseCase = GetWeeklyProgressUseCase(journeyRepo),
        isJourneyCompletedUseCase = IsJourneyCompletedUseCase(journeyRepo),
        resetJourneyUseCase = ResetJourneyUseCase(journeyRepo),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(prefsRepo),
        savePreferencesUseCase = SavePreferencesUseCase(prefsRepo),
        getDebugDayOffsetUseCase = GetDebugDayOffsetUseCase(journeyRepo),
        incrementDebugDayOffsetUseCase = IncrementDebugDayOffsetUseCase(journeyRepo),
        dateTimeProvider = dateTimeProvider,
        getJourneyStreakUseCase = GetJourneyStreakUseCase(journeyRepo),
        getBookmarkedJourneyIdsUseCase = GetBookmarkedJourneyIdsUseCase(prefsRepo),
        toggleJourneyBookmarkUseCase = ToggleJourneyBookmarkUseCase(prefsRepo),
        getSahabaCatalogUseCase = GetSahabaCatalogUseCase(sahabaRepo),
        getActiveHabitsUseCase = GetActiveHabitsUseCase(habitRepo),
        toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(habitRepo)
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads journey, streak, hijriDate, tenPromised and habit into uiState`() = runTest {
        val today = testJourney(id = "abu_bakr_1", title = "Abu Bakr")
        journeyRepo.todayJourney = today
        journeyRepo.allJourneys = listOf(today)
        journeyRepo.streakFlow.value = 12

        val umar = testSahaba(id = "umar", name = "Umar ibn al-Khattab", category = SahabaCategory.TEN_PROMISED)
        val uthman = testSahaba(id = "uthman", name = "Uthman ibn Affan", category = SahabaCategory.TEN_PROMISED)
        val ansarSahaba = testSahaba(id = "ansar_1", name = "Ansar 1", category = SahabaCategory.ANSAR)
        sahabaRepo.setCatalog(listOf(umar, uthman, ansarSahaba))

        val habit = Habit(
            id = "smile_everyone",
            title = "Smile at Everyone",
            description = "Smiling is charity",
            category = HabitCategory.CHARACTER,
            isCompletedToday = false
        )
        habitRepo.setActiveHabits(listOf(habit))

        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(today, state.journey)
        assertEquals(12, state.streakCount)
        assertEquals("14 RAMADAN 1447 AH", state.hijriDate)
        assertEquals(5, state.currentDayOfWeek)
        assertEquals(2, state.tenPromisedCompanions.size)
        assertEquals(listOf(umar, uthman), state.tenPromisedCompanions)
        assertEquals(habit, state.todayHabit)
        assertFalse(state.isHabitCompleted)
        assertFalse(state.isBookmarked)

        collectJob.cancel()
    }

    @Test
    fun `onBeginJourneyClick emits NavigateToJourneyFlow effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch { viewModel.uiEffects.toList(effects) }

        viewModel.onBeginJourneyClick()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(HomeUiEffect.NavigateToJourneyFlow, effects.first())
        job.cancel()
    }

    @Test
    fun `onBookmarkClick toggles journey bookmark status`() = runTest {
        val today = testJourney(id = "journey_1")
        journeyRepo.todayJourney = today
        journeyRepo.allJourneys = listOf(today)

        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isBookmarked)

        viewModel.onBookmarkClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBookmarked)

        viewModel.onBookmarkClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isBookmarked)
        collectJob.cancel()
    }

    @Test
    fun `onToggleHabitClick toggles habit completion status`() = runTest {
        val habit = Habit(
            id = "smile_everyone",
            title = "Smile at Everyone",
            description = "Smiling is charity",
            category = HabitCategory.CHARACTER,
            isCompletedToday = false
        )
        habitRepo.setActiveHabits(listOf(habit))

        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isHabitCompleted)

        viewModel.onToggleHabitClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isHabitCompleted)
        collectJob.cancel()
    }

    @Test
    fun `onSahabaCardClick and onDismissSahabaDetail manage bottom sheet state`() = runTest {
        val sahaba = testSahaba(id = "umar", name = "Umar ibn al-Khattab")
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedSahabaDetail)

        viewModel.onSahabaCardClick(sahaba)
        advanceUntilIdle()

        assertEquals(sahaba, viewModel.uiState.value.selectedSahabaDetail)

        viewModel.onDismissSahabaDetail()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedSahabaDetail)
        collectJob.cancel()
    }

    @Test
    fun `onViewAllTenPromisedClick emits NavigateToLibrary effect with TEN_PROMISED`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch { viewModel.uiEffects.toList(effects) }

        viewModel.onViewAllTenPromisedClick()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(HomeUiEffect.NavigateToLibrary(category = SahabaCategory.TEN_PROMISED), effects.first())
        job.cancel()
    }
}
