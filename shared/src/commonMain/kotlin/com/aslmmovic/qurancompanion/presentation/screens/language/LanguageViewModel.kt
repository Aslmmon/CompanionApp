package com.aslmmovic.qurancompanion.presentation.screens.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LanguageViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val localeProvider: LocaleProvider
) : ViewModel() {

    private val _uiEffects = MutableSharedFlow<LanguageUiEffect>()
    val uiEffects: SharedFlow<LanguageUiEffect> = _uiEffects.asSharedFlow()

    fun selectLanguage(languageCode: String) {
        viewModelScope.launch {
            val currentPrefs = getUserPreferencesUseCase().first()
            savePreferencesUseCase(
                currentPrefs.copy(preferredLanguage = languageCode)
            )
            localeProvider.changeLocale(languageCode)
            _uiEffects.emit(LanguageUiEffect.NavigateToHome)
        }
    }
}
