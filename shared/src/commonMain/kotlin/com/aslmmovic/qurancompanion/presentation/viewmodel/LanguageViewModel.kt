package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class LanguageUiEvent {
    data object NavigateToHome : LanguageUiEvent()
}

class LanguageViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<LanguageUiEvent>()
    val uiEvents: SharedFlow<LanguageUiEvent> = _uiEvents.asSharedFlow()

    fun selectLanguage(languageCode: String) {
        viewModelScope.launch {
            val currentPrefs = userPreferencesRepository.getUserPreferences().first()
            userPreferencesRepository.saveUserPreferences(
                currentPrefs.copy(preferredLanguage = languageCode)
            )
            _uiEvents.emit(LanguageUiEvent.NavigateToHome)
        }
    }
}
