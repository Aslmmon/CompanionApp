package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import com.aslmmovic.qurancompanion.presentation.screens.CompletionScreen
import com.aslmmovic.qurancompanion.presentation.screens.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.JourneyFlowScreen
import com.aslmmovic.qurancompanion.presentation.screens.LanguageSelectionScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.AppViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.LanguageUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.LanguageViewModel
import com.aslmmovic.qurancompanion.presentation.screens.SplashScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.SplashUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.SplashViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel: AppViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.isInitialized) return

    val isDarkMode = when (state.isDarkMode) {
        true -> true
        false -> false
        null -> isSystemInDarkTheme()
    }

    QuranCompanionTheme(
        darkTheme = isDarkMode,
        themeName = state.todayJourney?.theme,
        isArabic = state.isArabic
    ) {
        val navController = rememberNavController()

        state.startDestination?.let { targetDest ->
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
                        val vm: SplashViewModel = koinViewModel()

                        LaunchedEffect(vm) {
                            vm.uiEvents.collect { event ->
                                when (event) {
                                    SplashUiEvent.NavigateNext ->
                                        navController.navigate(targetDest) {
                                            popUpTo(AppRoute.Splash.route) { inclusive = true }
                                        }
                                }
                            }
                        }

                        SplashScreen(vm)
                    }

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