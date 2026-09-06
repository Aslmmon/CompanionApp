package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake implementation of [UserPreferencesRepository] for TDD unit tests.
 */
class FakeUserPreferencesRepository(
    initialPreferences: UserPreferences = UserPreferences()
) : UserPreferencesRepository {

    private val _preferences = MutableStateFlow(initialPreferences)

    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        _preferences.value = preferences
    }
}
