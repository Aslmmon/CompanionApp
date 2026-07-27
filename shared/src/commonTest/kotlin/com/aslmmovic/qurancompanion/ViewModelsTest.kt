package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo = FakeJourneyRepository()

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
        // Note: in FakeJourneyRepository we return todayJourney for tomorrow's journey too, let's keep it simple

        val viewModel = HomeViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            getTomorrowJourneyUseCase = GetTomorrowJourneyUseCase(repo),
            getWeeklyProgressUseCase = GetWeeklyProgressUseCase(repo),
            isJourneyCompletedUseCase = IsJourneyCompletedUseCase(repo),
            resetJourneyUseCase = ResetJourneyUseCase(repo)
        )

        advanceUntilIdle()

        assertEquals(today, viewModel.journey.value)
        assertEquals(today, viewModel.tomorrowJourney.value)
    }

    @Test
    fun `HomeViewModel onBeginJourneyClick emits NavigateToJourneyFlow event`() = runTest {
        val viewModel = HomeViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            getTomorrowJourneyUseCase = GetTomorrowJourneyUseCase(repo),
            getWeeklyProgressUseCase = GetWeeklyProgressUseCase(repo),
            isJourneyCompletedUseCase = IsJourneyCompletedUseCase(repo),
            resetJourneyUseCase = ResetJourneyUseCase(repo)
        )

        val events = mutableListOf<HomeUiEvent>()
        val job = launch {
            viewModel.uiEvents.toList(events)
        }

        viewModel.onBeginJourneyClick()
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertEquals(HomeUiEvent.NavigateToJourneyFlow, events.first())
        job.cancel()
    }

    @Test
    fun `HomeViewModel onResetCompletionClick resets completion status`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today
        repo.markCompleted(today.id)

        val viewModel = HomeViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            getTomorrowJourneyUseCase = GetTomorrowJourneyUseCase(repo),
            getWeeklyProgressUseCase = GetWeeklyProgressUseCase(repo),
            isJourneyCompletedUseCase = IsJourneyCompletedUseCase(repo),
            resetJourneyUseCase = ResetJourneyUseCase(repo)
        )

        advanceUntilIdle()
        val collectJob = launch {
            viewModel.isCompleted.collect {}
        }
        advanceUntilIdle()

        assertTrue(viewModel.isCompleted.value)

        viewModel.onResetCompletionClick()
        advanceUntilIdle()

        assertFalse(viewModel.isCompleted.value)
        collectJob.cancel()
    }

    @Test
    fun `JourneyViewModel stepping and flow logic`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today

        val viewModel = JourneyViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo)
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
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo)
        )

        advanceUntilIdle()

        val events = mutableListOf<JourneyUiEvent>()
        val job = launch {
            viewModel.uiEvents.toList(events)
        }

        viewModel.onFinish()
        advanceUntilIdle()

        assertTrue(repo.isCompleted(today.id).first())
        assertEquals(1, events.size)
        assertEquals(JourneyUiEvent.NavigateToCompletion, events.first())

        job.cancel()
    }

    @Test
    fun `JourneyViewModel return home resets index and emits event`() = runTest {
        val today = testJourney(id = "today")
        repo.todayJourney = today

        val viewModel = JourneyViewModel(
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo)
        )

        advanceUntilIdle()
        viewModel.onNextStep()
        assertEquals(1, viewModel.currentStepIndex.value)

        val events = mutableListOf<JourneyUiEvent>()
        val job = launch {
            viewModel.uiEvents.toList(events)
        }

        viewModel.onReturnHome()
        advanceUntilIdle()

        assertEquals(0, viewModel.currentStepIndex.value)
        assertEquals(1, events.size)
        assertEquals(JourneyUiEvent.NavigateToHome, events.first())

        job.cancel()
    }
}
