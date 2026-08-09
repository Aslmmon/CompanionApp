package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow

class GetTodayJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(): Journey? = repository.getTodayJourney()
}

class IsJourneyCompletedUseCase(private val repository: JourneyRepository) {
    operator fun invoke(journeyId: String, date: String): Flow<Boolean> =
        repository.isCompleted(journeyId, date)
}

class MarkJourneyCompletedUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, date: String) =
        repository.markCompleted(journeyId, date)
}

class ResetJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, date: String) =
        repository.resetCompletion(journeyId, date)
}

class GetTomorrowJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(): Journey? = repository.getTomorrowJourney()
}

class GetWeeklyProgressUseCase(private val repository: JourneyRepository) {
    operator fun invoke(): Flow<List<Boolean>> = repository.getWeeklyProgress()
}

class GetDebugDayOffsetUseCase(private val repository: JourneyRepository) {
    operator fun invoke(): Flow<Int> = repository.getDebugDayOffset()
}

class IncrementDebugDayOffsetUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke() = repository.incrementDebugDayOffset()
}
