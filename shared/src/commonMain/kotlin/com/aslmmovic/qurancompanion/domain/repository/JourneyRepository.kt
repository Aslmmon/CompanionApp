package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.Journey
import kotlinx.coroutines.flow.Flow

interface JourneyRepository {
    suspend fun getAllJourneys(): List<Journey>
    suspend fun getTodayJourney(): Journey?
    suspend fun getTomorrowJourney(): Journey?
    fun isCompleted(journeyId: String, date: String): Flow<Boolean>
    fun getWeeklyProgress(): Flow<List<Boolean>>
    suspend fun markCompleted(journeyId: String, date: String)
    suspend fun resetCompletion(journeyId: String, date: String)
    
    // Debug helper functions
    fun getDebugDayOffset(): Flow<Int>
    suspend fun incrementDebugDayOffset()
}
