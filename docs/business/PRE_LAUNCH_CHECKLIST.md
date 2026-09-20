# Pre-Launch Technical & Quality Checklist
**Application ID:** `com.aslmmovic.qurancompanion`  
**Target Release:** Production Release Candidate v1.0.0 (Version Code: 1)  
**Target Stores:** Google Play Store (Production Track) & Apple App Store (iOS-ready)  
**Audit Date:** September 2026

---

## 1. Executive Code Audit Summary

A rigorous codebase audit across Kotlin Multiplatform layers (`:shared` and `:androidApp`), data sources, ViewModels, Compose screens, and automated test runners revealed **5 Critical Launch Blockers (Priority 0)** that must be resolved prior to deploying the signed Android App Bundle (AAB) to Google Play Console.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRE-LAUNCH AUDIT STATUS                         │
├──────────────────────────────┬───────────────────┬─────────────────────┤
│ Severity Category            │ Issues Identified │ Blocking Release?   │
├──────────────────────────────┼───────────────────┼─────────────────────┤
│ Priority 0 (Critical Bugs)   │ 5 items           │ 🔴 YES (Release P0) │
│ Priority 1 (Architecture/UX) │ 6 items           │ 🟡 HIGH (P1)        │
│ Priority 2 (Content & Asset) │ 3 items           │ 🟢 MEDIUM (P2)      │
│ Priority 3 (Build & Proguard)│ 3 items           │ 🟢 VERIFIED (P3)    │
└──────────────────────────────┴───────────────────┴─────────────────────┘
```

---

## 2. Priority 0: Critical Launch Blockers (Must Fix Immediately)

### 🔴 Blocker 1: Android Notification 15-Minute Test Interval Bug in Production
* **Affected File:** [`shared/src/androidMain/kotlin/com/aslmmovic/qurancompanion/util/AndroidNotificationScheduler.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/androidMain/kotlin/com/aslmmovic/qurancompanion/util/AndroidNotificationScheduler.kt#L72-L75)
* **Code Issue:**
  ```kotlin
  // Lines 72-75
  override fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String) {
      // Scheduled as 15-minute periodic reminder for testing functionality as requested
      schedulePeriodicReminder(15L, title, body)
  }
  ```
* **Impact:** In production, any user enabling daily reminders will receive push notifications **every 15 minutes**, causing immediate 1-star ratings, uninstalls, and spam reports on Google Play!
* **Required Resolution:**
  1. Remove `schedulePeriodicReminder(15L, ...)`.
  2. Compute initial delay until user's specified `hour` and `minute` using `java.util.Calendar`:
     ```kotlin
     val now = Calendar.getInstance()
     val target = Calendar.getInstance().apply {
         set(Calendar.HOUR_OF_DAY, hour)
         set(Calendar.MINUTE, minute)
         set(Calendar.SECOND, 0)
         set(Calendar.MILLISECOND, 0)
         if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
     }
     val initialDelayMs = target.timeInMillis - now.timeInMillis
     ```
  3. Enqueue a 24-hour `PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)` with `setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)` and `ExistingPeriodicWorkPolicy.UPDATE`.

---

