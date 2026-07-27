package com.aslmmovic.qurancompanion.presentation.navigation

/**
 * Type-safe route definitions for the app's navigation graph.
 * Using a sealed class rather than a bare enum allows routes to carry
 * typed arguments if needed in the future.
 */
sealed class AppRoute(val route: String) {
    data object Welcome : AppRoute("welcome")
    data object Home : AppRoute("home")
    data object JourneyFlow : AppRoute("journey_flow")
    data object Completion : AppRoute("completion")
}
