package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class UserPreferencesRepositoryImpl(
    private val storage: KeyValueStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserPreferencesRepository {

    private val _preferences = MutableStateFlow(loadFromStorage())

    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun saveUserPreferences(preferences: UserPreferences) = withContext(ioDispatcher) {
        try {
            storage.putInt(KEY_REMINDER_HOUR, preferences.reminderHour)
            storage.putInt(KEY_REMINDER_MINUTE, preferences.reminderMinute)
            storage.putBoolean(KEY_REMINDER_ENABLED, preferences.isReminderEnabled)
            preferences.preferredLanguage?.let { storage.putString(KEY_PREFERRED_LANGUAGE, it) }
            storage.putString(KEY_DARK_MODE, preferences.isDarkMode?.toString() ?: "system")
            _preferences.value = preferences
        } catch (e: Exception) {
            // handle exception silently or log
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
                isDarkMode = isDarkMode
            )
        } catch (e: Exception) {
            UserPreferences() // Safe fallback
        }
    }

    companion object {
        private const val KEY_REMINDER_HOUR = "pref_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "pref_reminder_minute"
        private const val KEY_REMINDER_ENABLED = "pref_reminder_enabled"
        private const val KEY_PREFERRED_LANGUAGE = "pref_preferred_language"
        private const val KEY_DARK_MODE = "pref_dark_mode"
    }
}
