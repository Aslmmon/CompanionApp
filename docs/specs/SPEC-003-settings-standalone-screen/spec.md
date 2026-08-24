# SPEC-003: Settings Standalone Screen & Route Refactoring

## Status
- [ ] Draft
- [ ] In Review
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Refactor the temporary `SettingsBottomSheet` modal into a dedicated, standalone screen destination (`presentation/screens/settings/SettingsScreen.kt`) with its own route (`AppRoute.Settings`), isolated `SettingsViewModel`, `@Immutable SettingsUiState`, `SettingsUiEffect`, and modularized sub-components.
- **User Story**: *As a user, I want a dedicated Settings screen where I can configure daily reminder times, change language, toggle theme, and access debug tools with standard screen navigation and back navigation.*
- **Success Metrics**:
  - Full separation of Settings logic from `HomeViewModel` and `HomeScreen`.
  - Zero modal bottom sheet clutter on the Home screen.
  - Sub-composables decomposed into single-responsibility files under 60–80 lines.
  - 100% test pass rate with complete unit test coverage for `SettingsViewModel`.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
- Define `AppRoute.Settings` in `presentation/navigation/AppRoute.kt`.
- Create `presentation/screens/settings/` containing:
  - `SettingsUiState.kt`
  - `SettingsUiEffect.kt`
  - `SettingsViewModel.kt`
  - `SettingsScreen.kt`
  - `SettingsContent.kt`
  - `components/SettingsTopBar.kt`
  - `components/ReminderSettingsSection.kt`
  - `components/TimeOptionRow.kt`
  - `components/LanguageSettingsSection.kt`
  - `components/ThemeSettingsSection.kt`
  - `components/DebugSettingsSection.kt`
- Delete `presentation/screens/home/components/SettingsBottomSheet.kt`.
- Refactor `HomeScreen`, `HomeContent`, and `HomeViewModel` to trigger `NavigateToSettings` on menu click.
- Register `SettingsViewModel` in `AppModule.kt`.
- Create `SettingsViewModelTest.kt` in `commonTest`.

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> - Do NOT modify existing domain models (`Journey`, `UserPreferences`, etc.).
> - Do NOT alter core theme styles or business logic of reminder scheduling.
> - Do NOT add third-party MVI or routing libraries; stay strictly within Compose Multiplatform + Navigation Compose.

---

## 3. Domain Contracts & Pure Business Logic
Settings utilizes existing domain use cases:
- `GetUserPreferencesUseCase` (`domain/usecase/OnboardingUseCases.kt`)
- `SavePreferencesUseCase` (`domain/usecase/OnboardingUseCases.kt`)
- `ScheduleDailyReminderUseCase` (`domain/usecase/NotificationUseCases.kt`)
- `RequestNotificationPermissionUseCase` (`domain/usecase/NotificationUseCases.kt`)
- `IncrementDebugDayOffsetUseCase` (`domain/usecase/JourneyUseCases.kt`)
- `LocaleProvider` (`data/datasource/LocaleProvider.kt`)

---

## 4. Presentation & State Machine Contract

### 4.1 UI State & Effects (`presentation/screens/settings/`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.screens.settings

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.UserPreferences

@Immutable
data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)

sealed interface SettingsUiEffect {
    data object NavigateBack : SettingsUiEffect
}
```

### 4.2 ViewModel Contract (`presentation/screens/settings/SettingsViewModel.kt`)
- `val uiState: StateFlow<SettingsUiState>`
- `val uiEffects: SharedFlow<SettingsUiEffect>`
- `fun onToggleReminder(isEnabled: Boolean)`
- `fun onUpdateReminderTime(hour: Int, minute: Int)`
- `fun onLanguageSelected(languageCode: String)`
- `fun onToggleTheme(isDarkMode: Boolean)`
- `fun onSimulateNextDay()`
- `fun onBackClick()`

---

## 5. Acceptance Criteria & Test Matrix

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected State / Effect (Then) | Target Layer |
|:---|:---|:---|:---|:---|
| **AC-01** | `SettingsViewModel` initialized | UI collects `uiState` | `uiState` reflects preferences from repository with `isLoading = false` | Presentation |
| **AC-02** | Notifications enabled | `onToggleReminder(false)` called | Preferences updated to `isReminderEnabled = false`, reminder canceled | Presentation |
| **AC-03** | Reminder time change | `onUpdateReminderTime(20, 0)` called | Preferences updated to 20:00, reminder rescheduled | Presentation |
| **AC-04** | Language selected | `onLanguageSelected("ar")` called | Preferences saved with `preferredLanguage = "ar"`, `LocaleProvider.changeLocale("ar")` called | Presentation |
| **AC-05** | Theme toggled | `onToggleTheme(true)` called | Preferences updated with `isDarkMode = true` | Presentation |
| **AC-06** | Simulate next day | `onSimulateNextDay()` called | `IncrementDebugDayOffsetUseCase` invoked | Presentation |
| **AC-07** | Back button clicked | `onBackClick()` called | Emits `SettingsUiEffect.NavigateBack` | Presentation |

---

## 6. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `action_back` | "Back" | "رجوع" |
| `settings_title` | "Settings" | "الإعدادات" |
| `settings_daily_reminders` | "Daily Reminder" | "التذكير اليومي" |
| `settings_reminder_time` | "Reminder Time" | "وقت التذكير" |
| `settings_change_language` | "Change Language" | "تغيير اللغة" |
| `settings_debug_options` | "Developer Options" | "خيارات المطور" |
| `settings_simulate_next_day` | "Simulate Next Day" | "محاكاة اليوم التالي" |