### 🔴 Blocker 2: Kotlin/Native iOS Test Compilation Crash (Illegal Comma in Function Names)
* **Affected File:** [`shared/src/commonTest/kotlin/com/aslmmovic/qurancompanion/JourneyRepositoryImplTest.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonTest/kotlin/com/aslmmovic/qurancompanion/JourneyRepositoryImplTest.kt#L226-L260)
* **Code Issue:**
  - Line 226: ``fun `when locale changes to arabic, cached journeys reload with arabic journeys`()``
  - Line 260: ``fun `when locale is regional arabic variant, it normalizes to ar and loads arabic journeys`()``
* **Impact:** Kotlin/Native (iOS compiler) prohibits commas `,` inside backtick identifier names. Running `./gradlew :shared:allTests` crashes with:
  ```
  > Task :shared:compileTestKotlinIosSimulatorArm64 FAILED
  e: JourneyRepositoryImplTest.kt:226:9 Name contains illegal characters: ",".
  e: JourneyRepositoryImplTest.kt:260:9 Name contains illegal characters: ",".
  ```
* **Required Resolution:**
  Rename the two test methods to remove commas:
  - Line 226: ``fun `when locale changes to arabic then cached journeys reload with arabic journeys`()``
  - Line 260: ``fun `when locale is regional arabic variant then it normalizes to ar and loads arabic journeys`()``

---

### 🔴 Blocker 3: 73.3% Dynamic Palette / Theming Failure
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Theme.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Theme.kt#L38-L194) vs [`files/en/journeys.json`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/composeResources/files/en/journeys.json)
* **Code Issue:**
  In `journeys.json`, themes are defined as:
  - `"Golden"` (10 journeys) ➔ `Theme.kt` only checks `"gold"`.
  - `"Midnight"` (4 journeys) ➔ `Theme.kt` only checks `"night"`.
  - `"Sunrise"` (4 journeys) ➔ Not defined in `Theme.kt`.
  - `"Mushaf"` (4 journeys) ➔ Not defined in `Theme.kt`.
* **Impact:** 22 out of 30 journeys (73.3%) fail theme mapping and silently fall back to the default theme.
* **Required Resolution:**
  Update `getThemeColorScheme` in `Theme.kt` to normalize input and map:
  ```kotlin
  when (themeName.trim().lowercase()) {
      "desert", "dune" -> ...
      "emerald", "andalus" -> ...
      "ocean", "ocean breeze" -> ...
      "night", "midnight", "midnight crescent" -> NightColorScheme
      "gold", "golden", "golden dune" -> GoldColorScheme
      "mushaf", "mushaf parchment" -> MushafColorScheme
      "sunrise" -> SunriseColorScheme
      else -> if (darkTheme) DarkColorScheme else LightColorScheme
  }
  ```

---

### 🔴 Blocker 4: Hardcoded Day Number Badge on Home Screen
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/home/components/ReadyStateContent.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/home/components/ReadyStateContent.kt#L130)
* **Code Issue:**
  ```kotlin
  // Line 129-134
  RubElHizbBadge(
      number = "1",
      badgeSize = 36.dp,
      starColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary
  )
  ```
* **Impact:** The day number inside the Islamic 8-pointed star is hardcoded to `"1"` on every single day! Even when reading Day 15 or Day 30, it displays "1".
* **Required Resolution:**
  Change line 130 to:
  ```kotlin
  RubElHizbBadge(
      number = journey.dayNumber.toString(),
      badgeSize = 36.dp,
      starColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary
  )
  ```

---

### 🔴 Blocker 5: Reminder Settings Section Missing from Settings UI
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/settings/SettingsContent.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/settings/SettingsContent.kt#L50-L53)
* **Code Issue:**
  `ReminderSettingsSection` is imported at line 21, and `onUpdateReminderTime` is accepted in the composable arguments, but between lines 50 and 53, `ReminderSettingsSection` is **never invoked** inside the `Column`!
* **Impact:** Users are completely unable to toggle reminders or choose their preferred notification time (Post-Fajr 6 AM, Morning 8 AM, Evening 8 PM) from the app's Settings!
* **Required Resolution:**
  Insert `ReminderSettingsSection` into `SettingsContent.kt`:
  ```kotlin
  ReminderSettingsSection(
      isReminderEnabled = uiState.userPreferences.isReminderEnabled,
      reminderHour = uiState.userPreferences.reminderHour,
      reminderMinute = uiState.userPreferences.reminderMinute,
      onToggleReminder = { enabled ->
          // wire toggle reminder intent
      },
      onUpdateReminderTime = onUpdateReminderTime
  )
  ```

---

## 3. Priority 1: High Priority Architecture & UX Tasks

### 🟡 Task 6: Decouple Progression from Gregorian Day-of-Year Modulo
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/data/repository/JourneyRepositoryImpl.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/data/repository/JourneyRepositoryImpl.kt#L85)
* **Code Issue:**
  ```kotlin
  val index = (dateTimeProvider.getCurrentDayOfYear() - 1 + offset) % journeys.size
  ```
* **Impact:** A new user installing the app on September 20 starts on Day 24, missing the first 23 foundational companions!
* **Required Resolution:**
  Store user sequential progression (`completed_journey_count` or `unlocked_day_index`) in `KeyValueStorage`. New users start at Day 1 and unlock Day 2 upon completing Day 1.

---

### 🟡 Task 7: Eliminate Clean Architecture Layer Leaks in ViewModels
* **Affected Files:**
  - [`LanguageViewModel.kt#L5`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/language/LanguageViewModel.kt#L5)
  - [`SettingsViewModel.kt#L5`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/settings/SettingsViewModel.kt#L5)
  - [`AppViewModel.kt#L5`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/viewmodel/AppViewModel.kt#L5)
* **Code Issue:** ViewModels directly import `com.aslmmovic.qurancompanion.data.datasource.LocaleProvider`, bypassing domain layer isolation.
* **Required Resolution:**
  Create domain Use Cases (`GetSystemLocaleUseCase`, `SetAppLocaleUseCase`) or access locale through `UserPreferencesRepository`.

---

### 🟡 Task 8: Defer Abrupt Notification Permission Prompt on Cold Start
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/viewmodel/AppViewModel.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/viewmodel/AppViewModel.kt#L36)
* **Code Issue:**
  `requestNotificationPermissionUseCase()` is invoked on the very first frame of app launch in `init`.
* **Impact:** Android 13+ displays an immediate system permission dialog before the user sees any app value, resulting in high denial rates (>60%).
* **Required Resolution:**
  Remove from `AppViewModel.kt init`. Trigger permission only when:
  1. The user finishes Day 1 and reaches `CompletionContent`, OR
  2. The user toggles "Daily Reminder" in Settings.

---

### 🟡 Task 9: Reduce 5-Second Splash Screen Delay
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/splash/SplashViewModel.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/splash/SplashViewModel.kt#L28)
* **Code Issue:** `SPLASH_DELAY_MS = 5000L` forces users to wait 5 seconds on cold launch.
* **Required Resolution:** Reduce `SPLASH_DELAY_MS` to `1200L`–`1500L`, or navigate immediately once `AppViewModel.uiState.isInitialized` is true.

---

### 🟡 Task 10: Localize Push Notification Default Strings
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/usecase/NotificationUseCases.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/usecase/NotificationUseCases.kt#L22-L26)
* **Code Issue:** `"Sahaba Companion"` and `"Discover today's journey with the Sahaba."` are hardcoded English strings. Arabic users receive notifications in English.
* **Required Resolution:** Use localized strings based on user's active language preference (`notification_default_title` and `notification_default_body`).

---

### 🟡 Task 11: Hide Developer Debug Options from Production Build
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/settings/SettingsContent.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/settings/SettingsContent.kt#L63-L66)
* **Code Issue:** `DebugSettingsSection` ("Simulate Next Day", "Trigger Notification Now") is displayed to all end users.
* **Required Resolution:** Wrap `DebugSettingsSection` behind a debug flag (`Platform.isDebug`) or a 7-tap easter egg on the version code.

---

## 4. Priority 2: Content & Asset Integrity Tasks

### 🟢 Task 12: Differentiate Duplicate Salman al-Farsi Titles
* **Affected Files:**
  - [`files/en/journeys.json`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/composeResources/files/en/journeys.json) (Days 15 & 16)
  - [`files/ar/journeys.json`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/composeResources/files/ar/journeys.json) (Days 15 & 16)
* **Issue:** Days 15 and 16 both have the identical title *"The Journey in Search of Truth"*.
* **Required Resolution:**
  - Day 15: *"Salman al-Farsi: The Search for Truth (Part 1 — The Escape)"* / *"سلمان الفارسي: رحلة البحث عن الحق (الجزء الأول — الهرب)"*
  - Day 16: *"Salman al-Farsi: The Search for Truth (Part 2 — The Meeting)"* / *"سلمان الفارسي: رحلة البحث عن الحق (الجزء الثاني — اللقاء)"*

---

### 🟢 Task 13: Clean Up Unused Lottie Assets or Wire into Completion Screen
* **Affected Files:**
  - `shared/src/commonMain/composeResources/drawable/animation_1.json` (176 KB)
  - `shared/src/commonMain/composeResources/drawable/animation_1.lottie` (68 KB)
* **Issue:** 244 KB of animations exist in resources but are not wired into `CompletionContent.kt`.
* **Required Resolution:** Wire celebration animation into `CompletionContent.kt` or remove unreferenced files to minimize APK download size.

---

### 🟢 Task 14: Graceful Fallback for Missing Companion Cover Illustrations
* **Affected File:** [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/model/Journey.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/model/Journey.kt#L23)
* **Issue:** `journeys.json` references 26 illustration assets (`cover.asset`) that do not exist in drawables.
* **Required Resolution:** Ensure UI components fallback gracefully to a golden geometric pattern or the app logo (`ic_app_logo.png`) rather than throwing resource lookup exceptions.

---

## 5. Build, Proguard & Security Release Standards

### 5.1 Proguard & Minification Rules
Verify that [`androidApp/proguard-rules.pro`](file:///Users/macbookpro/Desktop/CompanionApp/androidApp/proguard-rules.pro) contains all mandatory directives:
```proguard
# Mandatory Kotlinx Serialization
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class * { *** Companion; }
-keepattributes *Annotation*, Signature, Exception, InnerClasses, EnclosingMethod

# Mandatory Koin Injection
-keep class org.koin.** { *; }

# Mandatory WorkManager Rules
-keep class * extends androidx.work.ListenableWorker { public <init>(...); }
-keep class * extends androidx.work.Worker { public <init>(...); }
-dontwarn androidx.work.impl.**

# Stacktrace preservation for crash reporting
-keepattributes SourceFile, LineNumberTable
```

### 5.2 Release Build Configuration
Verify [`androidApp/build.gradle.kts`](file:///Users/macbookpro/Desktop/CompanionApp/androidApp/build.gradle.kts#L82-L94):
- `isMinifyEnabled = true` (Active)
- `isShrinkResources = true` (Active)
- `signingConfig = signingConfigs.getByName("release")` (Active with `release-upload.jks`)

---

## 6. Pre-Launch Verification Protocol & Acceptance Matrix

Run the following test inner-loop before producing the release bundle:

```bash
# 1. Execute Android Host Tests
./gradlew :shared:testAndroidHostTest

# 2. Execute Shared Multiplatform Tests (after fixing test name commas)
./gradlew :shared:allTests

# 3. Execute Android App Unit Tests
./gradlew :androidApp:testDebugUnitTest

# 4. Build Signed Release Android App Bundle (AAB)
./gradlew :androidApp:bundleRelease

# 5. Export Release Bundle to Store Directory
./gradlew exportReleaseBundleToStore
```

### 6.1 Sign-Off Checklist Matrix

| Checkpoint | Verification Method | Owner | Status | Gate |
| :--- | :--- | :--- | :---: | :---: |
| **Notification Daily Scheduling** | Verify WorkManager delay matches user time, not 15m | Android Lead | [ ] | **BLOCKER** |
| **iOS Test Comma Fix** | Verify `./gradlew :shared:compileTestKotlinIosSimulatorArm64` passes | KMP Lead | [ ] | **BLOCKER** |
| **Theme Mapping Coverage** | Verify all 30 journeys render expected color scheme | UI Lead | [ ] | **BLOCKER** |
| **Home Badge Day Number** | Verify star badge renders `1`..`30` dynamically | UI Lead | [ ] | **BLOCKER** |
| **Settings Reminder Controls** | Verify toggle and time selection UI in Settings | UI Lead | [ ] | **BLOCKER** |
| **Sequential Progression** | Verify new install starts on Day 1 | Data Lead | [ ] | HIGH |
| **Splash Duration** | Verify cold start splash delay < 1.5s | UI Lead | [ ] | HIGH |
| **Clean Architecture Imports** | Verify no `data.datasource` imports in ViewModels | Arch Lead | [ ] | HIGH |
| **Release AAB Size** | Verify signed AAB is under 15 MB | DevOps | [ ] | PASS |
| **Google Play Data Safety** | Verify no tracking / offline privacy declaration | Compliance | [ ] | PASS |
| **Feature Graphic & Screenshots** | Verify 1024x500 banner & 6 phone mockups in `docs/store/` | Design Lead | [ ] | PASS |
