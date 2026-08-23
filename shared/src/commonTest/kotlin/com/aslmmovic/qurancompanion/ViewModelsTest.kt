package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeUiEffect
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.screens.journey.JourneyUiEffect
import com.aslmmovic.qurancompanion.presentation.screens.journey.JourneyViewModel
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo = FakeJourneyRepository()
    private val prefsRepo = FakeUserPreferencesRepository()
    private val fakeDateTimeProvider = object : DateTimeProvider {
        override fun getCurrentDayOfYear(): Int = 1
        override fun getCurrentDayOfWeek(): Int = 1
        override fun getCurrentDateString(): String = "2026-01-01"
    }

    private fun createHomeViewModel() = HomeViewModel(
        getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
        getTomorrowJourneyUseCase = GetTomorrowJourneyUseCase(repo),
        getWeeklyProgressUseCase = GetWeeklyProgressUseCase(repo),
        isJourneyCompletedUseCase = IsJourneyCompletedUseCase(repo),
        resetJourneyUseCase = ResetJourneyUseCase(repo),
        getUserPreferencesUseCase = GetUserPreferencesUseCase(prefsRepo),
        savePreferencesUseCase = SavePreferencesUseCase(prefsRepo),
        getDebugDayOffsetUseCase = GetDebugDayOffsetUseCase(repo),
        incrementDebugDayOffsetUseCase = IncrementDebugDayOffsetUseCase(repo),
        dateTimeProvider = fakeDateTimeProvider
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
    fun `HomeViewModel loads journeys on initialization`() = runTest {
        val today = testJourney(id = "today")
        val tomorrow = testJourney(id = "tomorrow")
        repo.todayJourney = today
        repo.allJourneys = listOf(today, tomorrow)

        val viewModel = createHomeViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(today, viewModel.uiState.value.journey)
        assertEquals(today, viewModel.uiState.value.tomorrowJourney)
        collectJob.cancel()
    }

    @Test
    fun `HomeViewModel onToggleTheme updates preferences`() = runTest {
        val viewModel = createHomeViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.userPreferences.isDarkMode)

        viewModel.onToggleTheme(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userPreferences.isDarkMode == true)
        collectJob.cancel()
    }

    @Test
    fun `HomeViewModel onBeginJourneyClick emits NavigateToJourneyFlow event`() = runTest {
        val viewModel = createHomeViewModel()

        val effects = mutableListOf<HomeUiEffect>()
        val job = launch {
            viewModel.uiEffects.toList(effects)
        }

        viewModel.onBeginJourneyClick()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(HomeUiEffect.NavigateToJourneyFlow, effects.first())
        job.cancel()
    }

    @Test
    fun `HomeViewModel onSettingsClick emits NavigateToSettings event`() = runTest {
        val viewModel = createHomeViewModel()

        val effects = mutableListOf<HomeUiEffect>()
        val job = launch {
            viewModel.uiEffects.toList(effects)
        }

        viewModel.onSettingsClick()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(HomeUiEffect.NavigateToSettings, effects.first())
        job.cancel()
    }

    @Test
    fun `HomeViewModel onResetCompletionClick resets completion status`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today
        repo.markCompleted(today.id, "2026-01-01")

        val viewModel = createHomeViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)

        viewModel.onResetCompletionClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCompleted)
        collectJob.cancel()
    }

    @Test
    fun `JourneyViewModel stepping and flow logic`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today

        val viewModel = JourneyViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo),
            dateTimeProvider = fakeDateTimeProvider
        )

        advanceUntilIdle()

        assertEquals(today, viewModel.journey.value)
        assertEquals(0, viewModel.currentStepIndex.value)

        // Step forward (max index is steps.size - 1 = 1)
        viewModel.onNextStep()
        assertEquals(1, viewModel.currentStepIndex.value)

        // Attempting to step forward beyond max index does nothing
        viewModel.onNextStep()
        assertEquals(1, viewModel.currentStepIndex.value)

        // Step backward
        viewModel.onPreviousStep()
        assertEquals(0, viewModel.currentStepIndex.value)

        // Attempting to step backward below 0 does nothing
        viewModel.onPreviousStep()
        assertEquals(0, viewModel.currentStepIndex.value)
    }

    @Test
    fun `JourneyViewModel finish emits navigation event and marks completed`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today

        val viewModel = JourneyViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo),
            dateTimeProvider = fakeDateTimeProvider
        )

        advanceUntilIdle()

        val effects = mutableListOf<JourneyUiEffect>()
        val job = launch {
            viewModel.uiEffects.toList(effects)
        }

        viewModel.onFinish()
        advanceUntilIdle()

        assertTrue(repo.isCompleted(today.id, "2026-01-01").first())
        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.NavigateToCompletion, effects.first())

        job.cancel()
    }

    @Test
    fun `JourneyViewModel return home resets index and emits event`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today

        val viewModel = JourneyViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo),
            dateTimeProvider = fakeDateTimeProvider
        )

        advanceUntilIdle()
        viewModel.onNextStep()
        assertEquals(1, viewModel.currentStepIndex.value)

        val effects = mutableListOf<JourneyUiEffect>()
        val job = launch {
            viewModel.uiEffects.toList(effects)
        }

        viewModel.onReturnHome()
        advanceUntilIdle()

        assertEquals(0, viewModel.currentStepIndex.value)
        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.NavigateToHome, effects.first())

        job.cancel()
    }

    @Test
    fun `HomeViewModel onNextJourneyClick increments offset`() = runTest {
        val viewModel = createHomeViewModel()

        advanceUntilIdle()
        
        var offsetValue = 0
        val collectJob = launch {
            repo.getDebugDayOffset().collect { offsetValue = it }
        }
        advanceUntilIdle()
        assertEquals(0, offsetValue)

        viewModel.onNextJourneyClick()
        advanceUntilIdle()

        assertEquals(1, offsetValue)
        collectJob.cancel()
    }
}
