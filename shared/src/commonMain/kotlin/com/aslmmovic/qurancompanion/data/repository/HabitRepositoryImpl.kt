package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.HabitLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.dto.CustomHabitDto
import com.aslmmovic.qurancompanion.data.dto.toDomain
import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.repository.HabitRepository
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HabitRepositoryImpl(
    private val localDataSource: HabitLocalDataSource,
    private val localeProvider: LocaleProvider,
    private val storage: KeyValueStorage,
    private val dateTimeProvider: DateTimeProvider,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HabitRepository {

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _activeHabits = MutableStateFlow<List<Habit>>(emptyList())
    private val _curatedCatalog = MutableStateFlow<List<Habit>>(emptyList())

    companion object {
        const val KEY_HABITS_INITIALIZED = "habits_tracker_initialized_v1"
        const val KEY_ACTIVE_HABIT_IDS = "habits_active_ids"
        const val KEY_CUSTOM_HABITS = "habits_custom_list"
        const val KEY_STREAK_PREFIX = "habit_streak_"
        const val KEY_LAST_DATE_PREFIX = "habit_last_date_"

        val DEFAULT_HABIT_IDS = listOf(
            "morning_adhkar",
            "duha_prayer",
            "smile_everyone",
            "read_quran_page"
        )
    }

    init {
        scope.launch {
            initDefaultHabitsIfNeeded()
            localeProvider.currentLocaleFlow.collect {
                refreshHabits()
            }
        }
    }

    private suspend fun initDefaultHabitsIfNeeded() {
        val initialized = storage.getBoolean(KEY_HABITS_INITIALIZED, false)
        if (!initialized) {
            val encodedIds = json.encodeToString(DEFAULT_HABIT_IDS)
            storage.putString(KEY_ACTIVE_HABIT_IDS, encodedIds)
            storage.putBoolean(KEY_HABITS_INITIALIZED, true)
        }
    }

    private suspend fun refreshHabits() = withContext(ioDispatcher) {
        mutex.withLock {
            val locale = localeProvider.currentLocale
            val catalogDtos = try {
                localDataSource.loadHabitsCatalog(locale)
            } catch (e: Exception) {
                emptyList()
            }

            val today = dateTimeProvider.getCurrentDateString()
            val yesterday = getYesterdayIso(today)

            val activeIds = loadActiveIds()
            val customHabits = loadCustomHabits()

            // Map curated catalog
            _curatedCatalog.value = catalogDtos.map { dto ->
                val streak = storage.getInt("$KEY_STREAK_PREFIX${dto.id}", 0)
                val lastDate = storage.getString("$KEY_LAST_DATE_PREFIX${dto.id}")
                val isDone = lastDate == today
                val effectiveStreak = when {
                    isDone -> streak
                    lastDate == yesterday -> streak
                    else -> 0
                }
                dto.toDomain(
                    currentStreak = effectiveStreak,
                    isCompletedToday = isDone,
                    lastCompletedDate = lastDate
                )
            }

            // Map active habits preserving active order
            val allHabitsMap = mutableMapOf<String, Habit>()
            _curatedCatalog.value.forEach { allHabitsMap[it.id] = it }

            customHabits.forEach { custom ->
                val streak = storage.getInt("$KEY_STREAK_PREFIX${custom.id}", 0)
                val lastDate = storage.getString("$KEY_LAST_DATE_PREFIX${custom.id}")
                val isDone = lastDate == today
                val effectiveStreak = when {
                    isDone -> streak
                    lastDate == yesterday -> streak
                    else -> 0
                }
                val domainCategory = try {
                    HabitCategory.valueOf(custom.category)
                } catch (e: Exception) {
                    HabitCategory.ALL
                }
                allHabitsMap[custom.id] = Habit(
                    id = custom.id,
                    title = custom.title,
                    description = custom.description,
                    category = domainCategory,
                    isCustom = true,
                    currentStreak = effectiveStreak,
                    isCompletedToday = isDone,
                    lastCompletedDate = lastDate
                )
            }

            _activeHabits.value = activeIds.mapNotNull { allHabitsMap[it] }
        }
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = _activeHabits.map { habits ->
        val today = dateTimeProvider.getCurrentDateString()
        val yesterday = getYesterdayIso(today)
        habits.map { habit ->
            val isDone = habit.lastCompletedDate == today
            val streak = storage.getInt("$KEY_STREAK_PREFIX${habit.id}", 0)
            val effectiveStreak = when {
                isDone -> streak
                habit.lastCompletedDate == yesterday -> streak
                else -> 0
            }
            habit.copy(
                isCompletedToday = isDone,
                currentStreak = effectiveStreak
            )
        }
    }

    override fun observeCuratedCatalog(): Flow<List<Habit>> = _curatedCatalog.map { catalog ->
        val today = dateTimeProvider.getCurrentDateString()
        val yesterday = getYesterdayIso(today)
        catalog.map { habit ->
            val isDone = habit.lastCompletedDate == today
            val streak = storage.getInt("$KEY_STREAK_PREFIX${habit.id}", 0)
            val effectiveStreak = when {
                isDone -> streak
                habit.lastCompletedDate == yesterday -> streak
                else -> 0
            }
            habit.copy(
                isCompletedToday = isDone,
                currentStreak = effectiveStreak
            )
        }
    }

    override suspend fun toggleHabitCompletion(habitId: String): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                val today = dateTimeProvider.getCurrentDateString()
                val yesterday = getYesterdayIso(today)
                val currentStreak = storage.getInt("$KEY_STREAK_PREFIX$habitId", 0)
                val lastDate = storage.getString("$KEY_LAST_DATE_PREFIX$habitId")

                if (lastDate == today) {
                    // Undo completion
                    val revertedStreak = maxOf(0, currentStreak - 1)
                    storage.putInt("$KEY_STREAK_PREFIX$habitId", revertedStreak)
                    storage.putString("$KEY_LAST_DATE_PREFIX$habitId", if (revertedStreak > 0) yesterday else "")
                } else {
                    // Complete today
                    val effectiveBaseStreak = if (lastDate == yesterday) currentStreak else 0
                    val newStreak = effectiveBaseStreak + 1
                    storage.putInt("$KEY_STREAK_PREFIX$habitId", newStreak)
                    storage.putString("$KEY_LAST_DATE_PREFIX$habitId", today)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        refreshHabits()
        Result.success(Unit)
    }

    override suspend fun addHabitFromCatalog(habitId: String): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                val activeIds = loadActiveIds().toMutableList()
                if (!activeIds.contains(habitId)) {
                    activeIds.add(habitId)
                    storage.putString(KEY_ACTIVE_HABIT_IDS, json.encodeToString(activeIds))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        refreshHabits()
        Result.success(Unit)
    }

    override suspend fun createCustomHabit(
        title: String,
        description: String,
        category: HabitCategory
    ): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                val customHabits = loadCustomHabits().toMutableList()
                val id = "custom_" + (title.hashCode() + (1..10000).random())
                val newCustom = CustomHabitDto(
                    id = id,
                    title = title,
                    description = description,
                    category = category.name
                )
                customHabits.add(newCustom)
                storage.putString(KEY_CUSTOM_HABITS, json.encodeToString(customHabits))

                val activeIds = loadActiveIds().toMutableList()
                if (!activeIds.contains(id)) {
                    activeIds.add(id)
                    storage.putString(KEY_ACTIVE_HABIT_IDS, json.encodeToString(activeIds))
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        refreshHabits()
        Result.success(Unit)
    }

    override suspend fun removeHabit(habitId: String): Result<Unit> = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                val activeIds = loadActiveIds().toMutableList()
                activeIds.remove(habitId)
                storage.putString(KEY_ACTIVE_HABIT_IDS, json.encodeToString(activeIds))
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
        refreshHabits()
        Result.success(Unit)
    }

    private fun loadActiveIds(): List<String> {
        val raw = storage.getString(KEY_ACTIVE_HABIT_IDS) ?: return DEFAULT_HABIT_IDS
        return try {
            json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) {
            DEFAULT_HABIT_IDS
        }
    }

    private fun loadCustomHabits(): List<CustomHabitDto> {
        val raw = storage.getString(KEY_CUSTOM_HABITS) ?: return emptyList()
        return try {
            json.decodeFromString<List<CustomHabitDto>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getYesterdayIso(todayIso: String): String {
        val parts = todayIso.split("-")
        if (parts.size != 3) return ""
        var year = parts[0].toIntOrNull() ?: return ""
        var month = parts[1].toIntOrNull() ?: return ""
        var day = parts[2].toIntOrNull() ?: return ""

        day -= 1
        if (day < 1) {
            month -= 1
            if (month < 1) {
                month = 12
                year -= 1
            }
            day = daysInMonth(year, month)
        }
        return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
