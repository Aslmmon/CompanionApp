package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import com.aslmmovic.qurancompanion.presentation.screens.CompletionScreen
import com.aslmmovic.qurancompanion.presentation.screens.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.JourneyFlowScreen
import com.aslmmovic.qurancompanion.presentation.screens.LanguageSelectionScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.LanguageUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.LanguageViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val userPreferencesRepository: UserPreferencesRepository = koinInject()
    val localeProvider: LocaleProvider = koinInject()
    val prefsState by userPreferencesRepository.getUserPreferences().collectAsState(null)

    // Handle auto-selecting Arabic if device language is Arabic
    LaunchedEffect(prefsState) {
        val prefs = prefsState ?: return@LaunchedEffect
        if (prefs.preferredLanguage == null) {
            val systemLocale = localeProvider.currentLocale
            if (systemLocale == "ar") {
                userPreferencesRepository.saveUserPreferences(prefs.copy(preferredLanguage = "ar"))
            }
        }
    }

    val isDarkMode = when (prefsState?.isDarkMode) {
        true -> true
        false -> false
        null -> isSystemInDarkTheme()
    }

    QuranCompanionTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()

        val startDestination = remember(prefsState) {
            val prefs = prefsState ?: return@remember null
            if (prefs.preferredLanguage != null) {
                AppRoute.Home.route
            } else {
                val systemLocale = localeProvider.currentLocale
                if (systemLocale == "ar") {
                    AppRoute.Home.route
                } else {
                    AppRoute.Welcome.route
                }
            }
        }

        if (startDestination != null) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .safeContentPadding()
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(AppRoute.Welcome.route) {
                        val vm: LanguageViewModel = koinViewModel()

                        LaunchedEffect(vm) {
                            vm.uiEvents.collect { event ->
                                when (event) {
                                    LanguageUiEvent.NavigateToHome ->
                                        navController.navigate(AppRoute.Home.route) {
                                            popUpTo(AppRoute.Welcome.route) { inclusive = true }
                                        }
                                }
                            }
                        }

                        LanguageSelectionScreen(vm)
                    }

                    composable(AppRoute.Home.route) {
                        val vm: HomeViewModel = koinViewModel()

                        LaunchedEffect(vm) {
                            vm.uiEvents.collect { event ->
                                when (event) {
                                    HomeUiEvent.NavigateToJourneyFlow ->
                                        navController.navigate(AppRoute.JourneyFlow.route)
                                }
                            }
                        }

                        HomeScreen(vm)
                    }

                    composable(AppRoute.JourneyFlow.route) {
                        val vm: JourneyViewModel = koinViewModel()

                        LaunchedEffect(vm) {
                            vm.uiEvents.collect { event ->
                                when (event) {
                                    JourneyUiEvent.NavigateToCompletion ->
                                        navController.navigate(AppRoute.Completion.route)
                                    JourneyUiEvent.NavigateToHome ->
                                        navController.popBackStack(
                                            route = AppRoute.Home.route,
                                            inclusive = false
                                        )
                                }
                            }
                        }

                        JourneyFlowScreen(vm)
                    }

                    composable(AppRoute.Completion.route) {
                        val vm: JourneyViewModel = koinViewModel()

                        LaunchedEffect(vm) {
                            vm.uiEvents.collect { event ->
                                when (event) {
                                    JourneyUiEvent.NavigateToHome ->
                                        navController.popBackStack(
                                            route = AppRoute.Home.route,
                                            inclusive = false
                                        )
                                    JourneyUiEvent.NavigateToCompletion -> { /* already here */ }
                                }
                            }
                        }

                        CompletionScreen(vm)
                    }
                }
            }
        }
    }
}