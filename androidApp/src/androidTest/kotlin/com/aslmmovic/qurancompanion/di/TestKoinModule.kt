package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.Cover
import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.HabitRepository
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.domain.util.AppBuildInfoProvider
import com.aslmmovic.qurancompanion.domain.util.DateTimeProvider
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.dsl.module

object TestKoinModule {

    val sampleJourney = Journey(
        id = "sample_journey_1",
        dayNumber = 1,
        title = "Abu Bakr as-Siddiq",
        subtitle = "The Truthful Companion",
        category = "Caliph",
        person = "Abu Bakr",
        emotion = "Loyalty",
        theme = "Sahaba",
        heroQuote = "If he said it, then it is true.",
        intention = "Strive for truthfulness",
        durationMinutes = 10,
        difficulty = "Easy",
        estimatedReadingMinutes = 8,
        cover = Cover("illustration", "asset"),
        steps = listOf(
            JourneyStep(StepType.INTRO, "Introduction", "Abu Bakr was the closest companion."),
            JourneyStep(StepType.STORY, "The Cave", "They hid in the cave of Thawr."),
            JourneyStep(StepType.ACTION, "Reflection", "Reflect on loyalty.")
        ),
        references = listOf("Sahih Bukhari"),
        tags = listOf("Truthfulness"),
        arabicVerse = "إِلَّا تَنصُرُوهُ فَقَدْ نَصَرَهُ اللَّهُ"
    )

    val sampleSahaba = Sahaba(
        id = "abu_bakr",
        name = "Abu Bakr as-Siddiq",
        arabicName = "أبو بكر الصديق",
        initialLetterArabic = "أ",
        epithet = "As-Siddiq",
        category = SahabaCategory.TEN_PROMISED,
        journeyCount = 10,
        bio = "The first adult free male to convert to Islam."
    )

    val sampleHabit = Habit(
        id = "habit_1",
        title = "Morning Adhkar",
        description = "Recite morning remembrances",
        category = HabitCategory.ADHKAR,
        currentStreak = 3,
        isCompletedToday = false
    )

    val catalogHabit = Habit(
        id = "habit_catalog_1",
        title = "Duha Prayer",
        description = "Pray two rak'ahs after sunrise",
        category = HabitCategory.DAILY_PRAYERS,
        currentStreak = 0,
        isCompletedToday = false
    )

    class TestUserPreferencesRepository : UserPreferencesRepository {
        val prefsFlow = MutableStateFlow(
            UserPreferences(
                preferredLanguage = "en",
                isReminderEnabled = true,
                reminderHour = 8,
                reminderMinute = 0,
                isDarkMode = false
            )
        )
        val bookmarksFlow = MutableStateFlow<Set<String>>(emptySet())

        override fun getUserPreferences(): Flow<UserPreferences> = prefsFlow.asStateFlow()

        override suspend fun saveUserPreferences(preferences: UserPreferences) {
            prefsFlow.value = preferences
        }

        override fun getBookmarkedJourneyIds(): Flow<Set<String>> = bookmarksFlow.asStateFlow()

        override suspend fun toggleJourneyBookmark(journeyId: String): Boolean {
            val current = bookmarksFlow.value.toMutableSet()
            val added = if (current.contains(journeyId)) {
                current.remove(journeyId)
                false
            } else {
                current.add(journeyId)
                true
            }
            bookmarksFlow.value = current
            return added
        }
    }

    class TestJourneyRepository : JourneyRepository {
        var currentJourney: Journey? = sampleJourney
        var tomorrowJourney: Journey? = sampleJourney
        val completedFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())
        val debugDayOffsetFlow = MutableStateFlow(0)

        override suspend fun getAllJourneys(): List<Journey> = listOfNotNull(currentJourney)

        override suspend fun getTodayJourney(): Journey? = currentJourney

        override suspend fun getTomorrowJourney(): Journey? = tomorrowJourney

        override fun isCompleted(journeyId: String, date: String): Flow<Boolean> =
            completedFlow.map { it["$journeyId|$date"] ?: false }

        override fun getWeeklyProgress(): Flow<List<Boolean>> =
            flowOf(listOf(true, false, true, false, false, false, false))

        override suspend fun markCompleted(journeyId: String, date: String) {
            completedFlow.value += ("$journeyId|$date" to true)
        }

        override suspend fun resetCompletion(journeyId: String, date: String) {
            completedFlow.value += ("$journeyId|$date" to false)
        }

        override fun getDebugDayOffset(): Flow<Int> = debugDayOffsetFlow.asStateFlow()

