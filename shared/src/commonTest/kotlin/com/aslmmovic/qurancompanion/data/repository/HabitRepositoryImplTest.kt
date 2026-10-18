package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.HabitLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.HabitDto
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HabitRepositoryImplTest {

    private val fakeDataSource = FakeHabitLocalDataSource()
    private val fakeLocaleProvider = FakeLocaleProvider()
    private val fakeStorage = FakeKeyValueStorage()
    private val fakeDateTimeProvider = FakeDateTimeProvider()
    private val json = Json { ignoreUnknownKeys = true }

    private fun TestScope.createRepository() = HabitRepositoryImpl(
        localDataSource = fakeDataSource,
        localeProvider = fakeLocaleProvider,
        storage = fakeStorage,
        dateTimeProvider = fakeDateTimeProvider,
        json = json,
        ioDispatcher = UnconfinedTestDispatcher(testScheduler)
    )

    @BeforeTest
    fun setUp() {
        fakeDataSource.habits = listOf(
            HabitDto("morning_adhkar", "Morning Adhkar", "Desc 1", "Adhkar", isDefaultActive = true),
            HabitDto("duha_prayer", "Duha Prayer", "Desc 2", "DailyPrayers", isDefaultActive = true),
            HabitDto("smile_everyone", "Smile at Everyone", "Desc 3", "Character", isDefaultActive = true),
            HabitDto("read_quran_page", "Read 1 Page Quran", "Desc 4", "QuranAndFasting", isDefaultActive = true),
            HabitDto("tahajjud_prayer", "Tahajjud", "Desc 5", "DailyPrayers", isDefaultActive = false)
        )
        fakeDateTimeProvider.dateString = "2026-10-17"
    }

    @Test
    fun `test_AC01_freshInstall_seeds4DefaultHabits`() = runTest {
        val repository = createRepository()
        val active = repository.observeActiveHabits().first()

        assertEquals(4, active.size)
        assertEquals("morning_adhkar", active[0].id)
        assertEquals("duha_prayer", active[1].id)
        assertEquals("smile_everyone", active[2].id)
        assertEquals("read_quran_page", active[3].id)
        assertTrue(active.all { it.currentStreak == 0 && !it.isCompletedToday })
    }

    @Test
    fun `test_AC02_toggleHabit_completesTodayAndStartsStreakAtOne`() = runTest {
        val repository = createRepository()
        val res = repository.toggleHabitCompletion("morning_adhkar")
        assertTrue(res.isSuccess)

        val active = repository.observeActiveHabits().first()
        val habit = active.first { it.id == "morning_adhkar" }
        assertTrue(habit.isCompletedToday)
        assertEquals(1, habit.currentStreak)
    }

    @Test
    fun `test_AC03_toggleHabit_consecutiveDays_incrementsStreak`() = runTest {
        fakeDateTimeProvider.dateString = "2026-10-16"
        val repository = createRepository()
        repository.toggleHabitCompletion("morning_adhkar")

        // Advance to next day
        fakeDateTimeProvider.dateString = "2026-10-17"
        val nextDayHabitBeforeCheck = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertFalse(nextDayHabitBeforeCheck.isCompletedToday)
        assertEquals(1, nextDayHabitBeforeCheck.currentStreak) // Preserved ongoing streak

        repository.toggleHabitCompletion("morning_adhkar")
        val nextDayHabitAfterCheck = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertTrue(nextDayHabitAfterCheck.isCompletedToday)
        assertEquals(2, nextDayHabitAfterCheck.currentStreak)
    }

    @Test
    fun `test_AC04_toggleHabit_afterMissedDay_resetsStreakToOne`() = runTest {
        fakeDateTimeProvider.dateString = "2026-10-10"
        val repository = createRepository()
        repository.toggleHabitCompletion("morning_adhkar")

        // Jump 5 days forward (missed 4 days)
        fakeDateTimeProvider.dateString = "2026-10-15"
        val habitBeforeCheck = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertEquals(0, habitBeforeCheck.currentStreak) // Reset to 0d due to missed days

        repository.toggleHabitCompletion("morning_adhkar")
        val habitAfterCheck = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertEquals(1, habitAfterCheck.currentStreak)
    }

    @Test
    fun `test_AC05_undoToggleHabit_revertsTodayCompletion`() = runTest {
        val repository = createRepository()
        repository.toggleHabitCompletion("morning_adhkar")
        val habitChecked = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertTrue(habitChecked.isCompletedToday)
        assertEquals(1, habitChecked.currentStreak)

        // Untoggle
        repository.toggleHabitCompletion("morning_adhkar")
        val habitUnchecked = repository.observeActiveHabits().first().first { it.id == "morning_adhkar" }
        assertFalse(habitUnchecked.isCompletedToday)
        assertEquals(0, habitUnchecked.currentStreak)
    }

    @Test
    fun `test_AC09_addHabitFromCatalog_addsToActiveList`() = runTest {
        val repository = createRepository()
        assertEquals(4, repository.observeActiveHabits().first().size)

        repository.addHabitFromCatalog("tahajjud_prayer")
        val active = repository.observeActiveHabits().first()
        assertEquals(5, active.size)
        assertTrue(active.any { it.id == "tahajjud_prayer" })
    }

    @Test
    fun `test_createCustomHabit_persistsAndAppearsInActiveList`() = runTest {
        val repository = createRepository()
        repository.createCustomHabit("Give Charity Daily", "Sadaqah cleanses wealth", HabitCategory.CHARACTER)

        val active = repository.observeActiveHabits().first()
        assertEquals(5, active.size)
        val custom = active.first { it.title == "Give Charity Daily" }
        assertTrue(custom.isCustom)
        assertEquals(HabitCategory.CHARACTER, custom.category)
    }

    @Test
    fun `test_AC10_removeHabit_removesFromActiveList`() = runTest {
        val repository = createRepository()
        repository.removeHabit("duha_prayer")

        val active = repository.observeActiveHabits().first()
        assertEquals(3, active.size)
        assertFalse(active.any { it.id == "duha_prayer" })
    }

    private class FakeHabitLocalDataSource : HabitLocalDataSource {
        var habits: List<HabitDto> = emptyList()
        override suspend fun loadHabitsCatalog(locale: String): List<HabitDto> = habits
    }

    private class FakeLocaleProvider : LocaleProvider {
        private val _currentLocaleFlow = MutableStateFlow("en")
        override val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()
        override var currentLocale: String
            get() = _currentLocaleFlow.value
            set(value) { _currentLocaleFlow.value = value }
        override fun changeLocale(locale: String) { currentLocale = locale }
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
        var dateString = "2026-10-17"
        override fun getCurrentDayOfYear(): Int = 290
        override fun getCurrentDayOfWeek(): Int = 6
        override fun getCurrentDateString(): String = dateString
    }
}
