package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aslmmovic.qurancompanion.presentation.navigation.AppRoute
import com.aslmmovic.qurancompanion.presentation.screens.CompletionScreen
import com.aslmmovic.qurancompanion.presentation.screens.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.JourneyFlowScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyUiEvent
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    QuranCompanionTheme {
        val navController = rememberNavController()

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.Home.route
            ) {
                composable(AppRoute.Home.route) {
                    val vm: HomeViewModel = koinViewModel()

                    // Navigation coordinator — translates VM events to NavController calls
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