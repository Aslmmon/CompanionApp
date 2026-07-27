package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.data.datasource.JourneyLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.JourneyDto
import com.aslmmovic.qurancompanion.data.dto.JourneyStepDto
import com.aslmmovic.qurancompanion.data.repository.JourneyRepositoryImpl
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JourneyRepositoryImplTest {

    private val fakeDataSource = FakeJourneyLocalDataSource()
    private val fakeLocaleProvider = FakeLocaleProvider()
    private val fakeStorage = FakeKeyValueStorage()
    private val fakeDateTimeProvider = FakeDateTimeProvider()

    private val repository = JourneyRepositoryImpl(
        localDataSource = fakeDataSource,
        localeProvider = fakeLocaleProvider,
        storage = fakeStorage,
        dateTimeProvider = fakeDateTimeProvider
    )

    @Test
    fun `getTodayJourney cycles through available journeys correctly`() = runTest {
        val journeys = listOf(
            createJourneyDto("1"),
            createJourneyDto("2"),
            createJourneyDto("3")
        )
        fakeDataSource.journeys = journeys

        // Day 1 -> Index 0
        fakeDateTimeProvider.dayOfYear = 1
        assertEquals("1", repository.getTodayJourney()?.id)

        // Day 2 -> Index 1
        fakeDateTimeProvider.dayOfYear = 2
        assertEquals("2", repository.getTodayJourney()?.id)

        // Day 3 -> Index 2
        fakeDateTimeProvider.dayOfYear = 3
        assertEquals("3", repository.getTodayJourney()?.id)

        // Day 4 -> Index 0 (Wrap around)
        fakeDateTimeProvider.dayOfYear = 4
        assertEquals("1", repository.getTodayJourney()?.id)
    }

    @Test
    fun `getTomorrowJourney returns next day cycle correctly`() = runTest {
        val journeys = listOf(
            createJourneyDto("1"),
            createJourneyDto("2"),
            createJourneyDto("3")
        )
        fakeDataSource.journeys = journeys

        // Today is Day 1 -> Tomorrow is Day 2 (Index 1)
        fakeDateTimeProvider.dayOfYear = 1
        assertEquals("2", repository.getTomorrowJourney()?.id)

        // Today is Day 3 -> Tomorrow is Day 4 -> Index 0 (Wrap around)
        fakeDateTimeProvider.dayOfYear = 3
        assertEquals("1", repository.getTomorrowJourney()?.id)
    }

    @Test
    fun `getWeeklyProgress correctly calculates completed states offset by current day of week`() = runTest {
        val journeys = listOf(
            createJourneyDto("1"), // index 0
            createJourneyDto("2"), // index 1
            createJourneyDto("3"), // index 2
            createJourneyDto("4"), // index 3
            createJourneyDto("5")  // index 4
        )
        fakeDataSource.journeys = journeys

        // Setup date: Wednesday (3rd day of week), Day 10 of the year
        // Today index: (10 - 1) % 5 = 4 (Journey "5")
        // Mon (day 1, offset -2 from Wed) -> Day of year = 8 -> index (8-1)%5 = 2 -> Journey "3"
        // Tue (day 2, offset -1 from Wed) -> Day of year = 9 -> index (9-1)%5 = 3 -> Journey "4"
        // Wed (day 3, offset 0 from Wed) -> Day of year = 10 -> index (10-1)%5 = 4 -> Journey "5"
        // Thu (day 4, offset +1 from Wed) -> Day of year = 11 -> index (11-1)%5 = 0 -> Journey "1"
        // Fri (day 5, offset +2 from Wed) -> Day of year = 12 -> index (12-1)%5 = 1 -> Journey "2"
        // Sat (day 6, offset +3 from Wed) -> Day of year = 13 -> index (13-1)%5 = 2 -> Journey "3"
        // Sun (day 7, offset +4 from Wed) -> Day of year = 14 -> index (14-1)%5 = 3 -> Journey "4"
        
        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dayOfYear = 10

        // Populate database with all journeys to trigger state cache initialization
        repository.getAllJourneys()

        // Complete Journey "3" (Mon & Sat) and Journey "5" (Wed)
        repository.markCompleted("3")
        repository.markCompleted("5")

        val progress = repository.getWeeklyProgress().first()
        
        // Expected progress: [Mon=true (J3), Tue=false (J4), Wed=true (J5), Thu=false (J1), Fri=false (J2), Sat=true (J3), Sun=false (J4)]
        assertEquals(
            listOf(true, false, true, false, false, true, false),
            progress
        )
    }

    @Test
    fun `marking journey completed persists in storage and emits true`() = runTest {
        val journeys = listOf(createJourneyDto("test-id"))
        fakeDataSource.journeys = journeys
        repository.getAllJourneys()

        assertFalse(repository.isCompleted("test-id").first())

        repository.markCompleted("test-id")
        assertTrue(repository.isCompleted("test-id").first())
        assertTrue(fakeStorage.getBoolean("journey_completed_test-id", false))

        repository.resetCompletion("test-id")
        assertFalse(repository.isCompleted("test-id").first())
        assertFalse(fakeStorage.getBoolean("journey_completed_test-id", true))
    }

    // Helper functions and fakes
    private fun createJourneyDto(id: String) = JourneyDto(
        id = id,
        dayNumber = 1,
        title = "Journey $id",
        subtitle = "Subtitle $id",
        category = "Category",
        durationMinutes = 10,
        steps = listOf(JourneyStepDto(com.aslmmovic.qurancompanion.data.dto.StepTypeDto.INTRO, "Title", "Content"))
    )

    private class FakeJourneyLocalDataSource : JourneyLocalDataSource {
        var journeys: List<JourneyDto> = emptyList()
        override suspend fun loadJourneys(locale: String): List<JourneyDto> = journeys
    }

    private class FakeLocaleProvider : LocaleProvider {
        override val currentLocale: String = "en"
    }

    private class FakeKeyValueStorage : KeyValueStorage {
        private val booleans = mutableMapOf<String, Boolean>()
        private val ints = mutableMapOf<String, Int>()
        private val strings = mutableMapOf<String, String>()

        override fun getString(key: String): String? = strings[key]
        override fun putString(key: String, value: String) { strings[key] = value }
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue
        override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
        override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
        override fun putInt(key: String, value: Int) { ints[key] = value }
    }

    private class FakeDateTimeProvider : DateTimeProvider {
        var dayOfYear = 1
        var dayOfWeek = 1
        override fun getCurrentDayOfYear(): Int = dayOfYear
        override fun getCurrentDayOfWeek(): Int = dayOfWeek
    }
}
