package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesUseCase(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<UserPreferences> = repository.getUserPreferences()
}

class SavePreferencesUseCase(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(preferences: UserPreferences) {
        repository.saveUserPreferences(preferences)
    }
}
