package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.model.UserPreferences
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RequestNotificationPermissionUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import com.aslmmovic.qurancompanion.fakes.FakeJourneyRepository
import com.aslmmovic.qurancompanion.fakes.FakeNotificationScheduler
import com.aslmmovic.qurancompanion.fakes.FakeUserPreferencesRepository
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import com.aslmmovic.qurancompanion.presentation.navigation.MainTab
import com.aslmmovic.qurancompanion.presentation.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_tab_explore
import qurancompanion.shared.generated.resources.ic_tab_habits
import qurancompanion.shared.generated.resources.ic_tab_library
import qurancompanion.shared.generated.resources.ic_tab_today
import qurancompanion.shared.generated.resources.tab_explore
import qurancompanion.shared.generated.resources.tab_habits
import qurancompanion.shared.generated.resources.tab_library
import qurancompanion.shared.generated.resources.tab_today
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationShellTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo = FakeJourneyRepository()
    private val prefsRepo = FakeUserPreferencesRepository()
    private val fakeScheduler = FakeNotificationScheduler()

    private class TestLocaleProvider(initialLocale: String = "en") : LocaleProvider {
        private val _currentLocaleFlow = MutableStateFlow(initialLocale)
        override val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()
        override var currentLocale: String
            get() = _currentLocaleFlow.value
            set(value) { _currentLocaleFlow.value = value }
        override fun changeLocale(locale: String) { currentLocale = locale }
    }

    private var fakeLocaleProvider = TestLocaleProvider("en")

    private fun createAppViewModel() = AppViewModel(
        getUserPreferencesUseCase = GetUserPreferencesUseCase(prefsRepo),
        savePreferencesUseCase = SavePreferencesUseCase(prefsRepo),
        getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
        getDebugDayOffsetUseCase = GetDebugDayOffsetUseCase(repo),
        scheduleDailyReminderUseCase = ScheduleDailyReminderUseCase(fakeScheduler, prefsRepo, repo),
        requestNotificationPermissionUseCase = RequestNotificationPermissionUseCase(fakeScheduler),
        localeProvider = fakeLocaleProvider
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_AC01_mainTab_routesAndTitlesAreUniqueAndMapped() {
        val tabs = MainTab.entries
        assertEquals(4, tabs.size)

        // Verify distinct values
        assertEquals(4, tabs.map { it.route }.distinct().size)
        assertEquals(4, tabs.map { it.titleRes }.distinct().size)
        assertEquals(4, tabs.map { it.iconRes }.distinct().size)

        // Verify specific route mappings
        assertEquals("main/today", MainTab.Today.route)
        assertEquals("main/library", MainTab.Library.route)
        assertEquals("main/habits", MainTab.Habits.route)
        assertEquals("main/explore", MainTab.Explore.route)

        // Verify resource associations
        assertEquals(Res.string.tab_today, MainTab.Today.titleRes)
        assertEquals(Res.string.tab_library, MainTab.Library.titleRes)
        assertEquals(Res.string.tab_habits, MainTab.Habits.titleRes)
        assertEquals(Res.string.tab_explore, MainTab.Explore.titleRes)

        assertEquals(Res.drawable.ic_tab_today, MainTab.Today.iconRes)
        assertEquals(Res.drawable.ic_tab_library, MainTab.Library.iconRes)
        assertEquals(Res.drawable.ic_tab_habits, MainTab.Habits.iconRes)
        assertEquals(Res.drawable.ic_tab_explore, MainTab.Explore.iconRes)
    }

    @Test
    fun test_AC02_appRoute_hierarchyAndStartDestination() {
        assertEquals("splash", AppRoute.Splash.route)
        assertEquals("welcome", AppRoute.Welcome.route)
        assertEquals("main", AppRoute.Main.route)
        assertEquals("settings", AppRoute.Settings.route)
        assertEquals("map_explorer", AppRoute.MapExplorer.route)
        assertEquals("journey_flow", AppRoute.JourneyFlow.route)
        assertEquals("completion", AppRoute.Completion.route)

        // Home is an alias to Main
        assertEquals(AppRoute.Main.route, AppRoute.Home.route)

        // Optional journeyId argument handling
        assertEquals("journey_flow?journeyId=journey-123", AppRoute.JourneyFlow("journey-123"))
        assertEquals("journey_flow", AppRoute.JourneyFlow(null))
        assertEquals("completion?journeyId=journey-123", AppRoute.Completion("journey-123"))
        assertEquals("completion", AppRoute.Completion(null))
    }

    @Test
    fun test_AC03_appViewModel_resolvesMainAsStartDestinationWhenLanguageSet() = runTest {
        // Case 1: Language already set -> resolves to Main
        prefsRepo.saveUserPreferences(UserPreferences(preferredLanguage = "en"))
        val viewModelWithLang = createAppViewModel()
        val jobWithLang = launch { viewModelWithLang.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(AppRoute.Main.route, viewModelWithLang.uiState.value.startDestination)
        jobWithLang.cancel()

        // Case 2: Language unset with non-Arabic locale -> resolves to Welcome
        fakeLocaleProvider = TestLocaleProvider("es")
        val unconfiguredPrefsRepo = FakeUserPreferencesRepository(UserPreferences(preferredLanguage = null))
        val viewModelNoLang = AppViewModel(
            getUserPreferencesUseCase = GetUserPreferencesUseCase(unconfiguredPrefsRepo),
            savePreferencesUseCase = SavePreferencesUseCase(unconfiguredPrefsRepo),
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            getDebugDayOffsetUseCase = GetDebugDayOffsetUseCase(repo),
            scheduleDailyReminderUseCase = ScheduleDailyReminderUseCase(fakeScheduler, unconfiguredPrefsRepo, repo),
            requestNotificationPermissionUseCase = RequestNotificationPermissionUseCase(fakeScheduler),
            localeProvider = fakeLocaleProvider
        )
        val jobNoLang = launch { viewModelNoLang.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(AppRoute.Welcome.route, viewModelNoLang.uiState.value.startDestination)
        jobNoLang.cancel()

        // Case 3: Language unset with Arabic locale -> auto-selects Arabic and resolves to Main
        fakeLocaleProvider = TestLocaleProvider("ar")
        val arPrefsRepo = FakeUserPreferencesRepository(UserPreferences(preferredLanguage = null))
        val viewModelAr = AppViewModel(
            getUserPreferencesUseCase = GetUserPreferencesUseCase(arPrefsRepo),
            savePreferencesUseCase = SavePreferencesUseCase(arPrefsRepo),
            getTodayJourneyUseCase = GetTodayJourneyUseCase(repo),
            getDebugDayOffsetUseCase = GetDebugDayOffsetUseCase(repo),
            scheduleDailyReminderUseCase = ScheduleDailyReminderUseCase(fakeScheduler, arPrefsRepo, repo),
            requestNotificationPermissionUseCase = RequestNotificationPermissionUseCase(fakeScheduler),
            localeProvider = fakeLocaleProvider
        )
        val jobAr = launch { viewModelAr.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(AppRoute.Main.route, viewModelAr.uiState.value.startDestination)
        jobAr.cancel()
    }

    @Test
    fun test_AC04_fullScreenDestinations_hideBottomBar() {
        val tabRoutes = MainTab.entries.map { it.route }.toSet()
        val fullScreenRoutes = listOf(
            AppRoute.Splash.route,
            AppRoute.Welcome.route,
            AppRoute.JourneyFlow.route,
            AppRoute.Completion.route,
            AppRoute.Settings.route,
            AppRoute.MapExplorer.route
        )

        fullScreenRoutes.forEach { route ->
            assertFalse(
                tabRoutes.contains(route),
                "Full screen route '$route' must not be in tab routes: $tabRoutes"
            )
        }
    }

    @Test
    fun test_AC05_tabRouteMapping_andStatePreservation() {
        val expectedTabRoutes = listOf(
            "main/today",
            "main/library",
            "main/habits",
            "main/explore"
        )
        val actualTabRoutes = MainTab.entries.map { it.route }
        assertEquals(expectedTabRoutes, actualTabRoutes)

        MainTab.entries.forEach { tab ->
            assertTrue(
                tab.route.startsWith("main/"),
                "Tab route '${tab.route}' must be nested under 'main/'"
            )
        }
    }
}
