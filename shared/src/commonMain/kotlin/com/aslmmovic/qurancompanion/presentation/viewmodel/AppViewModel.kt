package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val isInitialized: Boolean = false,
    val userPreferences: UserPreferences? = null,
    val todayJourney: Journey? = null,
    val isDarkMode: Boolean? = null,
    val isArabic: Boolean = false,
    val startDestination: String? = null
)

class AppViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val getDebugDayOffsetUseCase: GetDebugDayOffsetUseCase,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getUserPreferencesUseCase(),
                getDebugDayOffsetUseCase()
            ) { prefs, _ ->
                prefs
            }.collect { prefs ->
                var finalPrefs = prefs
                
                // Auto-select Arabic if device language is Arabic and preference is unset
                if (prefs.preferredLanguage == null) {
                    val systemLocale = localeProvider.currentLocale
                    if (systemLocale == "ar") {
                        finalPrefs = prefs.copy(preferredLanguage = "ar")
                        savePreferencesUseCase(finalPrefs)
                    }
                }

                // Retrieve today's journey based on active preferences (or debug offset)
                val journey = getTodayJourneyUseCase()

                // Resolve start destination path
                val destination = if (finalPrefs.preferredLanguage != null) {
                    AppRoute.Home.route
                } else {
                    val systemLocale = localeProvider.currentLocale
                    if (systemLocale == "ar") AppRoute.Home.route else AppRoute.Welcome.route
                }

                val isArabic = (finalPrefs.preferredLanguage ?: localeProvider.currentLocale) == "ar"

                _uiState.update {
                    it.copy(
                        isInitialized = true,
                        userPreferences = finalPrefs,
                        todayJourney = journey,
                        isDarkMode = finalPrefs.isDarkMode,
                        isArabic = isArabic,
                        startDestination = destination
                    )
                }
            }
        }
    }
}
