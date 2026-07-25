package com.aslmmovic.qurancompanion.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * NavigationManager handles screen navigation for the app.
 * It exposes the current [Screen] as a read‑only [StateFlow] and provides a
 * simple method to navigate to another screen.
 */
class NavigationManager {
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    /** Navigate to the given screen. */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
}
