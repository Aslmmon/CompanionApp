# TASK-001: UI/UX Refactor & Ecosystem Expansion (SDD Plan)

This document is the authoritative execution blueprint for refactoring the **Sahaba Companions** application to match the visual design and feature scope in [`design_ui_ux.pdf`](file:///Users/macbookpro/Desktop/CompanionApp/design_ui_ux.pdf) using **Spec-Driven Development (SDD)** and Clean Architecture.

---

## 1. Execution Principles & Architecture Rules

1. **Strict Clean Architecture**:
   * **Domain Layer**: Pure Kotlin data models, repository interfaces, and use cases. Zero Android SDK or presentation dependencies.
   * **Data Layer**: DTOs, data sources (bundled JSON + `KeyValueStorage`), and repository implementations. Maps entities to domain models before returning.
   * **Presentation Layer**: ViewModels (`StateFlow<UiState>`, `Flow<UiEffect>`), Screen containers (state hoisting), and Content composables (stateless).
2. **Localization First**:
   * All user-facing strings must have corresponding entries in both `values/strings.xml` (English) and `values-ar/strings.xml` (Arabic).
3. **No External Mocking Libraries**:
   * Use clean, in-memory fake classes in `commonTest`. No Mockk or Mockito.
4. **Offline-First Delivery**:
   * Static catalog data (Library Sahaba directory, Explore historical sites & collections) is stored in bundled JSON resources.
   * User dynamic state (habits, streak counts, completion timestamps, bookmarks) is persisted in [`KeyValueStorage`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/data/datasource/KeyValueStorage.kt) serialized via `kotlinx.serialization`.

---

## 2. Master Progress Tracker

- [x] **Phase 1: Design System & Color Tokens**
- [ ] **Phase 2: Fonts & Typography Scaling**
- [ ] **Phase 3: App Navigation Shell (Persistent 4-Tab Scaffold)**
- [ ] **Phase 4: Screen by Screen Implementation (Data + Domain + Presentation)**
  - [ ] **4.1 Splash Screen** (Page 6)
  - [ ] **4.2 Today Screen** (Page 7)
  - [ ] **4.3 Library Tab** (Page 1)
  - [ ] **4.4 Sunnah Habits Tab** (Page 2)
  - [ ] **4.5 Explore Tab & Map POI Viewer** (Page 3)
  - [ ] **4.6 Journey Reader Flow** (Page 4)
  - [ ] **4.7 Journey Complete Screen** (Page 5)
  - [ ] **4.8 Settings Screen** (Page 8)
- [ ] **Phase 5: Architectural Audit & Verification**

---

## 3. Phase Specifications

---

### Phase 1: Design System & Color Tokens

#### Goal
Establish the canonical **"Sahaba Modern"** color scheme and visual theme across light and dark modes, deprecating unused atmospheric palettes to unify the brand.

#### Files Modified
* [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Color.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Color.kt)
* [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Theme.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Theme.kt)

#### Technical Specification
* **Light Palette**:
  * `Primary`: `#1B4332` (Deep Islamic Evergreen)
  * `OnPrimary`: `#FFFFFF`
  * `PrimaryContainer`: `#E8F2EC` (Soft Celadon)
  * `OnPrimaryContainer`: `#0E291E`
  * `Secondary / Accent`: `#D4AF37` (Islamic Warm Metallic Gold)
  * `Background`: `#F9F8F3` (Warm Ivory Papyrus)
  * `OnBackground`: `#1C1E1D` (Dark Sepia Charcoal)
  * `Surface`: `#FFFFFF` (Crisp White Card)
  * `OnSurface`: `#1C1E1D`
  * `SurfaceVariant`: `#F4F2EC` (Card Outline / Chip Background)
  * `Outline`: `#EBE8DF` (Subtle 1dp card border)
  * `TextMuted`: `#6E7772` (Subtitles, metadata)
* **Dark Palette (Midnight Crescent)**:
  * `Primary`: `#E6C265` (Glowing Crescent Gold)
  * `OnPrimary`: `#141003`
  * `PrimaryContainer`: `#26362C` (Deep Pine)
  * `OnPrimaryContainer`: `#E2F1E8`
  * `Secondary`: `#52C490` (Emerald Highlight)
  * `Background`: `#0B1320` (Nocturnal Deep Slate)
  * `OnBackground`: `#F5F7FA`
  * `Surface`: `#131F33` (Midnight Navy Card)
  * `OnSurface`: `#F5F7FA`
  * `Outline`: `#25344D`
* **Card & Component Shapes**:
  * Standard Card: `RoundedCornerShape(16.dp)`
  * Large Card / Hero: `RoundedCornerShape(20.dp)`
  * Filter Chip: `RoundedCornerShape(100.dp)` (Pill)
  * Dialog / Bottom Sheet: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)`

#### Acceptance Criteria
* [ ] Both light and dark schemes reflect the exact hex codes from the design.
* [ ] Unit test `ThemeTest` passes and verifies token mappings.

---

### Phase 2: Fonts & Typography Scaling

#### Goal
Align typography with the elegant serif and sans-serif styling in the PDF, supporting Latin script (`OutfitFontFamily`), Arabic script (`ElMessiriFontFamily`), and custom verse typography (`QuranArabicTextStyle`).

#### Files Modified
* [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Type.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/ui/theme/Type.kt)

#### Technical Specification
* **Headlines & Titles**:
  * `headlineLarge`: `28.sp`, Bold, line height `36.sp`, tracking `0.sp`
  * `headlineMedium`: `22.sp`, SemiBold, line height `28.sp`
  * `titleLarge`: `18.sp`, SemiBold, line height `24.sp`
  * `titleMedium`: `16.sp`, Medium, line height `22.sp`
* **Body Text**:
  * `bodyLarge`: `15.sp`, line height `24.sp`, tracking `0.2.sp` (optimal readability for narratives)
  * `bodyMedium`: `13.sp`, line height `18.sp`, tracking `0.1.sp`
* **Labels & Badges**:
  * `labelMedium`: `11.sp`, Bold, tracking `1.2.sp` (All Caps, e.g., `12 JOURNEYS`, `STEP 2 OF 5`, `CURATED COLLECTIONS`)
  * `labelSmall`: `10.sp`, Medium, tracking `0.5.sp`
* **Arabic Quranic Verse**:
  * `QuranArabicTextStyle`: `24.sp`, line height `46.sp` to comfortably clear Arabic vowel signs (tashkeel).

#### Acceptance Criteria
* [ ] Typography correctly switches between Outfit and El Messiri based on `isArabic`.
* [ ] Line heights prevent Arabic tashkeel clipping.

---

### Phase 3: App Navigation Shell (Persistent 4-Tab Scaffold)

#### Goal
Implement the persistent Bottom Navigation Bar with 4 tabs (`Today`, `Library`, `Habits`, `Explore`), preserving tab backstack and state, with full-screen flows for reader and settings.

#### New Files
* `shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/navigation/MainTab.kt`
* `shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/navigation/MainNavigationScaffold.kt`

#### Files Modified
* [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/navigation/AppRoute.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/navigation/AppRoute.kt)
* [`shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/App.kt`](file:///Users/macbookpro/Desktop/CompanionApp/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/App.kt)

#### Technical Specification
* **`MainTab` Definition**:
  ```kotlin
  enum class MainTab(
      val route: String,
      val titleRes: StringResource,
      val iconRes: DrawableResource
  ) {
      Today("main/today", Res.string.tab_today, Res.drawable.ic_tab_today),
      Library("main/library", Res.string.tab_library, Res.drawable.ic_tab_library),
      Habits("main/habits", Res.string.tab_habits, Res.drawable.ic_tab_habits),
      Explore("main/explore", Res.string.tab_explore, Res.drawable.ic_tab_explore)
  }
  ```
* **State Preservation**:
  ```kotlin
  navController.navigate(tab.route) {
      popUpTo(navController.graph.findStartDestination().id) {
          saveState = true
      }
      launchSingleTop = true
      restoreState = true
  }
  ```
* **Full-Screen Destinations (Outside Bottom Bar)**:
  * `AppRoute.Splash`
  * `AppRoute.Welcome` (Language selection)
  * `AppRoute.JourneyFlow(journeyId)`
  * `AppRoute.Completion(journeyId)`
  * `AppRoute.Settings`
  * `AppRoute.MapExplorer`

#### Acceptance Criteria
* [ ] Switching tabs retains scroll position and filter state.
* [ ] Fullscreen flows (JourneyFlow, Settings) hide the bottom navigation bar.

---

### Phase 4: Screen-by-Screen Implementation

---

#### 4.1 Splash Screen (Page 6)
* **Visual Elements**:
  * Deep green rounded logo emblem with white mosque dome/arch & golden star.
  * English App Title: `Sahaba Companions`
  * Arabic Subtitle: `صحابة رسول الله`
  * Tagline: `JOURNEY INTO EXCELLENCE` (letter-spaced, muted gold/stone)
* **Files Modified**:
  * `SplashContent.kt`, `SplashScreen.kt`
* **Acceptance Criteria**:
  * [ ] Matches Page 6 layout, colors, and branding.

---

#### 4.2 Today Screen (Page 7)
* **Domain Layer**:
  * [NEW] `domain/util/HijriDateFormatter.kt`: Algorithmic Tabular Islamic calendar converter calculating Hijri day, month name in EN/AR, and year AH (e.g. `14 RAMADAN 1446 AH`).
  * [MODIFY] `GetTodayJourneyUseCase.kt`: Returns today's journey augmented with reader social proof (`+2k reading today`) and estimated read duration (`6 min read`).
* **Presentation Layer**:
  * `TodayHeader`: Hijri date, App title, streak badge (`12`), profile avatar leading to Settings.
  * `CuratedJourneyHeroCard`:
    * Category pill (`BIOGRAPHY • 6 min read`).
    * Sahaba name and subtitle quote (`"The one who believes when others doubt."`).
    * Social stack (`+2k`).
    * Primary CTA: `Begin Journey →`.
    * Completed state: Adapts to show completion badge, reflection card with share action, and re-read button.
  * `WeeklyStreakTracker`: 7-day row (Mon–Sun) with completed checkmarks and current day highlighted ring.
* **Acceptance Criteria**:
  * [ ] Hijri date correctly displays for the active date.
  * [ ] Streak counter matches actual consecutive completions.
  * [ ] Hero card adapts smoothly between Ready and Completed states.

---

#### 4.3 Library Tab (Page 1)
* **Domain Layer**:
  * [NEW] `domain/model/Sahaba.kt` & `domain/model/SahabaCategory.kt`:
    ```kotlin
    data class Sahaba(
        val id: String,
        val name: String,
        val arabicName: String,
        val initialLetterArabic: String,
        val epithet: String,
        val category: SahabaCategory,
        val journeyCount: Int,
        val defaultJourneyId: String,
        val bio: String
    )

    enum class SahabaCategory {
        ALL, TEN_PROMISED, MOTHERS_OF_BELIEVERS, ANSAR, MUHAJIRUN
    }
    ```
  * [NEW] `domain/repository/SahabaRepository.kt` & `GetSahabaCatalogUseCase.kt`.
* **Data Layer**:
  * [NEW] `files/en/sahaba.json` and `files/ar/sahaba.json` bundled multiplatform resources.
  * [NEW] `SahabaLocalDataSource.kt` & `SahabaRepositoryImpl.kt`.
* **Presentation Layer**:
  * `LibraryViewModel`: Exposes `uiState: StateFlow<LibraryUiState>` with query search and category filtering.
  * `LibraryScreen` & `LibraryContent`:
    * Search bar (`"Search Sahaba by name or trait..."`).
    * Category filter chips (`All`, `Ten Promised`, `Mothers of Believers`, `Ansar`, `Muhajirun`).
    * 2-column Sahaba Grid: Calligraphy badge (`أ`, `ع`, `خ`, etc.), English name, epithet, and journey count (`12 JOURNEYS`).
    * Direct Launch: Tapping card immediately triggers `NavigateToJourney(defaultJourneyId)`.
* **Acceptance Criteria**:
  * [ ] Filtering by category correctly narrows grid items.
  * [ ] Search filters companions in real-time by name and epithet.
  * [ ] Tapping a card launches the Journey Reader.

---

#### 4.4 Sunnah Habits Tab (Page 2)
* **Domain Layer**:
  * [NEW] `domain/model/SunnahHabit.kt`:
    ```kotlin
    data class SunnahHabit(
        val id: String,
        val title: String,
        val subtitle: String,
        val streakDays: Int,
        val isCompletedToday: Boolean,
        val lastCompletedDate: String? = null,
        val isCustom: Boolean = false
    )
    ```
  * [NEW] `domain/repository/HabitsRepository.kt` & `HabitUseCases.kt` (`GetActiveHabitsUseCase`, `ToggleHabitUseCase`, `AddHabitUseCase`).
* **Data Layer**:
  * [NEW] `HabitsRepositoryImpl.kt`: Stores active habits in `KeyValueStorage` via `kotlinx.serialization`. Seeded with presets:
    1. Morning Adhkar (Spiritual protection and mindfulness)
    2. Duha Prayer (The prayer of the repentant)
    3. Smile at Everyone (Sunnah of the Prophet ﷺ)
    4. Read 1 Page Quran (Daily spiritual nourishment)
* **Presentation Layer**:
  * `HabitsViewModel`: Calculates progress percentage and updates habit completion.
  * `HabitsScreen` & `HabitsContent`:
    * Daily Progress card (`Daily Progress 65%`) with Hadith quote.
    * Active habits checkable cards with flame streak indicator (`12d`, `5d`).
    * `+ Add New Habit` dashed button opening `AddHabitBottomSheet` with preset suggestions and custom input.
* **Acceptance Criteria**:
  * [ ] Toggling a habit updates daily progress percentage in real time.
  * [ ] Streaks persist across app launches.
  * [ ] Custom habits can be added and persisted.

---

#### 4.5 Explore Tab & Map POI Viewer (Page 3)
* **Domain Layer**:
  * [NEW] `domain/model/HistoricalSite.kt` & `domain/model/ExploreCollection.kt`.
  * [NEW] `domain/repository/ExploreRepository.kt` & `GetExploreContentUseCase.kt`.
* **Data Layer**:
  * [NEW] `files/en/explore.json` & `files/ar/explore.json`: Curated collections (`Letters & Treaties`, `The Commanders`, `Legacy of Charity`) and historical sites (`Makkah`, `Madinah`, `Badr`, `Uhud`, `Jerusalem`, `Damascus`).
  * [NEW] `ExploreRepositoryImpl.kt`.
* **Presentation Layer**:
  * `ExploreScreen` & `ExploreContent`:
    * Historical Sites Hero Card with antique map visual and `Open Map Explorer` button.
    * Curated Collections horizontal carousel (`18 ARTIFACTS`, `12 STRATEGIES`, `24 STORIES`).
    * Audio Stories discovery card (`Quiet in the Library` / `Discover New Path`).
  * `MapExplorerScreen`: Full-screen interactive Points of Interest (POI) viewer displaying historical sites with coordinates, historical context, and connected Sahaba journeys.
* **Acceptance Criteria**:
  * [ ] Tapping `Open Map Explorer` displays the interactive POI site viewer.
  * [ ] Curated collections display correct artifact and story badges.

---

#### 4.6 Journey Reader Flow (Page 4)
* **Domain & Data**:
  * [NEW] `domain/repository/BookmarksRepository.kt` & `BookmarksRepositoryImpl.kt`: Persisted in `KeyValueStorage`.
* **Presentation Layer**:
  * `JourneyFlowScreen` & `JourneyFlowContent`:
    * Top Bar: Back button, step title (`STEP 2 OF 5 / The Test of Loyalty`), and bookmark toggle button.
    * Segmented 5-step progress bar at top.
    * Ayah / Calligraphy Card:
      * Ornate quotation marks.
      * Calligraphy Arabic verse (`إِنَّ اللَّهَ مَعَ الصَّابِرِينَ`).
      * English translation & citation (`AL-BAQARAH 2:153`).
    * Narrative Story Paragraph: Clean typography, high readability.
    * Reflection Callout Box: Gold left border bracket, `REFLECTION` label, question prompt.
    * Floating `Continue →` CTA button (transitions to `Complete Journey ✓` on step 5).
* **Acceptance Criteria**:
  * [ ] Segmented progress bar accurately updates with step changes.
  * [ ] Tapping the bookmark button persists the journey to the saved list.
  * [ ] Arabic Ayah displays with proper tashkeel spacing.

---

#### 4.7 Journey Complete Screen (Page 5)
* **Presentation Layer**:
  * `CompletionScreen` & `CompletionContent`:
    * Soft radial aura glow background with circular green checkmark.
    * Celebration Title: `Journey Complete / May it bring you peace`.
    * Daily Reflection Card: Quote, companion attribution, date, and native share button.
    * Primary CTA: `Done for Today` (returns to Today screen).
    * Countdown Timer: `Next Journey in 14h 22m`.
* **Acceptance Criteria**:
  * [ ] Share button triggers platform share sheet with the reflection quote.
  * [ ] Countdown calculates remaining time until midnight.

---

#### 4.8 Settings Screen (Page 8)
* **Presentation Layer**:
  * `SettingsScreen` & `SettingsContent`:
    * Header: `Settings / Personalize your journey`.
    * Appearance Section: Dark Mode switch, App Language selector (`English >` / `العربية >`).
    * Notifications Section: Daily Habit Time picker (`08:00 AM`), Push Notifications switch.
    * Note: Account section (Profile, Privacy) and Logout are omitted per directive.
* **Acceptance Criteria**:
  * [ ] Toggling dark mode instantly applies the new theme.
  * [ ] Switching language triggers locale shift and RTL/LTR adjustment.
  * [ ] Notification schedule updates via `NotificationScheduler`.

---

## 5. Architectural Verification & Testing Strategy

### Automated Unit Tests
Command to execute tests:
```bash
./gradlew :shared:testAndroidHostTest
```

Expected Suites:
* `ThemeTest`: Verifies "Sahaba Modern" color tokens and theme generation.
* `HijriDateFormatterTest`: Verifies conversion accuracy across leap years and Islamic months.
* `SahabaRepositoryTest`: Verifies catalog loading, search filtering, and category queries.
* `HabitsRepositoryTest`: Verifies habit completion, streak tracking, and daily percentage calculation.
* `ExploreRepositoryTest`: Verifies loading of historical sites and collections.
* `BookmarksRepositoryTest`: Verifies bookmark persistence.
* `TodayViewModelTest`, `LibraryViewModelTest`, `HabitsViewModelTest`, `ExploreViewModelTest`, `SettingsViewModelTest`: Full state and effect flow verification.

### Architectural Audit
Run the `arch-audit` skill:
* Verify zero `android.*` platform leaks in `domain/` or `commonMain/`.
* Verify 100% parity between `values/strings.xml` and `values-ar/strings.xml`.
* Verify ViewModels only consume Use Cases and expose immutable `UiState`.
