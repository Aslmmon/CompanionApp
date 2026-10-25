package com.aslmmovic.qurancompanion.presentation.screens.journey

import com.aslmmovic.qurancompanion.data.dto.CoverDto
import com.aslmmovic.qurancompanion.data.dto.JourneyDto
import com.aslmmovic.qurancompanion.data.dto.JourneyStepDto
import com.aslmmovic.qurancompanion.data.dto.StepTypeDto
import com.aslmmovic.qurancompanion.data.dto.toDomain
import com.aslmmovic.qurancompanion.domain.model.Cover
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import com.aslmmovic.qurancompanion.fakes.FakeDateTimeProvider
import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.FakeUserPreferencesRepository
import com.aslmmovic.qurancompanion.fakes.testJourney
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JourneyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo = FakeJourneyRepository()
    private val prefsRepo = FakeUserPreferencesRepository()
    private val fakeDateTimeProvider = FakeDateTimeProvider(
        dayOfYear = 1,
        dayOfWeek = 1,
        dateString = "2026-01-01",
        remainingTimeUntilMidnight = Pair(14, 22)
    )


    private fun createViewModel() = JourneyViewModel(
        getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
        markJourneyCompletedUseCase = MarkJourneyCompletedUseCase(repo),
        dateTimeProvider = fakeDateTimeProvider,
        getBookmarkedJourneyIdsUseCase = com.aslmmovic.qurancompanion.domain.usecase.GetBookmarkedJourneyIdsUseCase(prefsRepo),
        toggleJourneyBookmarkUseCase = com.aslmmovic.qurancompanion.domain.usecase.ToggleJourneyBookmarkUseCase(prefsRepo)
    )

    private val sampleJourney = testJourney(
        id = "journey-1",
        title = "Abu Bakr (RA)",
        steps = listOf(
            JourneyStep(StepType.INTRO, "Introduction", "Abu Bakr (RA) was the first caliph."),
            JourneyStep(StepType.STORY, "The Hijrah", "During the journey with the Prophet ﷺ..."),
            JourneyStep(StepType.KEY_LESSONS, "Steadfastness", "He stood firm during adversity."),
            JourneyStep(StepType.REFLECTION, "Contemplation", "How do we embody loyalty?"),
            JourneyStep(StepType.ACTION, "Today's Habit", "Give charity with sincere intention.")
        )
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo.todayJourney = sampleJourney
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads journey and step 0 and bookmark status`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(sampleJourney, state.journey)

        assertEquals(0, state.currentStepIndex)
        assertFalse(state.isBookmarked)
        assertFalse(state.isLoading)
        collectJob.cancel()
    }

    @Test
    fun `forward step navigation until last step`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentStepIndex)

        // Step 0 -> 1
        viewModel.onNextStep()
        assertEquals(1, viewModel.uiState.value.currentStepIndex)

        // Step 1 -> 2
        viewModel.onNextStep()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)

        // Step 2 -> 3
        viewModel.onNextStep()
        assertEquals(3, viewModel.uiState.value.currentStepIndex)

        // Step 3 -> 4 (last step)
        viewModel.onNextStep()
        assertEquals(4, viewModel.uiState.value.currentStepIndex)

        // Beyond last step should remain at 4
        viewModel.onNextStep()
        assertEquals(4, viewModel.uiState.value.currentStepIndex)
        collectJob.cancel()
    }

    @Test
    fun `step-aware backward navigation decrements step if on step greater than zero`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onNextStep()
        viewModel.onNextStep()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)

        viewModel.onBackPressed()
        assertEquals(1, viewModel.uiState.value.currentStepIndex)

        viewModel.onBackPressed()
        assertEquals(0, viewModel.uiState.value.currentStepIndex)
        collectJob.cancel()
    }

    @Test
    fun `step-aware backward navigation emits NavigateToHome if on step zero`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val effects = mutableListOf<JourneyUiEffect>()
        val effectsJob = launch { viewModel.uiEffects.toList(effects) }

        assertEquals(0, viewModel.uiState.value.currentStepIndex)

        viewModel.onBackPressed()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.NavigateToHome, effects.first())

        effectsJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun `final step action marks journey completed and emits NavigateToCompletion`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val effects = mutableListOf<JourneyUiEffect>()
        val effectsJob = launch { viewModel.uiEffects.toList(effects) }

        viewModel.onFinish()
        advanceUntilIdle()

        assertTrue(repo.isCompleted(sampleJourney.id, "2026-01-01").first())
        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.NavigateToCompletion, effects.first())

        effectsJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun `bookmark toggle toggles isBookmarked in state persists in repository and emits ShowBookmarkFeedback`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val effects = mutableListOf<JourneyUiEffect>()
        val effectsJob = launch { viewModel.uiEffects.toList(effects) }

        assertFalse(viewModel.uiState.value.isBookmarked)

        // First toggle: Bookmarked
        viewModel.onToggleBookmark()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBookmarked)
        assertTrue(prefsRepo.getBookmarkedJourneyIds().first().contains(sampleJourney.id))
        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.ShowBookmarkFeedback(isBookmarked = true), effects.last())

        // Second toggle: Unbookmarked
        viewModel.onToggleBookmark()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isBookmarked)
        assertFalse(prefsRepo.getBookmarkedJourneyIds().first().contains(sampleJourney.id))
        assertEquals(2, effects.size)
        assertEquals(JourneyUiEffect.ShowBookmarkFeedback(isBookmarked = false), effects.last())

        effectsJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun `journeyDto maps optional arabicVerse to journey domain model`() {
        val dto = JourneyDto(
            id = "dto-1",
            dayNumber = 1,
            title = "Title",
            subtitle = "Subtitle",
            category = "Category",
            emotion = "Emotion",
            theme = "Theme",
            heroQuote = "Quote",
            intention = "Intention",
            durationMinutes = 5,
            difficulty = "Easy",
            estimatedReadingMinutes = 4,
            cover = CoverDto("illustration", "asset"),
            arabicVerse = "إِنَّ مَعَ العُسْرِ يُسْرًا"
        )

        val domain = dto.toDomain()
        assertEquals("إِنَّ مَعَ العُسْرِ يُسْرًا", domain.arabicVerse)
    }

    @Test
    fun `initialization loads remaining hours and minutes until midnight into uiState`() = runTest {
        fakeDateTimeProvider.remainingTimeUntilMidnight = Pair(14, 22)
        val viewModel = createViewModel()

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(14, state.hoursUntilMidnight)
        assertEquals(22, state.minutesUntilMidnight)
        collectJob.cancel()
    }

    @Test
    fun `onReturnHome resets current step index to zero and emits NavigateToHome effect`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onNextStep()
        viewModel.onNextStep()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)

        val effects = mutableListOf<JourneyUiEffect>()
        val effectsJob = launch { viewModel.uiEffects.toList(effects) }

        viewModel.onReturnHome()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.currentStepIndex)
        assertEquals(1, effects.size)
        assertEquals(JourneyUiEffect.NavigateToHome, effects.first())

        effectsJob.cancel()
        collectJob.cancel()
    }
}

