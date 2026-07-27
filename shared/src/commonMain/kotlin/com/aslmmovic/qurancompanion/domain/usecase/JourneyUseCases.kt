package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow

class GetTodayJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(): Journey? = repository.getTodayJourney()
}

class IsJourneyCompletedUseCase(private val repository: JourneyRepository) {
    operator fun invoke(journeyId: String): Flow<Boolean> = repository.isCompleted(journeyId)
}

class MarkJourneyCompletedUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String) = repository.markCompleted(journeyId)
}

class ResetJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String) = repository.resetCompletion(journeyId)
}

class GetTomorrowJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(): Journey? = repository.getTomorrowJourney()
}

class GetWeeklyProgressUseCase(private val repository: JourneyRepository) {
    operator fun invoke(): Flow<List<Boolean>> = repository.getWeeklyProgress()
}
