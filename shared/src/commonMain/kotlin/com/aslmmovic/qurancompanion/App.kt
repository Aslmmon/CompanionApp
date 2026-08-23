package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.journey.CompletionScreen
import com.aslmmovic.qurancompanion.presentation.screens.journey.JourneyFlowScreen
import com.aslmmovic.qurancompanion.presentation.screens.language.LanguageSelectionScreen
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsScreen
import com.aslmmovic.qurancompanion.presentation.screens.splash.SplashScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.AppViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel: AppViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.isInitialized) return

    val isDarkMode = when (state.isDarkMode) {
        true  -> true
        false -> false
        null  -> isSystemInDarkTheme()
    }

    QuranCompanionTheme(
        darkTheme = isDarkMode,
        themeName = state.todayJourney?.theme,
        isArabic = state.isArabic
    ) {
        val navController = rememberNavController()

        state.startDestination?.let { startDest ->
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .safeContentPadding()
                    .fillMaxSize()
            ) {
                com.aslmmovic.qurancompanion.ui.components.IslamicBackgroundLattice()

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Splash.route
                ) {
                    composable(AppRoute.Splash.route) {
                        SplashScreen(
                            onNavigateNext = {
                                navController.navigate(startDest) {
                                    popUpTo(AppRoute.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoute.Welcome.route) {
                        LanguageSelectionScreen(
                            onNavigateToHome = {
                                navController.navigate(AppRoute.Home.route) {
                                    popUpTo(AppRoute.Welcome.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoute.Home.route) {
                        HomeScreen(
                            onNavigateToJourneyFlow = {
                                navController.navigate(AppRoute.JourneyFlow.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(AppRoute.Settings.route)
                            }
                        )
                    }

                    composable(AppRoute.JourneyFlow.route) {
                        JourneyFlowScreen(
                            onNavigateToCompletion = {
                                navController.navigate(AppRoute.Completion.route)
                            },
                            onNavigateToHome = {
                                navController.popBackStack(
                                    route = AppRoute.Home.route,
                                    inclusive = false
                                )
                            }
                        )
                    }

                    composable(AppRoute.Completion.route) {
                        CompletionScreen(
                            onNavigateToHome = {
                                navController.popBackStack(
                                    route = AppRoute.Home.route,
                                    inclusive = false
                                )
                            }
                        )
                    }

                    composable(AppRoute.Settings.route) {
                        SettingsScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}