package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.fakes.FakeHabitRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitUseCasesTest {

    private lateinit var repository: FakeHabitRepository
    private lateinit var getActiveHabitsUseCase: GetActiveHabitsUseCase
    private lateinit var getCuratedHabitsUseCase: GetCuratedHabitsUseCase
    private lateinit var toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
    private lateinit var addHabitFromCatalogUseCase: AddHabitFromCatalogUseCase
    private lateinit var createCustomHabitUseCase: CreateCustomHabitUseCase
    private lateinit var removeHabitUseCase: RemoveHabitUseCase

    private val habit1 = Habit(
        id = "morning_adhkar",
        title = "Morning Adhkar",
        description = "Spiritual protection and mindfulness",
        category = HabitCategory.ADHKAR
    )
    private val habit2 = Habit(
        id = "duha_prayer",
        title = "Duha Prayer",
        description = "The prayer of the repentant",
        category = HabitCategory.DAILY_PRAYERS
    )
    private val habit3 = Habit(
        id = "smile_everyone",
        title = "Smile at Everyone",
        description = "Sunnah of the Prophet (ﷺ)",
        category = HabitCategory.CHARACTER
    )

    @BeforeTest
    fun setUp() {
        repository = FakeHabitRepository(
            initialActive = listOf(habit1, habit2),
            initialCatalog = listOf(habit1, habit2, habit3)
        )
        getActiveHabitsUseCase = GetActiveHabitsUseCase(repository)
        getCuratedHabitsUseCase = GetCuratedHabitsUseCase(repository)
        toggleHabitCompletionUseCase = ToggleHabitCompletionUseCase(repository)
        addHabitFromCatalogUseCase = AddHabitFromCatalogUseCase(repository)
        createCustomHabitUseCase = CreateCustomHabitUseCase(repository)
        removeHabitUseCase = RemoveHabitUseCase(repository)
    }

    @Test
    fun `test_observeActiveHabits_returnsActiveHabits`() = runTest {
        val active = getActiveHabitsUseCase().first()
        assertEquals(2, active.size)
        assertEquals("morning_adhkar", active[0].id)
    }

    @Test
    fun `test_filterCuratedCatalog_byCategory_returnsMatching`() = runTest {
        val result = getCuratedHabitsUseCase(category = HabitCategory.ADHKAR).first()
        assertEquals(1, result.size)
        assertEquals("morning_adhkar", result[0].id)
    }

    @Test
    fun `test_toggleHabitCompletion_togglesState`() = runTest {
        val initial = getActiveHabitsUseCase().first().first { it.id == "morning_adhkar" }
        assertEquals(false, initial.isCompletedToday)

        val result = toggleHabitCompletionUseCase("morning_adhkar")
        assertTrue(result.isSuccess)

        val updated = getActiveHabitsUseCase().first().first { it.id == "morning_adhkar" }
        assertEquals(true, updated.isCompletedToday)
        assertEquals(1, updated.currentStreak)
    }

    @Test
    fun `test_addHabitFromCatalog_addsToActiveList`() = runTest {
        val initial = getActiveHabitsUseCase().first()
        assertEquals(2, initial.size)

        val result = addHabitFromCatalogUseCase("smile_everyone")
        assertTrue(result.isSuccess)

        val updated = getActiveHabitsUseCase().first()
        assertEquals(3, updated.size)
        assertTrue(updated.any { it.id == "smile_everyone" })
    }

    @Test
    fun `test_createCustomHabit_addsToActiveList`() = runTest {
        val result = createCustomHabitUseCase(
            title = "Read Surah Al-Kahf",
            description = "Light between two Fridays",
            category = HabitCategory.QURAN_AND_FASTING
        )
        assertTrue(result.isSuccess)

        val updated = getActiveHabitsUseCase().first()
        assertEquals(3, updated.size)
        assertTrue(updated.any { it.title == "Read Surah Al-Kahf" })
    }

    @Test
    fun `test_removeHabit_removesFromActiveList`() = runTest {
        val result = removeHabitUseCase("morning_adhkar")
        assertTrue(result.isSuccess)

        val updated = getActiveHabitsUseCase().first()
        assertEquals(1, updated.size)
        assertEquals("duha_prayer", updated[0].id)
    }
}
