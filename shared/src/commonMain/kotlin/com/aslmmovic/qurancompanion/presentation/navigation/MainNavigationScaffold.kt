package com.aslmmovic.qurancompanion.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeScreen
import com.aslmmovic.qurancompanion.presentation.screens.placeholder.ExplorePlaceholderScreen
import com.aslmmovic.qurancompanion.presentation.screens.placeholder.HabitsPlaceholderScreen
import com.aslmmovic.qurancompanion.presentation.screens.placeholder.LibraryPlaceholderScreen
import com.aslmmovic.qurancompanion.ui.components.CustomBottomNavigation

@Composable
fun MainNavigationScaffold(
    onNavigateToJourneyFlow: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMapExplorer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            val selectedTab = MainTab.entries.firstOrNull { tab ->
                currentDestination?.hierarchy?.any { it.route == tab.route } == true
            } ?: MainTab.Today

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CustomBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (currentDestination?.route != tab.route) {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = MainTab.startTab.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(MainTab.Today.route) {
                HomeScreen(
                    onNavigateToJourneyFlow = onNavigateToJourneyFlow,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
            composable(MainTab.Library.route) {
                LibraryPlaceholderScreen()
            }
            composable(MainTab.Habits.route) {
                HabitsPlaceholderScreen()
            }
            composable(MainTab.Explore.route) {
                ExplorePlaceholderScreen(
                    onNavigateToMap = onNavigateToMapExplorer
                )
            }
        }
    }
}
