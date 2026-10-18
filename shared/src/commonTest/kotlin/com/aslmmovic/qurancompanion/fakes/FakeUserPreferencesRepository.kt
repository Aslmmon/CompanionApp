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
    private val _bookmarkedJourneys = MutableStateFlow<Set<String>>(emptySet())

    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        _preferences.value = preferences
    }

    override fun getBookmarkedJourneyIds(): Flow<Set<String>> = _bookmarkedJourneys.asStateFlow()

    override suspend fun toggleJourneyBookmark(journeyId: String): Boolean {
        val current = _bookmarkedJourneys.value.toMutableSet()
        val isNowBookmarked = if (current.contains(journeyId)) {
            current.remove(journeyId)
            false
        } else {
            current.add(journeyId)
            true
        }
        _bookmarkedJourneys.value = current
        return isNowBookmarked
    }
}
