package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.repository.UserPreferencesRepositoryImpl
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.presentation.navigation.Screen
import com.aslmmovic.qurancompanion.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeKeyValueStorage : KeyValueStorage {
    private val stringPrefs = mutableMapOf<String, String>()
    private val boolPrefs = mutableMapOf<String, Boolean>()
    private val intPrefs = mutableMapOf<String, Int>()

    override fun getString(key: String): String? = stringPrefs[key]
    override fun putString(key: String, value: String) { stringPrefs[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = boolPrefs[key] ?: defaultValue
    override fun putBoolean(key: String, value: Boolean) { boolPrefs[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = intPrefs[key] ?: defaultValue
    override fun putInt(key: String, value: Int) { intPrefs[key] = value }
}

class OnboardingViewModelTest {

    private lateinit var fakeStorage: FakeKeyValueStorage
    private lateinit var repository: UserPreferencesRepositoryImpl
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase
    private lateinit var savePreferencesUseCase: SavePreferencesUseCase
    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeTest
    fun setUp() {
        fakeStorage = FakeKeyValueStorage()
        repository = UserPreferencesRepositoryImpl(fakeStorage)
        getUserPreferencesUseCase = GetUserPreferencesUseCase(repository)
        savePreferencesUseCase = SavePreferencesUseCase(repository)
    }

    private fun createViewModel() = OnboardingViewModel(
        getUserPreferencesUseCase = getUserPreferencesUseCase,
        savePreferencesUseCase = savePreferencesUseCase,
        coroutineScope = testScope
    )

    @Test
    fun testInitialState_defaultPreferences() {
        val viewModel = createViewModel()
        
        assertEquals(Screen.Welcome, viewModel.currentScreen.value)
        assertEquals(8, viewModel.preferencesState.value.reminderHour)
        assertEquals(0, viewModel.preferencesState.value.reminderMinute)
        assertTrue(viewModel.preferencesState.value.isReminderEnabled)
    }

    @Test
    fun testNavigateToSetup() {
        val viewModel = createViewModel()
        
        viewModel.navigateToSetup()
        assertEquals(Screen.Setup, viewModel.currentScreen.value)
    }

    @Test
    fun testUpdateReminderPreferences() {
        val viewModel = createViewModel()
        
        viewModel.updateReminderEnabled(false)
        viewModel.updateReminderTime(21, 30)

        val currentPrefs = viewModel.preferencesState.value
        assertFalse(currentPrefs.isReminderEnabled)
        assertEquals(21, currentPrefs.reminderHour)
        assertEquals(30, currentPrefs.reminderMinute)
    }

    @Test
    fun testCompleteSetup() {
        val viewModel = createViewModel()
        
        viewModel.completeSetup()

        // Screen is now Home
        assertEquals(Screen.Home, viewModel.currentScreen.value)
        assertTrue(viewModel.preferencesState.value.isSetupCompleted)

        // Setup completed flag is persisted
        assertTrue(fakeStorage.getBoolean("pref_setup_completed", false))
    }

    @Test
    fun testResumeOnboarding_whenAlreadyCompleted() {
        // Setup initial storage state before repository is created
        fakeStorage.putBoolean("pref_setup_completed", true)
        
        // Re-initialize repository so it picks up the storage changes
        repository = UserPreferencesRepositoryImpl(fakeStorage)
        getUserPreferencesUseCase = GetUserPreferencesUseCase(repository)

        val viewModel = createViewModel()

        // Should load completed state and launch Home directly
        assertEquals(Screen.Home, viewModel.currentScreen.value)
        assertTrue(viewModel.preferencesState.value.isSetupCompleted)
    }
}
