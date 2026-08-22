# SPEC-002: HomeViewModel & State Pipeline Refactoring

## Status
- [ ] Draft
- [ ] In Review (Spec-Driven Development)
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Refactor `HomeViewModel` to eliminate constructor parameter explosion (reducing from 13 down to essential focused use cases), resolve race conditions in `init` caused by parallel uncoordinated flow collections, and unify 5 separate `StateFlow`s into a single reactive `@Immutable HomeUiState`.
- **User Story**: *As a developer and user, I want a reactive, performant, race-condition-free Home architecture that loads state deterministically without redundant calculations.*
- **Success Metrics**:
  - Constructor parameter count reduced by over 50%.
  - Zero duplicate initial calculations or duplicate alarm scheduling in `init`.
  - Single `@Immutable HomeUiState` driving the Compose UI cleanly.
  - 100% existing test pass rate with enhanced test coverage.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope
- Unify UI state into `@Immutable data class HomeUiState`.
- Combine reactive streams (`userPreferences`, `debugDayOffset`, `weeklyProgress`) using standard Kotlin Flow operators (`combine`, `flatMapLatest`, `stateIn`).
- Encapsulate preference mutations into a cleaner helper or structured use case.
- Update `HomeScreen` and `HomeContent` to consume `HomeUiState` (while preserving backward compatibility or clean hoisting).
- Update Koin DI bindings in `AppModule.kt` and all unit test suites in `ViewModelsTest.kt`.

### Out-of-Scope / Non-Goals
> [!IMPORTANT]
> - Do NOT change domain entities (`Journey`, `Step`, `UserPreferences`).
> - Do NOT alter the visual design or UI styling of `HomeContent` or `SettingsBottomSheet`.
> - Do NOT introduce third-party MVI libraries; maintain pure Compose Multiplatform + Kotlin Coroutines architecture.

---

## 3. Architecture & Contracts

### 3.1 Unified Presentation State Contract (`presentation/viewmodel/HomeUiState.kt`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.viewmodel

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

@Immutable
data class HomeUiState(
    val journey: Journey? = null,
    val tomorrowJourney: Journey? = null,
    val isCompleted: Boolean = false,
    val weeklyProgress: List<Boolean> = List(7) { false },
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)
```

### 3.2 Refactored ViewModel Contract
```kotlin
class HomeViewModel(
    private val getTodayJourneyUseCase: GetTodayJourneyUseCase,
    private val getTomorrowJourneyUseCase: GetTomorrowJourneyUseCase,
    private val getWeeklyProgressUseCase: GetWeeklyProgressUseCase,
    private val isJourneyCompletedUseCase: IsJourneyCompletedUseCase,
    private val resetJourneyUseCase: ResetJourneyUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
    private val getDebugDayOffsetUseCase: GetDebugDayOffsetUseCase,
    private val incrementDebugDayOffsetUseCase: IncrementDebugDayOffsetUseCase,
    private val dateTimeProvider: DateTimeProvider,
    private val localeProvider: LocaleProvider,
    private val scheduleDailyReminderUseCase: ScheduleDailyReminderUseCase,
    private val requestNotificationPermissionUseCase: RequestNotificationPermissionUseCase
) : ViewModel() {

    // Single unified reactive state flow
    val uiState: StateFlow<HomeUiState>
    val uiEvents: SharedFlow<HomeUiEvent>

    fun onBeginJourneyClick()
    fun onResetCompletionClick()
    fun onNextJourneyClick()
    fun onLanguageSelected(languageCode: String)
    fun onToggleTheme(isDarkMode: Boolean)
    fun onToggleReminder(isEnabled: Boolean)
    fun onUpdateReminderTime(hour: Int, minute: Int)
}
```

---

## 4. Acceptance Criteria & Test Matrix

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected Result / State (Then) | Target Layer |
|:---|:---|:---|:---|:---|
| **AC-01** | Initial app launch | `HomeViewModel` created | Single reactive initialization; `uiState` reflects today's journey, tomorrow's preview, progress, and preferences with no race conditions | Presentation |
| **AC-02** | User toggles reminder | `onToggleReminder(false)` called | `uiState.userPreferences.isReminderEnabled == false`, alarm cancelled | Presentation |
| **AC-03** | User changes language | `onLanguageSelected("ar")` called | `localeProvider.changeLocale("ar")` called, preference saved | Presentation |
| **AC-04** | User completes journey and clicks reset | `onResetCompletionClick()` called | `isCompleted` updates to `false` in `uiState` | Presentation |
| **AC-05** | Developer clicks simulate next day | `onNextJourneyClick()` called | `debugDayOffset` increments, `uiState` updates with next day's journey reactively | Presentation |

---

## 5. Atomic Task Checklist (`tasks.md`)

- [ ] **Step 1**: Create `HomeUiState` definition in `presentation/viewmodel/HomeUiState.kt`.
- [ ] **Step 2**: Refactor `HomeViewModel.kt` to construct `uiState` via reactive `combine` / `flatMapLatest`, eliminating duplicate launch jobs in `init`.
- [ ] **Step 3**: Update `HomeScreen.kt` to collect `uiState` and pass fields down to `HomeContent.kt`.
- [ ] **Step 4**: Update unit tests in `ViewModelsTest.kt` to assert on `uiState` and verify all acceptance scenarios.
- [ ] **Step 5**: Run `./gradlew :shared:testAndroidHostTest` and `./gradlew :androidApp:assembleDebug`.
