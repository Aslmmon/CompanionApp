package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeStorage = TestKeyValueStorage()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleJourneyBookmark adds journey ID to storage and emits updated set`() = runTest(testDispatcher) {
        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        assertTrue(repository.getBookmarkedJourneyIds().first().isEmpty())

        // Toggle ON
        val isBookmarked1 = repository.toggleJourneyBookmark("journey-1")
        advanceUntilIdle()

        assertTrue(isBookmarked1)
        assertEquals(setOf("journey-1"), repository.getBookmarkedJourneyIds().first())
        assertEquals("journey-1", fakeStorage.getString("pref_bookmarked_journeys"))

        // Toggle ON second journey
        val isBookmarked2 = repository.toggleJourneyBookmark("journey-2")
        advanceUntilIdle()

        assertTrue(isBookmarked2)
        assertEquals(setOf("journey-1", "journey-2"), repository.getBookmarkedJourneyIds().first())

        // Toggle OFF first journey
        val isBookmarked3 = repository.toggleJourneyBookmark("journey-1")
        advanceUntilIdle()

        assertFalse(isBookmarked3)
        assertEquals(setOf("journey-2"), repository.getBookmarkedJourneyIds().first())
        assertEquals("journey-2", fakeStorage.getString("pref_bookmarked_journeys"))
    }

    @Test
    fun `initialization loads existing bookmarks from storage`() = runTest(testDispatcher) {
        fakeStorage.putString("pref_bookmarked_journeys", "journey-a,journey-b")

        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val bookmarks = repository.getBookmarkedJourneyIds().first()
        assertEquals(setOf("journey-a", "journey-b"), bookmarks)
    }

    @Test
    fun getUserPreferences_freshStorage_returnsDefaultPreferences() = runTest(testDispatcher) {
        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val prefs = repository.getUserPreferences().first()
        assertEquals(8, prefs.reminderHour)
        assertEquals(0, prefs.reminderMinute)
        assertTrue(prefs.isReminderEnabled)
        assertNull(prefs.preferredLanguage)
        assertNull(prefs.isDarkMode)
    }

    @Test
    fun saveUserPreferences_fullPreferences_roundTripsAllFields() = runTest(testDispatcher) {
        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val newPrefs = UserPreferences(
            reminderHour = 6,
            reminderMinute = 30,
            isReminderEnabled = false,
            preferredLanguage = "ar",
            isDarkMode = true
        )
        repository.saveUserPreferences(newPrefs)
        advanceUntilIdle()

        val loaded = repository.getUserPreferences().first()
        assertEquals(newPrefs, loaded)
    }

    @Test
    fun saveUserPreferences_preferredLanguageAr_persistsAcrossReload() = runTest(testDispatcher) {
        val repository1 = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        repository1.saveUserPreferences(UserPreferences(preferredLanguage = "ar"))
        advanceUntilIdle()

        val repository2 = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val loaded = repository2.getUserPreferences().first()
        assertEquals("ar", loaded.preferredLanguage)
    }

    @Test
    fun saveUserPreferences_darkMode_persistsAcrossReload() = runTest(testDispatcher) {
        val repository1 = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        repository1.saveUserPreferences(UserPreferences(isDarkMode = true))
        advanceUntilIdle()

        val repository2 = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val loaded = repository2.getUserPreferences().first()
        assertEquals(true, loaded.isDarkMode)
    }

    @Test
    fun initialization_emptyStorageString_emitsEmptySet() = runTest(testDispatcher) {
        fakeStorage.putString("pref_bookmarked_journeys", "")

        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        val bookmarks = repository.getBookmarkedJourneyIds().first()
        assertTrue(bookmarks.isEmpty())
    }

    @Test
    fun getBookmarkedJourneyIds_emitsReactively_afterToggle() = runTest(testDispatcher) {
        val repository = UserPreferencesRepositoryImpl(
            storage = fakeStorage,
            ioDispatcher = testDispatcher
        )
        advanceUntilIdle()

        assertEquals(emptySet(), repository.getBookmarkedJourneyIds().first())

        repository.toggleJourneyBookmark("journey-xyz")
        advanceUntilIdle()

        assertEquals(setOf("journey-xyz"), repository.getBookmarkedJourneyIds().first())
    }

    private class TestKeyValueStorage : KeyValueStorage {
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
}
