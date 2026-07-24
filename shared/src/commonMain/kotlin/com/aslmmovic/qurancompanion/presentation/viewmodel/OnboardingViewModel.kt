package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.presentation.navigation.Screen
import com.aslmmovic.qurancompanion.presentation.navigation.NavigationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val navigationManager: NavigationManager,
    private val coroutineScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = coroutineScope ?: viewModelScope

    // Navigation is handled by NavigationManager

    val currentScreen: StateFlow<Screen> = navigationManager.currentScreen

    private val _preferencesState = MutableStateFlow(UserPreferences())
    val preferencesState: StateFlow<UserPreferences> = _preferencesState.asStateFlow()

    init {
        scope.launch {
            val initialPrefs = getUserPreferencesUseCase().first()
            _preferencesState.value = initialPrefs
            if (initialPrefs.isSetupCompleted) {
                navigationManager.navigateTo(Screen.Home)
            } else {
                navigationManager.navigateTo(Screen.Welcome)
            }
        }
    }

    fun navigateToSetup() {
        navigationManager.navigateTo(Screen.Setup)
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        _preferencesState.value = _preferencesState.value.copy(
            reminderHour = hour,
            reminderMinute = minute
        )
    }

    fun updateReminderEnabled(enabled: Boolean) {
        _preferencesState.value = _preferencesState.value.copy(isReminderEnabled = enabled)
    }

    fun completeSetup() {
        scope.launch {
            val updated = _preferencesState.value.copy(isSetupCompleted = true)
            savePreferencesUseCase(updated)
            _preferencesState.value = updated
            navigationManager.navigateTo(Screen.Home)
        }
    }

    fun resetSetup() {
        scope.launch {
            val reset = UserPreferences()
            savePreferencesUseCase(reset)
            _preferencesState.value = reset
            navigationManager.navigateTo(Screen.Welcome)
        }
    }
}
