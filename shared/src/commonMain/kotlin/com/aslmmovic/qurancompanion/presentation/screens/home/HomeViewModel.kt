package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
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
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val getTomorrowJourneyUseCase: GetTomorrowJourneyUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase,
    private val isJourneyCompletedUseCase: IsJourneyCompletedUseCase,
    private val resetJourneyUseCase: ResetJourneyUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val getDebugDayOffsetUseCase: GetDebugDayOffsetUseCase,
    private val incrementDebugDayOffsetUseCase: IncrementDebugDayOffsetUseCase,
    private val dateTimeProvider: DateTimeProvider,
    private val getJourneyStreakUseCase: GetJourneyStreakUseCase,
    private val getBookmarkedJourneyIdsUseCase: GetBookmarkedJourneyIdsUseCase,
    private val toggleJourneyBookmarkUseCase: ToggleJourneyBookmarkUseCase,
    private val getSahabaCatalogUseCase: GetSahabaCatalogUseCase,
    private val getActiveHabitsUseCase: GetActiveHabitsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<HomeUiEffect>()
    val uiEffects: SharedFlow<HomeUiEffect> = _uiEffects.asSharedFlow()

    private val _selectedSahabaDetail = MutableStateFlow<Sahaba?>(null)

    private data class BaseJourneyData(
        val preferences: UserPreferences,
        val weeklyProgress: List<Boolean>,
        val streakCount: Int,
        val bookmarkedIds: Set<String>,
        val today: Journey?,
        val tomorrow: Journey?
    )

    val uiState: StateFlow<HomeUiState> = combine(
        getUserPreferencesUseCase(),
        getDebugDayOffsetUseCase(),
        getWeeklyProgressUseCase(),
        getJourneyStreakUseCase(),
        getBookmarkedJourneyIdsUseCase()
    ) { preferences, _, weeklyProgress, streakCount, bookmarkedIds ->
        val today = getTodayJourneyUseCase()
        val tomorrow = getTomorrowJourneyUseCase()
        BaseJourneyData(preferences, weeklyProgress, streakCount, bookmarkedIds, today, tomorrow)
    }.flatMapLatest { base ->
        val isCompletedFlow = if (base.today != null) {
            isJourneyCompletedUseCase(base.today.id, dateTimeProvider.getCurrentDateString())
        } else {
            flowOf(false)
        }
        combine(
            isCompletedFlow,
            getSahabaCatalogUseCase(category = SahabaCategory.TEN_PROMISED),
            getActiveHabitsUseCase(),
            _selectedSahabaDetail
        ) { isCompleted, tenPromised, activeHabits, selectedSahaba ->
            val featuredHabit = activeHabits.find { it.id == "smile_everyone" } ?: activeHabits.firstOrNull()
            HomeUiState(
                journey = base.today,
                tomorrowJourney = base.tomorrow,
                isCompleted = isCompleted,
                isBookmarked = base.today?.let { base.bookmarkedIds.contains(it.id) } ?: false,
                weeklyProgress = base.weeklyProgress,
                streakCount = base.streakCount,
                currentDayOfWeek = dateTimeProvider.getCurrentDayOfWeek(),
                hijriDate = dateTimeProvider.getCurrentHijriDateString(),
                tenPromisedCompanions = tenPromised,
                todayHabit = featuredHabit,
                isHabitCompleted = featuredHabit?.isCompletedToday == true,
                selectedSahabaDetail = selectedSahaba,
                userPreferences = base.preferences,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    private suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        val current = getUserPreferencesUseCase().first()
        savePreferencesUseCase(transform(current))
    }

    fun onBeginJourneyClick() {
        viewModelScope.launch { _uiEffects.emit(HomeUiEffect.NavigateToJourneyFlow) }
    }

    fun onSettingsClick() {
        viewModelScope.launch { _uiEffects.emit(HomeUiEffect.NavigateToSettings) }
    }

    fun onNotificationClick() {
        onSettingsClick()
    }

    fun onBookmarkClick() {
        viewModelScope.launch {
            uiState.value.journey?.let {
                toggleJourneyBookmarkUseCase(it.id)
            }
        }
    }

    fun onToggleHabitClick() {
        viewModelScope.launch {
            uiState.value.todayHabit?.let {
                toggleHabitCompletionUseCase(it.id)
            }
        }
    }

    fun onSahabaCardClick(sahaba: Sahaba) {
        _selectedSahabaDetail.value = sahaba
    }

    fun onDismissSahabaDetail() {
        _selectedSahabaDetail.value = null
    }

    fun onViewAllTenPromisedClick() {
        viewModelScope.launch {
            _uiEffects.emit(HomeUiEffect.NavigateToLibrary(category = SahabaCategory.TEN_PROMISED))
        }
    }

    fun onResetCompletionClick() {
        viewModelScope.launch {
            uiState.value.journey?.let {
                resetJourneyUseCase(it.id, dateTimeProvider.getCurrentDateString())
            }
        }
    }

    fun onNextJourneyClick() {
        viewModelScope.launch {
            incrementDebugDayOffsetUseCase()
        }
    }

    fun onToggleTheme(isDarkMode: Boolean) {
        viewModelScope.launch {
            updatePreferences { it.copy(isDarkMode = isDarkMode) }
        }
    }
}
