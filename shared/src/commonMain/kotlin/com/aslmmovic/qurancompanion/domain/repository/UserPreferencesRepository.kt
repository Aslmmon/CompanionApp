package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun saveUserPreferences(preferences: UserPreferences)
    fun getBookmarkedJourneyIds(): Flow<Set<String>>
    suspend fun toggleJourneyBookmark(journeyId: String): Boolean
}
