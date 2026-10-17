# SPEC-009: Sunnah Habits Tracker

## Status
- [ ] Draft
- [ ] In Review
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Implements the persistent **Sunnah Habits Tracker** screen corresponding to Figma design node `3107:559`. Replaces the current `HabitsPlaceholderScreen` on `MainTab.Habits` (`main/habits`). Provides a daily habit checklist rooted in the Sunnah of the Prophet ﷺ and the Sahaba, calculates daily completion progress, preserves ongoing streaks, and enables adding curated and custom habits via an interactive bottom sheet.
- **User Story**: *As a Muslim user striving to emulate the Prophet ﷺ and his companions, I want a daily Sunnah habit checklist with streak tracking and an authentic Hadith reminder so that I can consistently perform small, beloved daily deeds.*
- **Success Metrics**:
  - Instantaneous (<16ms) habit check/uncheck updates.
  - Zero network dependency (100% offline-first using bundled localized JSON catalog and local key-value storage).
  - 100% test coverage across Domain, Data, and Presentation layers in `commonTest`.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
1. **Domain Layer**:
   - `Habit` model and `HabitCategory` enum (`DailyPrayers`, `Adhkar`, `Character`, `QuranAndFasting`).
   - `HabitRepository` contract.
   - Use cases: `GetActiveHabitsUseCase`, `GetCuratedHabitsUseCase`, `ToggleHabitCompletionUseCase`, `AddHabitFromCatalogUseCase`, `CreateCustomHabitUseCase`, `RemoveHabitUseCase`.
2. **Data Layer**:
   - Localized JSON catalogs: `files/en/habits.json` and `files/ar/habits.json` with 10+ curated Sunnah habits (including the 4 Figma defaults: `morning_adhkar`, `duha_prayer`, `smile_everyone`, `read_quran_page`).
   - `HabitDto` with `toDomain()` mapping.
   - `HabitLocalDataSource` and `HabitRepositoryImpl` using `KeyValueStorage` and `DateTimeProvider`.
   - Streak tracking logic (streak increments on consecutive days, resets after missed days, reverts on uncheck today).
3. **Presentation Layer**:
   - `HabitsUiState` (Immutable), `HabitsUiEffect` (`ShowCelebrationEffect`, `ShowErrorEffect`).
   - `HabitsViewModel` managing reactive flows and explicit action methods.
   - `HabitsScreen` (container) and `HabitsContent` (stateless UI).
   - Components: `DailyProgressCard`, `ActiveHabitsSection`, `HabitCard`, `AddHabitButton`, `AddHabitBottomSheet`, `HabitDetailBottomSheet`.
4. **DI & Localization**:
   - Koin registration in `AppModule.kt`.
   - Navigation integration replacing `HabitsPlaceholderScreen` in `MainNavigationScaffold.kt`.
   - Comprehensive bilingual string definitions in `values/strings.xml` and `values-ar/strings.xml`.

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> The implementation MUST NOT include or introduce any of the following:
> - No network calls or remote API synchronization.
> - No platform SDK imports in `commonMain` or `domain`.
> - No mock frameworks (e.g. Mockk/Mockito); use in-memory fake repositories in `commonTest`.
> - No SQL database creation; use lightweight bundled JSON and `KeyValueStorage`.

---

## 3. Domain Contracts & Pure Business Logic

### 3.1 Domain Models (`domain/model/Habit.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class HabitCategory {
    DailyPrayers,
    Adhkar,
    Character,
    QuranAndFasting
}

@Immutable
data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val category: HabitCategory,
    val isCustom: Boolean = false,
    val currentStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String? = null
)
```

### 3.2 Repository Contract (`domain/repository/HabitRepository.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<Habit>>
    fun observeCuratedCatalog(): Flow<List<Habit>>
    suspend fun toggleHabitCompletion(habitId: String): Result<Unit>
    suspend fun addHabitFromCatalog(habitId: String): Result<Unit>
    suspend fun createCustomHabit(title: String, description: String, category: HabitCategory): Result<Unit>
    suspend fun removeHabit(habitId: String): Result<Unit>
}
```

### 3.3 Use Case Contracts (`domain/usecase/HabitUseCases.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow

class GetActiveHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> = repository.observeActiveHabits()
}

class GetCuratedHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Flow<List<Habit>> = repository.observeCuratedCatalog()
}

class ToggleHabitCompletionUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String): Result<Unit> = repository.toggleHabitCompletion(habitId)
}

class AddHabitFromCatalogUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String): Result<Unit> = repository.addHabitFromCatalog(habitId)
}

class CreateCustomHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(title: String, description: String, category: HabitCategory): Result<Unit> =
        repository.createCustomHabit(title, description, category)
}

class RemoveHabitUseCase(private val repository: HabitRepository) {
    suspend operator fun invoke(habitId: String): Result<Unit> = repository.removeHabit(habitId)
}
```

---

## 4. Presentation & State Machine Contract

### 4.1 UI State & Effects (`presentation/screens/habits/`)
```kotlin
@Immutable
data class HabitsUiState(
    val activeHabits: List<Habit> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val progressPercentage: Int = 0,
    val isAllCompleted: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val curatedCatalog: List<Habit> = emptyList(),
    val selectedCatalogCategory: HabitCategory? = null,
    val selectedHabitForDetail: Habit? = null,
    val isLoading: Boolean = false
)

sealed interface HabitsUiEffect {
    data object ShowCelebrationEffect : HabitsUiEffect
    data class ShowErrorEffect(val messageRes: org.jetbrains.compose.resources.StringResource) : HabitsUiEffect
}
```

---

## 5. Acceptance Criteria & Test Matrix (TDD Single Source of Truth)

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected State / Effect (Then) | Target Layer | Automated Test Function |
|:---|:---|:---|:---|:---|:---|
| **AC-01** | First launch with no stored habits | `observeActiveHabits()` invoked | 4 default Sunnah habits seeded with 0d streaks | Data | `test_AC01_givenFreshInstall_whenObserveActive_thenSeedsDefaultHabits` |
| **AC-02** | Active habit unchecked today | `toggleHabitCompletion(id)` invoked | `isCompletedToday = true`, streak becomes 1, `lastCompletedDate = today` | Data | `test_AC02_givenUncheckedHabit_whenToggle_thenMarksCompletedWithStreakOne` |
| **AC-03** | Habit completed yesterday with streak N | `toggleHabitCompletion(id)` invoked today | Streak increments to N + 1 | Data | `test_AC03_givenCompletedYesterday_whenCheckedToday_thenIncrementsStreak` |
| **AC-04** | Habit last completed 2+ days ago | `toggleHabitCompletion(id)` invoked today | Streak resets to 1 | Data | `test_AC04_givenMissedDay_whenCheckedToday_thenResetsStreakToOne` |
| **AC-05** | Habit completed today | `toggleHabitCompletion(id)` invoked today | `isCompletedToday = false`, streak reverts to previous count | Data | `test_AC05_givenCompletedToday_whenUncheck_thenRevertsStreak` |
| **AC-06** | 2 of 4 habits checked | ViewModel updates state | `completedCount = 2`, `totalCount = 4`, `progressPercentage = 50` | Presentation | `test_AC06_givenTwoOfFourCompleted_thenProgressIsFiftyPercent` |
| **AC-07** | All 4 active habits checked | Last habit toggled | `progressPercentage = 100`, emits `ShowCelebrationEffect` | Presentation | `test_AC07_givenAllCompleted_thenEmitsCelebrationEffect` |
| **AC-08** | Screen displayed | User taps "+ Add New Habit" | `isAddSheetOpen = true`, catalog loaded | Presentation | `test_AC08_whenOpenAddSheet_thenSheetIsOpenWithCatalog` |
| **AC-09** | Add sheet open | User selects habit from catalog | Habit added to active list with 0d streak, sheet closed | Data / Presentation | `test_AC09_whenAddFromCatalog_thenHabitAddedToActiveList` |
| **AC-10** | Detail sheet open | User taps remove habit | Habit removed from active list, progress recalculated | Data / Presentation | `test_AC10_whenRemoveHabit_thenHabitRemovedAndProgressUpdated` |
| **AC-11** | Custom habit created | User inputs title, description, category | Custom habit created, `isCustom = true`, appears in active list | Data / Presentation | `test_AC11_whenCreateCustomHabit_thenAddedWithCustomFlag` |

---

## 6. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `habits_title` | "Sunnah Habits" | "عادات السنة" |
| `habits_subtitle` | "Small consistent deeds beloved to Allah" | "أحب الأعمال إلى الله أدومها وإن قل" |
| `habits_progress_title` | "Today's Progress" | "إنجاز اليوم" |
| `habits_progress_hadith` | "The most beloved of deeds to Allah are those that are most consistent, even if they are small." | "أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ" |
| `habits_progress_hadith_ref` | "— Sahih al-Bukhari 6464" | "— صحيح البخاري ٦٤٦٤" |
| `habits_active_section_title` | "Active Habits" | "العادات النشطة" |
| `habits_done_count_badge` | "%1$d of %2$d done" | "%1$d من %2$d مكتملة" |
| `habits_streak_days` | "%1$dd" | "%1$dي" |
| `habits_add_button` | "+ Add New Habit" | "+ إضافة عادة جديدة" |
| `habits_add_sheet_title` | "Add Sunnah Habit" | "إضافة عادة نبوية" |
| `habits_add_sheet_curated` | "From the Sunnah & Sahaba" | "من السنة والصحابة" |
| `habits_add_sheet_custom` | "Create Custom Habit" | "إنشاء عادة خاصة" |
| `habits_filter_all` | "All" | "الكل" |
| `habits_cat_daily_prayers` | "Prayers" | "الصلوات" |
| `habits_cat_adhkar` | "Adhkar" | "الأذكار" |
| `habits_cat_character` | "Character" | "الأخلاق" |
| `habits_cat_quran_fasting` | "Quran & Fasting" | "القرآن والصيام" |
| `habits_custom_title_label` | "Habit Title" | "عنوان العادة" |
| `habits_custom_desc_label` | "Intention / Description" | "النية / الوصف" |
| `habits_custom_create_action` | "Add Habit" | "إضافة العادة" |
| `habits_detail_remove_action` | "Remove Habit" | "إزالة العادة" |
| `habits_detail_remove_confirm` | "Are you sure you want to remove this habit?" | "هل أنت متأكد من رغبتك في إزالة هذه العادة؟" |
| `habits_detail_streak_label` | "Current Streak: %1$d days" | "المحافظة الحالية: %1$d أيام" |
| `habits_empty_title` | "No Active Habits" | "لا توجد عادات نشطة" |
| `habits_empty_subtitle` | "Tap below to add habits from the Sunnah" | "اضغط أدناه لإضافة عادات من السنة" |
| `habits_celebration_title` | "Masha'Allah!" | "ما شاء الله!" |
| `habits_celebration_subtitle` | "You have completed all habits for today!" | "لقد أكملت جميع عادات اليوم!" |

---

## 7. Atomic Implementation Checklist

- [ ] **Phase 1: Red (Test Authoring)**
  - [ ] Create `FakeHabitRepository.kt` in `commonTest/fakes/`
  - [ ] Implement `HabitRepositoryImplTest.kt`
  - [ ] Implement `HabitUseCasesTest.kt`
  - [ ] Implement `HabitsViewModelTest.kt`
  - [ ] Run test task to confirm RED
- [ ] **Phase 2: Green (Implementation)**
  - [ ] Create localized JSON seed files `files/en/habits.json` and `files/ar/habits.json`
  - [ ] Create `domain/model/Habit.kt`
  - [ ] Create `domain/repository/HabitRepository.kt`
  - [ ] Create `domain/usecase/HabitUseCases.kt`
  - [ ] Create `data/dto/HabitDto.kt`
  - [ ] Create `data/datasource/HabitLocalDataSource.kt`
  - [ ] Create `data/repository/HabitRepositoryImpl.kt`
  - [ ] Create `presentation/screens/habits/HabitsUiState.kt` and `HabitsUiEffect.kt`
  - [ ] Create `presentation/screens/habits/HabitsViewModel.kt`
  - [ ] Create UI composables and components
  - [ ] Register DI in `AppModule.kt`
  - [ ] Update `MainNavigationScaffold.kt` to display `HabitsScreen()`
  - [ ] Add XML string resources in English and Arabic
  - [ ] Run test task until all tests PASS
- [ ] **Phase 3: Refactor & Verification**
  - [ ] Clean Architecture audit (no Android SDK in domain/commonMain)
  - [ ] Update `spec.md` and `FEATURE_REGISTRY.md` to `Verified`
