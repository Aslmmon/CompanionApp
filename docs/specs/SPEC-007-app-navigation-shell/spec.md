# SPEC-007: App Navigation Shell (Persistent 4-Tab Scaffold)

## Status
- [x] Draft
- [x] In Review
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Implements the persistent bottom navigation scaffold containing 4 primary tabs (`Today`, `Library`, `Habits`, `Explore`) with state and backstack preservation, while maintaining top-level full-screen flow isolation for the journey reader (`JourneyFlow`, `Completion`), settings, and map exploration.
- **User Story**: *As a user exploring the Sahaba Companions app, I want a persistent, responsive bottom navigation bar so that I can smoothly switch between daily journeys, the companion library, sunnah habits, and historical sites without losing my navigation state or scroll position.*
- **Success Metrics**: Zero tab switching latency, reliable backstack preservation, clean isolation of full-screen destinations, and full bilingual English/Arabic localization.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
- `MainTab` enum defining 4 core tabs with routes, localized titles, and vector icons.
- `AppRoute` sealed class hierarchy with top-level destinations (`Splash`, `Welcome`, `Main`, `Settings`, `MapExplorer`, `JourneyFlow`, `Completion`), retaining `Home` alias.
- `MainNavigationScaffold` implementing Material 3 `Scaffold` and `NavigationBar` with `launchSingleTop` and `restoreState` configuration.
- Screen placeholders (`Library`, `Habits`, `Explore`, `MapExplorer`) with Material 3 Sahaba Modern tokens.
- Updating `App.kt` to route `AppRoute.Main` to the navigation scaffold and mount full-screen routes.
- Updating `AppViewModel` to resolve `AppRoute.Main.route` as `startDestination` when language is set.
- XML vector drawables: `ic_tab_today.xml`, `ic_tab_library.xml`, `ic_tab_habits.xml`, `ic_tab_explore.xml`.
- XML string resources in English (`values/strings.xml`) and Arabic (`values-ar/strings.xml`).
- Automated TDD unit test suite `NavigationShellTest` in `commonTest`.

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> The implementation MUST NOT include or introduce any of the following:
- Domain repository or data-layer modifications (deferred to Phase 4).
- Third-party bottom navigation libraries (standard Material 3 Compose Multiplatform NavigationBar only).
- Android SDK platform dependencies in presentation navigation components.
- Bypassing Use Cases or ViewModels.

---

## 3. Navigation & Presentation Contracts

### 3.1 `MainTab` Contract (`presentation/navigation/MainTab.kt`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.navigation

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_tab_explore
import qurancompanion.shared.generated.resources.ic_tab_habits
import qurancompanion.shared.generated.resources.ic_tab_library
import qurancompanion.shared.generated.resources.ic_tab_today
import qurancompanion.shared.generated.resources.tab_explore
import qurancompanion.shared.generated.resources.tab_habits
import qurancompanion.shared.generated.resources.tab_library
import qurancompanion.shared.generated.resources.tab_today

enum class MainTab(
    val route: String,
    val titleRes: StringResource,
    val iconRes: DrawableResource
) {
    Today("main/today", Res.string.tab_today, Res.drawable.ic_tab_today),
    Library("main/library", Res.string.tab_library, Res.drawable.ic_tab_library),
    Habits("main/habits", Res.string.tab_habits, Res.drawable.ic_tab_habits),
    Explore("main/explore", Res.string.tab_explore, Res.drawable.ic_tab_explore);

    companion object {
        val startTab: MainTab = Today
    }
}
```

### 3.2 `AppRoute` Contract (`presentation/navigation/AppRoute.kt`)
```kotlin
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
```

### 3.3 Scaffold & Navigation Host Contract (`presentation/navigation/MainNavigationScaffold.kt`)
- Encapsulates nested `NavHost` driven by `tabNavController`.
- Uses `launchSingleTop = true` and `restoreState = true` with `popUpTo(graph.findStartDestination().id) { saveState = true }`.
- Bubbles fullscreen events (`onNavigateToJourneyFlow`, `onNavigateToSettings`, `onNavigateToMapExplorer`) up to root `App.kt`.

---

## 4. Acceptance Criteria & Test Matrix (TDD Single Source of Truth)

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected State / Effect (Then) | Target Layer | Automated Test Function |
|:---|:---|:---|:---|:---|:---|
| **AC-01** | `MainTab` enum entries | Queried for routes, titles, and icons | 4 distinct tabs exist (Today, Library, Habits, Explore) with unique routes and valid StringResource/DrawableResource | `presentation/navigation/` | `test_AC01_mainTab_routesAndTitlesAreUniqueAndMapped()` |
| **AC-02** | `AppRoute` sealed class | Inspected | Top-level routes (`Splash`, `Welcome`, `Main`, `Settings`, `MapExplorer`, `JourneyFlow`, `Completion`) are defined with valid paths | `presentation/navigation/` | `test_AC02_appRoute_hierarchyAndStartDestination()` |
| **AC-03** | `AppViewModel` initialized | User preferences have language set | `startDestination` resolves to `AppRoute.Main.route` (or `AppRoute.Welcome.route` if language unset) | `presentation/viewmodel/` | `test_AC03_appViewModel_resolvesMainAsStartDestinationWhenLanguageSet()` |
| **AC-04** | Full-screen destinations (`JourneyFlow`, `Completion`, `Settings`, `MapExplorer`) | Evaluated against `MainTab` routes | None are in `MainTab.entries`, ensuring bottom bar is isolated from full-screen flows | `presentation/navigation/` | `test_AC04_fullScreenDestinations_hideBottomBar()` |
| **AC-05** | Tab navigation destination routes | Checked for single-top navigation and state preservation configuration | Tab routes map to "main/today", "main/library", "main/habits", "main/explore" | `presentation/navigation/` | `test_AC05_tabRouteMapping_andStatePreservation()` |

---

## 5. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `tab_today` | "Today" | "اليوم" |
| `tab_library` | "Library" | "المكتبة" |
| `tab_habits` | "Habits" | "السنن" |
| `tab_explore` | "Explore" | "استكشف" |
| `map_explorer_title` | "Historical Sites Map" | "خريطة المعالم التاريخية" |

---

## 6. Atomic Implementation Checklist

- [x] **Phase 1: Test Definition (RED)**
  - [x] Create `NavigationShellTest` covering AC-01 to AC-05
  - [x] Confirm compilation failure on missing routes/tabs
- [x] **Phase 2: Resources & Assets (GREEN)**
  - [x] Add XML vector drawables `ic_tab_today.xml`, `ic_tab_library.xml`, `ic_tab_habits.xml`, `ic_tab_explore.xml`
  - [x] Add bilingual string resources in English and Arabic
- [x] **Phase 3: Navigation Architecture (GREEN)**
  - [x] Implement `MainTab` enum
  - [x] Expand `AppRoute` with `Main`, `MapExplorer`, `Home` alias, and optional `journeyId` handling
  - [x] Implement placeholder composables in `Placeholders.kt`
  - [x] Implement `MainNavigationScaffold` with Material 3 `NavigationBar` and state preservation
  - [x] Wire `AppRoute.Main` and `AppRoute.MapExplorer` in `App.kt`
  - [x] Update `AppViewModel` startDestination resolution logic
- [x] **Phase 4: Automated Verification & Audit (GREEN & REFACTOR)**
  - [x] Verify `NavigationShellTest` passes 100%
  - [x] Run full suite `shared:testAndroidHostTest` with 0 regressions
