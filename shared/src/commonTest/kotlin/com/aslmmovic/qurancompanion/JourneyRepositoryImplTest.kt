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
    fun `test_AC04_givenFreshInstall_whenGetTodayJourney_thenStartsAtDayOne`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1", title = "Abu Bakr"),
            createJourneyDto("2", title = "Umar"),
            createJourneyDto("3", title = "Uthman")
        )
        fakeDataSource.journeys = journeys

        // Even on day 263 of the year, fresh install starts on Day 1 (index 0)
        fakeDateTimeProvider.dayOfYear = 263
        fakeDateTimeProvider.dateString = "2026-09-20"

        assertEquals("1", repository.getTodayJourney()?.id)
        assertEquals("Abu Bakr", repository.getTodayJourney()?.title)
        assertEquals("2", repository.getTomorrowJourney()?.id)
    }

    @Test
    fun `test_AC05_givenCompletedPreviousDay_whenDateAdvances_thenProgressesToNextDay`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1", title = "Abu Bakr"),
            createJourneyDto("2", title = "Umar"),
            createJourneyDto("3", title = "Uthman")
        )
        fakeDataSource.journeys = journeys

        fakeDateTimeProvider.dateString = "2026-09-20"
        assertEquals("1", repository.getTodayJourney()?.id)

        // Complete Day 1 on 2026-09-20
        repository.markCompleted("1", "2026-09-20")

        // Advance date to 2026-09-21
        fakeDateTimeProvider.dateString = "2026-09-21"

        // Today progresses to Day 2 (Umar)
        assertEquals("2", repository.getTodayJourney()?.id)
        assertEquals("Umar", repository.getTodayJourney()?.title)
        assertEquals("3", repository.getTomorrowJourney()?.id)
    }

    @Test
    fun `given uncompleted journey when date advances then stays on same day`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1", title = "Abu Bakr"),
            createJourneyDto("2", title = "Umar"),
            createJourneyDto("3", title = "Uthman")
        )
        fakeDataSource.journeys = journeys

        fakeDateTimeProvider.dateString = "2026-09-20"
        assertEquals("1", repository.getTodayJourney()?.id)

        // Do NOT mark completed, advance date to 2026-09-21
        fakeDateTimeProvider.dateString = "2026-09-21"

        // Remains on Day 1 (Abu Bakr)
        assertEquals("1", repository.getTodayJourney()?.id)
        assertEquals("2", repository.getTomorrowJourney()?.id)
    }

    @Test
    fun `debug day offset increments smoothly`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1"),
            createJourneyDto("2"),
            createJourneyDto("3")
        )
        fakeDataSource.journeys = journeys
        fakeDateTimeProvider.dateString = "2026-09-20"

        assertEquals("1", repository.getTodayJourney()?.id)

        repository.incrementDebugDayOffset()
        assertEquals("2", repository.getTodayJourney()?.id)

        repository.incrementDebugDayOffset()
        assertEquals("3", repository.getTodayJourney()?.id)

        repository.incrementDebugDayOffset()
        assertEquals("1", repository.getTodayJourney()?.id)
    }

    @Test
    fun `getWeeklyProgress correctly calculates completed states offset by current day of week`() = runTest {
        val repository = createRepository()
        val journeys = listOf(
            createJourneyDto("1"),
            createJourneyDto("2"),
            createJourneyDto("3"),
            createJourneyDto("4"),
            createJourneyDto("5")
        )
        fakeDataSource.journeys = journeys

        // Setup date: Wednesday (3rd day of week)
        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dateString = "2026-01-10" // Jan 10 (Wednesday)

        // Populate database with all journeys to trigger state cache initialization
        repository.getAllJourneys()

        // Complete on Monday (Jan 8) and Wednesday (Jan 10)
        repository.markCompleted("3", "2026-01-08")
        repository.markCompleted("5", "2026-01-10")

        val progress = repository.getWeeklyProgress().first()

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

        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dateString = "2026-01-10"
        repository.getAllJourneys()

        repository.markCompleted("1", "2026-01-10") // Wednesday
        repository.markCompleted("2", "2026-01-08") // Monday

        val progressList = mutableListOf<List<Boolean>>()
        val job = launch {
            repository.getWeeklyProgress().collect { progressList.add(it) }
        }
        advanceUntilIdle()

        repository.incrementDebugDayOffset()
        advanceUntilIdle()

        job.cancel()

        assertTrue(progressList.size >= 2)
        assertEquals(listOf(true, false, true, false, false, false, false), progressList[0])
        assertEquals(listOf(true, false, true, false, false, false, false), progressList[1])
    }

    @Test
    fun `completing today journey only ticks today in weekly progress`() = runTest {
        val repository = createRepository()
        fakeDataSource.journeys = listOf(
            createJourneyDto("1"), createJourneyDto("2"), createJourneyDto("3")
        )

        fakeDateTimeProvider.dayOfWeek = 3
        fakeDateTimeProvider.dateString = "2026-08-08"
        repository.getAllJourneys()

        repository.markCompleted("1", "2026-08-08")
        val progress = repository.getWeeklyProgress().first()

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
    fun `when locale changes to arabic then cached journeys reload with arabic journeys`() = runTest {
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
    fun `when locale is regional arabic variant then it normalizes to ar and loads arabic journeys`() = runTest {
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
