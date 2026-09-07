package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.data.datasource.JourneyLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.CoverDto
import com.aslmmovic.qurancompanion.data.dto.JourneyDto
import com.aslmmovic.qurancompanion.data.dto.JourneyStepDto
import com.aslmmovic.qurancompanion.data.repository.JourneyRepositoryImpl
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JourneyRepositoryImplTest {

    private val fakeDataSource = FakeJourneyLocalDataSource()
    private val fakeLocaleProvider = FakeLocaleProvider()
    private val fakeStorage = FakeKeyValueStorage()
    private val fakeDateTimeProvider = FakeDateTimeProvider()

    private fun TestScope.createRepository() = JourneyRepositoryImpl(
        localDataSource = fakeDataSource,
        localeProvider = fakeLocaleProvider,
        storage = fakeStorage,
        dateTimeProvider = fakeDateTimeProvider,
        ioDispatcher = UnconfinedTestDispatcher(testScheduler)
    )

    @Test
    fun `getTodayJourney cycles through available journeys correctly`() = runTest {
        val repository = createRepository()
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
        val repository = createRepository()
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
        val repository = createRepository()
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
        fakeDateTimeProvider.dateString = "2026-01-10" // Day 10 of year = Jan 10 (Wednesday)

        // Populate database with all journeys to trigger state cache initialization
        repository.getAllJourneys()

        // Complete Journey "3" on Monday (Jan 8) and Journey "5" on Wednesday (Jan 10)
        // With date-keyed storage, J3 on Jan8 does NOT bleed into Sat Jan13 even though Sat also maps to J3
        repository.markCompleted("3", "2026-01-08") // Monday of that week
        repository.markCompleted("5", "2026-01-10") // Wednesday of that week

        val progress = repository.getWeeklyProgress().first()

        // Expected: [Mon=true (J3@Jan8), Tue=false (J4), Wed=true (J5@Jan10), Thu=false, Fri=false,
        //            Sat=false (J3@Jan13 NOT marked — different date from Mon!), Sun=false]
        assertEquals(
            listOf(true, false, true, false, false, false, false),
            progress
        )
    }

    @Test
    fun `marking journey completed persists in storage and emits true`() = runTest {
        val repository = createRepository()
        val journeys = listOf(createJourneyDto("test-id"))
        fakeDataSource.journeys = journeys
        repository.getAllJourneys()

        assertFalse(repository.isCompleted("test-id", "2026-01-01").first())

        repository.markCompleted("test-id", "2026-01-01")
        assertTrue(repository.isCompleted("test-id", "2026-01-01").first())
        assertTrue(fakeStorage.getBoolean("journey_completed_test-id_2026-01-01", false))

        repository.resetCompletion("test-id", "2026-01-01")
        assertFalse(repository.isCompleted("test-id", "2026-01-01").first())
        assertFalse(fakeStorage.getBoolean("journey_completed_test-id_2026-01-01", true))
    }

    @Test
    fun `getWeeklyProgress dynamically updates when incrementing debug day offset`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1"),
            createJourneyDto("2"),
            createJourneyDto("3")
        )
        fakeDataSource.journeys = journeys

        // Setup: Wednesday (3rd day of week), Day 10 of year = Jan 10 2026
        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dayOfYear = 10
        fakeDateTimeProvider.dateString = "2026-01-10"
        repository.getAllJourneys()

        // Mark J1 completed on Wednesday Jan 10, J2 completed on Monday Jan 8
        // With date-keyed storage, completions are pinned to the specific date.
        repository.markCompleted("1", "2026-01-10") // Wednesday
        repository.markCompleted("2", "2026-01-08") // Monday

        val progressList = mutableListOf<List<Boolean>>()
        val job = launch {
            repository.getWeeklyProgress().collect { progressList.add(it) }
        }
        advanceUntilIdle()

        // Increment day offset: shifts which journey appears on each slot but dates stay week-relative
        repository.incrementDebugDayOffset()
        advanceUntilIdle()

        job.cancel()

        // Verify that the flow emitted at least twice (offset change triggers recompute)
        println("PROGRESS LIST CONTENTS: $progressList")
        assertTrue(progressList.size >= 2)
        // offset 0: Mon(J2@Jan8)=true, Tue(J3@Jan9)=false, Wed(J1@Jan10)=true, Thu(J2@Jan11)=false,
        //           Fri(J3@Jan12)=false, Sat(J1@Jan13)=false, Sun(J2@Jan14)=false
        // J2 only marked for Jan8, not Jan11/Jan14; J1 only for Jan10, not Jan13
        assertEquals(listOf(true, false, true, false, false, false, false), progressList[0])
        // offset 1: all slots shift by 1 day-of-year → different (journey,date) pairs, none marked
        assertEquals(listOf(false, false, false, false, false, false, false), progressList[1])
    }

    @Test
    fun `completing today journey only ticks today in weekly progress`() = runTest {
        val repository = createRepository()
        fakeDataSource.journeys = listOf(
            createJourneyDto("1"), createJourneyDto("2"), createJourneyDto("3")
        )

        // Wednesday = day 3 of week, day 221 of year — today maps to journey index (221-1)%3 = 1 → "2"
        fakeDateTimeProvider.dayOfYear = 221
        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dateString = "2026-08-08"
        repository.getAllJourneys()

        repository.markCompleted("2", "2026-08-08")
        val progress = repository.getWeeklyProgress().first()

        // Only Wednesday (index 2, 0-based) must be true — no other day shares the tick
        assertEquals(listOf(false, false, true, false, false, false, false), progress)
    }

    @Test
    fun `offsetDate helper correctly shifts dates across month and year boundaries`() {
        assertEquals("2026-09-01", JourneyRepositoryImpl.offsetDate("2026-08-31", 1))
        assertEquals("2026-08-31", JourneyRepositoryImpl.offsetDate("2026-09-01", -1))
        assertEquals("2027-01-01", JourneyRepositoryImpl.offsetDate("2026-12-31", 1))
        assertEquals("2026-12-31", JourneyRepositoryImpl.offsetDate("2027-01-01", -1))
        assertEquals("2026-02-28", JourneyRepositoryImpl.offsetDate("2026-03-01", -1))
        assertEquals("2026-03-01", JourneyRepositoryImpl.offsetDate("2026-02-28", 1))
    }

    @Test
    fun `when locale changes to arabic, cached journeys reload with arabic journeys`() = runTest {
        val repository = createRepository()
        val enJourneys = listOf(createJourneyDto("1", title = "Abu Bakr"))
        val arJourneys = listOf(createJourneyDto("1", title = "أبو بكر الصديق"))
        fakeDataSource.journeysByLocale = mapOf("en" to enJourneys, "ar" to arJourneys)
        fakeLocaleProvider.changeLocale("en")
        advanceUntilIdle()

        assertEquals("Abu Bakr", repository.getAllJourneys().first().title)
        assertEquals("Abu Bakr", repository.getTodayJourney()?.title)

        fakeLocaleProvider.changeLocale("ar")
        advanceUntilIdle()

        assertEquals("أبو بكر الصديق", repository.getAllJourneys().first().title)
        assertEquals("أبو بكر الصديق", repository.getTodayJourney()?.title)
    }

    @Test
    fun `getAllJourneys reloads if active locale differs from cached locale`() = runTest {
        val repository = createRepository()
        val enJourneys = listOf(createJourneyDto("1", title = "Abu Bakr"))
        val arJourneys = listOf(createJourneyDto("1", title = "أبو بكر الصديق"))
        fakeDataSource.journeysByLocale = mapOf("en" to enJourneys, "ar" to arJourneys)
        fakeLocaleProvider.changeLocale("en")
        advanceUntilIdle()

        assertEquals("Abu Bakr", repository.getAllJourneys().first().title)

        fakeLocaleProvider.changeLocale("ar")
        assertEquals("أبو بكر الصديق", repository.getAllJourneys().first().title)
    }

    @Test
    fun `when locale is regional arabic variant, it normalizes to ar and loads arabic journeys`() = runTest {
        val repository = createRepository()
        val enJourneys = listOf(createJourneyDto("1", title = "Abu Bakr"))
        val arJourneys = listOf(createJourneyDto("1", title = "أبو بكر الصديق"))
        fakeDataSource.journeysByLocale = mapOf("en" to enJourneys, "ar" to arJourneys)
        fakeLocaleProvider.changeLocale("en")
        advanceUntilIdle()

        fakeLocaleProvider.changeLocale("ar-SA")
        advanceUntilIdle()

        assertEquals("أبو بكر الصديق", repository.getAllJourneys().first().title)
    }

    // Helper functions and fakes
    private fun createJourneyDto(id: String, title: String = "Journey $id") = JourneyDto(
        id = id,
        dayNumber = 1,
        title = title,
        subtitle = "Subtitle $id",
        category = "Category",
        person = "Person $id",
        emotion = "Emotion $id",
        theme = "Theme $id",
        heroQuote = "HeroQuote $id",
        intention = "Intention $id",
        durationMinutes = 10,
        difficulty = "Easy",
        estimatedReadingMinutes = 8,
        cover = CoverDto("illustration", "asset"),
        steps = listOf(JourneyStepDto(com.aslmmovic.qurancompanion.data.dto.StepTypeDto.INTRO, "Title", "Content")),
        references = listOf("Ref $id"),
        tags = listOf("Tag $id")
    )

    private class FakeJourneyLocalDataSource : JourneyLocalDataSource {
        var journeys: List<JourneyDto> = emptyList()
        var journeysByLocale: Map<String, List<JourneyDto>> = emptyMap()
        override suspend fun loadJourneys(locale: String): List<JourneyDto> =
            journeysByLocale[locale] ?: journeys
    }

    private class FakeLocaleProvider : LocaleProvider {
        private val _currentLocaleFlow = MutableStateFlow("en")
        override val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()

        override var currentLocale: String
            get() = _currentLocaleFlow.value
            set(value) {
                _currentLocaleFlow.value = value
            }

        override fun changeLocale(locale: String) {
            currentLocale = locale
        }
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
        var dateString = "2026-01-01"
        override fun getCurrentDayOfYear(): Int = dayOfYear
        override fun getCurrentDayOfWeek(): Int = dayOfWeek
        override fun getCurrentDateString(): String = dateString
    }
}
