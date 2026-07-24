package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepositoryImpl(
    private val storage: KeyValueStorage
) : UserPreferencesRepository {

    private val _preferences = MutableStateFlow(loadFromStorage())
    
    override fun getUserPreferences(): Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun saveUserPreferences(preferences: UserPreferences) {
        storage.putInt(KEY_REMINDER_HOUR, preferences.reminderHour)
        storage.putInt(KEY_REMINDER_MINUTE, preferences.reminderMinute)
        storage.putBoolean(KEY_REMINDER_ENABLED, preferences.isReminderEnabled)
        storage.putBoolean(KEY_SETUP_COMPLETED, preferences.isSetupCompleted)
        
        _preferences.value = preferences
    }

    private fun loadFromStorage(): UserPreferences {
        return UserPreferences(
            reminderHour = storage.getInt(KEY_REMINDER_HOUR, 8),
            reminderMinute = storage.getInt(KEY_REMINDER_MINUTE, 0),
            isReminderEnabled = storage.getBoolean(KEY_REMINDER_ENABLED, true),
            isSetupCompleted = storage.getBoolean(KEY_SETUP_COMPLETED, false)
        )
    }

    companion object {
        private const val KEY_REMINDER_HOUR = "pref_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "pref_reminder_minute"
        private const val KEY_REMINDER_ENABLED = "pref_reminder_enabled"
        private const val KEY_SETUP_COMPLETED = "pref_setup_completed"
    }
}
