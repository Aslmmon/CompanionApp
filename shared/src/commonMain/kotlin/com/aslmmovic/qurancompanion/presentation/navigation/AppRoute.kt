package com.aslmmovic.qurancompanion.presentation.navigation

/**
 * Type-safe route definitions for the app's navigation graph.
 */
sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Welcome : AppRoute("welcome")
    data object Main : AppRoute("main")
    data object Settings : AppRoute("settings")
    data object MapExplorer : AppRoute("map_explorer")

    data object JourneyFlow : AppRoute("journey_flow") {
        const val ARG_JOURNEY_ID = "journeyId"
        const val ROUTE_PATTERN = "journey_flow?journeyId={journeyId}"

        operator fun invoke(journeyId: String? = null): String = createRoute(journeyId)

        fun createRoute(journeyId: String? = null): String =
            if (journeyId != null) "journey_flow?journeyId=$journeyId" else route
    }

    data object Completion : AppRoute("completion") {
        const val ARG_JOURNEY_ID = "journeyId"
        const val ROUTE_PATTERN = "completion?journeyId={journeyId}"

        operator fun invoke(journeyId: String? = null): String = createRoute(journeyId)

        fun createRoute(journeyId: String? = null): String =
            if (journeyId != null) "completion?journeyId=$journeyId" else route
    }

    companion object {
        val Home: AppRoute = Main
    }
}
