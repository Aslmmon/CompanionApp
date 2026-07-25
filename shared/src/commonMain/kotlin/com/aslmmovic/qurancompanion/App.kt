package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aslmmovic.qurancompanion.presentation.navigation.NavigationManager
import com.aslmmovic.qurancompanion.presentation.navigation.Screen
import com.aslmmovic.qurancompanion.presentation.screens.CompletionScreen
import com.aslmmovic.qurancompanion.presentation.screens.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.JourneyFlowScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    QuranCompanionTheme {
        val navigationManager: NavigationManager = koinInject()
        val currentScreen by navigationManager.currentScreen.collectAsState()

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    val vm: HomeViewModel = koinViewModel()
                    HomeScreen(vm)
                }
                Screen.JourneyFlow -> {
                    val vm: JourneyViewModel = koinViewModel()
                    JourneyFlowScreen(vm)
                }
                Screen.Completion -> {
                    val vm: JourneyViewModel = koinViewModel()
                    CompletionScreen(vm)
                }
            }
        }
    }
}