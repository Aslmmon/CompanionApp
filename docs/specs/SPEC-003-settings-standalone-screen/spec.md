# SPEC-003: Settings Screen Redesign & 5-Tab Navigation Integration

## Status
- [x] Draft
- [x] In Review
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Modernizes the Settings Screen according to Figma node `3122-3` and elevates Settings to become the 5th tab in the persistent bottom navigation scaffold (`Today`, `Library`, `Habits`, `Explore`, `Settings`).
- **User Story**: *As a user of the Sahaba Companions app, I want an easily accessible Settings screen in my bottom navigation bar and home header so that I can personalize my appearance (Dark Mode, Language) and daily habit reminder notifications in a polished, Mushaf-inspired interface.*
- **Figma Design Reference**: [Settings Screen Frame (node-id=3122:3)](https://www.figma.com/design/I5omR3EiSE0hmkPWwlwnUT/Sahaba-companion?node-id=3122-3)

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
1. **5-Tab Navigation Shell**:
   - Add `Settings` entry to `MainTab` (`main/settings`, localized title `tab_settings`, icon `ic_tab_settings`).
   - Update `CustomBottomNavigation` and `MainNavigationScaffold` to host 5 tabs cleanly.
   - Route `HomeScreen` top-right avatar click directly to `MainTab.Settings`.
2. **Settings Screen Layout (Figma Node `3122:3`)**:
   - **Header**:
   - **Appearance Section**:
     - Dark Mode row with moon icon, title, subtitle ("Warm papyrus parchment default"), and Switch toggle.
     - App Language row with language icon, title, current language pill ("English (US)" / "العربية"), and chevron `>`; opens Material 3 Modal Bottom Sheet for language selection.
   - **Notifications Section**:
     - Daily Habit Reminder row with clock icon, title, subtitle ("Duha & Adhkar prompt"), time pill (e.g. "08:00 AM"), and chevron `>`; opens Material 3 TimePicker dialog and ensures push notifications are enabled upon confirmation.
     - Push Notifications row with bell icon, title, and emerald Switch toggle.
   - **App Info Footer**: "SAHABA COMPANIONS" and version text.
   - **Developer Options**: Preserved at the very bottom below App Info for testing notifications & day simulation.
3. **Resources & Assets**:
   - Vector drawables: `ic_tab_settings.xml`, `ic_clock.xml`, `ic_notification.xml`, `ic_language.xml`, `ic_chevron_right.xml`, `ic_moon.xml`.
   - String resources localized in both `values/strings.xml` and `values-ar/strings.xml`.
4. **Unit Tests**:
   - Update `NavigationShellTest` in `commonTest` for 5-tab assertions.
   - Update `SettingsViewModelTest` in `commonTest` for state mutations and effects.

### Out-of-Scope / Non-Goals (Explicit Exclusions)
- Account, Profile details, Privacy & Security, Cloud Journey Backup, and Log Out buttons (explicitly omitted as confirmed during /grill-me).
- Modifying underlying database schema or adding authentication SDKs.
- Introducing mock frameworks (Fakes only).

---

## 3. Architecture & Contracts

### 3.1 `MainTab` Contract (`presentation/navigation/MainTab.kt`)
```kotlin
enum class MainTab(
    val route: String,
    val titleRes: StringResource,
    val iconRes: DrawableResource
) {
    Today("main/today", Res.string.tab_today, Res.drawable.ic_tab_today),
    Library("main/library", Res.string.tab_library, Res.drawable.ic_tab_library),
    Habits("main/habits", Res.string.tab_habits, Res.drawable.ic_tab_habits),
    Explore("main/explore", Res.string.tab_explore, Res.drawable.ic_tab_explore),
    Settings("main/settings", Res.string.tab_settings, Res.drawable.ic_tab_settings);

    companion object {
        val startTab: MainTab = Today
    }
}
```

### 3.2 Acceptance Criteria (AC Matrix)
| ID | Given | When | Then | Layer | Test Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **AC-01** | `MainTab.entries` | Queried for count and uniqueness | Exactly 5 distinct tabs exist with unique routes and resources | `navigation` | `test_AC01_mainTab_fiveTabsExistAndUnique()` |
| **AC-02** | `SettingsViewModel` | `onToggleTheme(true)` called | `UserPreferences.isDarkMode` is persisted as `true` | `presentation/settings` | `test_AC02_settingsViewModel_toggleTheme()` |
| **AC-03** | `SettingsViewModel` | `onLanguageSelected("ar")` called | Preferences updated and `localeProvider.changeLocale("ar")` invoked | `presentation/settings` | `test_AC03_settingsViewModel_languageSelected()` |
| **AC-04** | `SettingsViewModel` | `onUpdateReminderTime(9, 30)` called | Hour/minute updated and daily reminder rescheduled | `presentation/settings` | `test_AC04_settingsViewModel_updateReminderTime()` |
| **AC-05** | `SettingsViewModel` | `onToggleReminder(true)` called | Notification permission requested and reminder scheduled | `presentation/settings` | `test_AC05_settingsViewModel_toggleReminder()` |
