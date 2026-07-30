package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider

sealed class LanguageUiEvent {
    data object NavigateToHome : LanguageUiEvent()
}

class LanguageViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<LanguageUiEvent>()
    val uiEvents: SharedFlow<LanguageUiEvent> = _uiEvents.asSharedFlow()

    fun selectLanguage(languageCode: String) {
        viewModelScope.launch {
            val currentPrefs = getUserPreferencesUseCase().first()
            savePreferencesUseCase(
                currentPrefs.copy(preferredLanguage = languageCode)
            )
            localeProvider.changeLocale(languageCode)
            _uiEvents.emit(LanguageUiEvent.NavigateToHome)
        }
    }
}
