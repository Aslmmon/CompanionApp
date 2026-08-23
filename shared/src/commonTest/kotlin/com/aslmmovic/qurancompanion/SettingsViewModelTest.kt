package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RequestNotificationPermissionUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsUiEffect
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsViewModel
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakePrefsRepo = FakeUserPreferencesRepository()
    private val fakeJourneyRepo = FakeJourneyRepository()
    private val fakeScheduler = FakeNotificationScheduler()
    private val fakeLocaleProvider = object : LocaleProvider {
        override var currentLocale = "en"
        override fun changeLocale(locale: String) {
            currentLocale = locale
        }
    }

    private fun createViewModel() = SettingsViewModel(
        getUserPreferencesUseCase = GetUserPreferencesUseCase(fakePrefsRepo),
        savePreferencesUseCase = SavePreferencesUseCase(fakePrefsRepo),
        scheduleDailyReminderUseCase = ScheduleDailyReminderUseCase(fakeScheduler, fakePrefsRepo, fakeJourneyRepo),
        requestNotificationPermissionUseCase = RequestNotificationPermissionUseCase(fakeScheduler),
        incrementDebugDayOffsetUseCase = IncrementDebugDayOffsetUseCase(fakeJourneyRepo),
        localeProvider = fakeLocaleProvider
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
    fun `SettingsViewModel loads preferences upon subscription`() = runTest {
        fakePrefsRepo.saveUserPreferences(
            UserPreferences(preferredLanguage = "ar", isReminderEnabled = true, reminderHour = 6)
        )
        val viewModel = createViewModel()

        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("ar", viewModel.uiState.value.userPreferences.preferredLanguage)
        assertTrue(viewModel.uiState.value.userPreferences.isReminderEnabled)
        assertEquals(6, viewModel.uiState.value.userPreferences.reminderHour)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onToggleReminder disables reminder and cancels scheduler`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userPreferences.isReminderEnabled)

        viewModel.onToggleReminder(false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.userPreferences.isReminderEnabled)
        assertTrue(fakeScheduler.isCancelled)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onToggleReminder enables reminder and schedules`() = runTest {
        fakePrefsRepo.saveUserPreferences(UserPreferences(isReminderEnabled = false))
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.userPreferences.isReminderEnabled)

        viewModel.onToggleReminder(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.userPreferences.isReminderEnabled)
        assertFalse(fakeScheduler.isCancelled)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onUpdateReminderTime updates preferences and reschedules`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onUpdateReminderTime(20, 0)
        advanceUntilIdle()

        assertEquals(20, viewModel.uiState.value.userPreferences.reminderHour)
        assertEquals(0, viewModel.uiState.value.userPreferences.reminderMinute)
        assertEquals(20, fakeScheduler.scheduledHour)
        assertEquals(0, fakeScheduler.scheduledMinute)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onLanguageSelected updates preferences and invokes locale provider`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onLanguageSelected("ar")
        advanceUntilIdle()

        assertEquals("ar", viewModel.uiState.value.userPreferences.preferredLanguage)
        assertEquals("ar", fakeLocaleProvider.currentLocale)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onToggleTheme updates dark mode preference`() = runTest {
        val viewModel = createViewModel()
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onToggleTheme(true)
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.userPreferences.isDarkMode)

        viewModel.onToggleTheme(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.userPreferences.isDarkMode)

        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onSimulateNextDay increments debug offset`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        var offset = 0
        val collectJob = launch { fakeJourneyRepo.getDebugDayOffset().collect { offset = it } }
        advanceUntilIdle()
        assertEquals(0, offset)

        viewModel.onSimulateNextDay()
        advanceUntilIdle()

        assertEquals(1, offset)
        collectJob.cancel()
    }

    @Test
    fun `SettingsViewModel onBackClick emits NavigateBack effect`() = runTest {
        val viewModel = createViewModel()
        val effects = mutableListOf<SettingsUiEffect>()
        val collectJob = launch { viewModel.uiEffects.toList(effects) }

        viewModel.onBackClick()
        advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(SettingsUiEffect.NavigateBack, effects.first())

        collectJob.cancel()
    }
}
