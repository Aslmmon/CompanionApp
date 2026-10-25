package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(
    private val storage: KeyValueStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    private val _preferences = MutableStateFlow(UserPreferences())
    private val _bookmarkedJourneys = MutableStateFlow<Set<String>>(emptySet())
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val saveMutex = Mutex()

    init {
        scope.launch {
            _preferences.value = loadFromStorage()
            _bookmarkedJourneys.value = loadBookmarksFromStorage()
        }
    }

    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun saveUserPreferences(preferences: UserPreferences) = withContext(ioDispatcher) {
        saveMutex.withLock {
            try {
                storage.putInt(KEY_REMINDER_HOUR, preferences.reminderHour)
                storage.putInt(KEY_REMINDER_MINUTE, preferences.reminderMinute)
                storage.putBoolean(KEY_REMINDER_ENABLED, preferences.isReminderEnabled)
                preferences.preferredLanguage?.let { storage.putString(KEY_PREFERRED_LANGUAGE, it) }
                storage.putString(KEY_DARK_MODE, preferences.isDarkMode?.toString() ?: "system")
                preferences.selectedTheme?.let { storage.putString(KEY_SELECTED_THEME, it) }
                _preferences.value = preferences
            } catch (e: Exception) {
                // handle exception silently or log
            }
        }
    }

    override fun getBookmarkedJourneyIds(): Flow<Set<String>> = _bookmarkedJourneys.asStateFlow()

    override suspend fun toggleJourneyBookmark(journeyId: String): Boolean = withContext(ioDispatcher) {
        saveMutex.withLock {
            val current = _bookmarkedJourneys.value.toMutableSet()
            val isNowBookmarked = if (current.contains(journeyId)) {
                current.remove(journeyId)
                false
            } else {
                current.add(journeyId)
                true
            }
            try {
                storage.putString(KEY_BOOKMARKED_JOURNEYS, current.joinToString(","))
            } catch (e: Exception) {
                // handle exception silently or log
            }
            _bookmarkedJourneys.value = current
            isNowBookmarked
        }
    }

    private fun loadFromStorage(): UserPreferences {
        return try {
            val isDarkModeString = storage.getString(KEY_DARK_MODE)
            val isDarkMode = if (isDarkModeString == "system") null else isDarkModeString?.toBooleanStrictOrNull()

            UserPreferences(
                reminderHour = storage.getInt(KEY_REMINDER_HOUR, 8),
                reminderMinute = storage.getInt(KEY_REMINDER_MINUTE, 0),
                isReminderEnabled = storage.getBoolean(KEY_REMINDER_ENABLED, true),
                preferredLanguage = storage.getString(KEY_PREFERRED_LANGUAGE),
                isDarkMode = isDarkMode,
                selectedTheme = storage.getString(KEY_SELECTED_THEME) ?: "classic"
            )
        } catch (e: Exception) {
            UserPreferences() // Safe fallback
        }
    }

    private fun loadBookmarksFromStorage(): Set<String> {
        return try {
            storage.getString(KEY_BOOKMARKED_JOURNEYS)
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    companion object {
        private const val KEY_REMINDER_HOUR = "pref_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "pref_reminder_minute"
        private const val KEY_REMINDER_ENABLED = "pref_reminder_enabled"
        private const val KEY_PREFERRED_LANGUAGE = "pref_preferred_language"
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_BOOKMARKED_JOURNEYS = "pref_bookmarked_journeys"
        private const val KEY_SELECTED_THEME = "pref_selected_theme"
    }
}