        override suspend fun incrementDebugDayOffset() {
            debugDayOffsetFlow.value += 1
        }
    }

    class TestSahabaRepository : SahabaRepository {
        var catalog = listOf(sampleSahaba)

        override fun getSahabaCatalog(): Flow<List<Sahaba>> = flowOf(catalog)

        override suspend fun getSahabaById(id: String): Result<Sahaba> {
            val found = catalog.firstOrNull { it.id == id }
            return if (found != null) Result.success(found) else Result.failure(NoSuchElementException(id))
        }
    }

    class TestHabitRepository : HabitRepository {
        val activeHabitsFlow = MutableStateFlow(listOf(sampleHabit))
        val catalogHabitsFlow = MutableStateFlow(listOf(sampleHabit, catalogHabit))

        override fun observeActiveHabits(): Flow<List<Habit>> = activeHabitsFlow.asStateFlow()

        override fun observeCuratedCatalog(): Flow<List<Habit>> = catalogHabitsFlow.asStateFlow()

        override suspend fun toggleHabitCompletion(habitId: String): Result<Unit> {
            val current = activeHabitsFlow.value.map { habit ->
                if (habit.id == habitId) {
                    val nextCompleted = !habit.isCompletedToday
                    val nextStreak = if (nextCompleted) habit.currentStreak + 1 else maxOf(0, habit.currentStreak - 1)
                    habit.copy(isCompletedToday = nextCompleted, currentStreak = nextStreak)
                } else habit
            }
            activeHabitsFlow.value = current
            return Result.success(Unit)
        }

        override suspend fun addHabitFromCatalog(habitId: String): Result<Unit> {
            val habitToAdd = catalogHabitsFlow.value.firstOrNull { it.id == habitId }
            if (habitToAdd != null && activeHabitsFlow.value.none { it.id == habitId }) {
                activeHabitsFlow.value = activeHabitsFlow.value + habitToAdd
            }
            return Result.success(Unit)
        }

        override suspend fun createCustomHabit(
            title: String,
            description: String,
            category: HabitCategory
        ): Result<Unit> {
            val custom = Habit(
                id = "custom_${title.hashCode()}",
                title = title,
                description = description,
                category = category,
                isCustom = true
            )
            activeHabitsFlow.value = activeHabitsFlow.value + custom
            return Result.success(Unit)
        }

        override suspend fun removeHabit(habitId: String): Result<Unit> {
            activeHabitsFlow.value = activeHabitsFlow.value.filterNot { it.id == habitId }
            return Result.success(Unit)
        }
    }

    class TestLocaleProvider : LocaleProvider {
        private val _currentLocaleFlow = MutableStateFlow("en")
        override val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()
        override var currentLocale: String
            get() = _currentLocaleFlow.value
            set(value) { _currentLocaleFlow.value = value }
        override fun changeLocale(locale: String) { currentLocale = locale }
    }

    class TestNotificationScheduler : NotificationScheduler {
        var scheduledHour: Int? = null
        var scheduledMinute: Int? = null
        var isCancelled: Boolean = false
        var immediateNotificationTitle: String? = null
        var immediateNotificationBody: String? = null

        override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
            scheduledHour = hour
            scheduledMinute = minute
            isCancelled = false
        }

        override fun schedulePeriodicReminder(intervalMinutes: Long, title: String, body: String) {
            isCancelled = false
        }

        override fun cancelDailyReminder() {
            isCancelled = true
        }

        override suspend fun requestNotificationPermission(): Boolean = true

        override fun showImmediateNotification(title: String, body: String) {
            immediateNotificationTitle = title
            immediateNotificationBody = body
        }
    }

    class TestDateTimeProvider : DateTimeProvider {
        override fun getCurrentDayOfYear(): Int = 1
        override fun getCurrentDayOfWeek(): Int = 1
        override fun getCurrentDateString(): String = "2026-01-01"
    }

    class TestAppBuildInfoProvider : AppBuildInfoProvider {
        override val versionName: String = "1.0.0"
        override val buildNumber: Long = 100L
        override val isDebug: Boolean = true
    }

    var userPreferencesRepo = TestUserPreferencesRepository()
    var journeyRepo = TestJourneyRepository()
    var sahabaRepo = TestSahabaRepository()
    var habitRepo = TestHabitRepository()
    var localeProv = TestLocaleProvider()
    var notificationSched = TestNotificationScheduler()
    var dateTimeProv = TestDateTimeProvider()
    var buildInfoProv = TestAppBuildInfoProvider()

    fun reset() {
        userPreferencesRepo = TestUserPreferencesRepository()
        journeyRepo = TestJourneyRepository()
        sahabaRepo = TestSahabaRepository()
        habitRepo = TestHabitRepository()
        localeProv = TestLocaleProvider()
        notificationSched = TestNotificationScheduler()
        dateTimeProv = TestDateTimeProvider()
        buildInfoProv = TestAppBuildInfoProvider()
    }

    val module = module {
        single<UserPreferencesRepository> { userPreferencesRepo }
        single<JourneyRepository> { journeyRepo }
        single<SahabaRepository> { sahabaRepo }
        single<HabitRepository> { habitRepo }
        single<LocaleProvider> { localeProv }
        single<NotificationScheduler> { notificationSched }
        single<DateTimeProvider> { dateTimeProv }
        single<AppBuildInfoProvider> { buildInfoProv }
    }
}
