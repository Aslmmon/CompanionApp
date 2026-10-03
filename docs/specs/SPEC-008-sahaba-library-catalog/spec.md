# SPEC-008: Sahaba Library Catalog & Encyclopedia

## Status
- [ ] Draft
- [ ] In Review
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Implements the persistent **Library Screen** (Figma node `3107-2` / Page 1 of `design_ui_ux.pdf`) as an authentic Sahaba Encyclopedia. Features a real-time search bar, category filter chips (*All*, *Ten Promised*, *Mothers of Believers*, *Ansar*, *Muhajirun*), a 2-column grid of companion cards with Arabic calligraphy initial-letter avatars, and an interactive Sahaba Biography Bottom Sheet.
- **User Story**: *As a Muslim user striving to learn from the Prophet's companions, I want to explore and search an encyclopedia of Sahaba categorized by their historical roles so that I can easily discover their virtues, epithets, and life stories.*
- **Success Metrics**: 
  - Instantaneous (<16ms) local search and category filtering.
  - Zero network dependency (100% offline-first using bundled localized JSON catalog).
  - 100% test coverage across Domain, Data, and Presentation layers in `commonTest`.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
1. **Domain Layer**:
   - `Sahaba` and `SahabaCategory` domain models.
   - `SahabaRepository` contract.
   - `GetSahabaCatalogUseCase` (with category filtering and text search).
   - `GetSahabaDetailUseCase`.
2. **Data Layer**:
   - Seed datasets: `files/en/sahaba.json` and `files/ar/sahaba.json` with 16 prominent companions covering all 4 categories.
   - `SahabaDto` with `toDomain()` mapping functions.
   - `SahabaLocalDataSource` and `SahabaRepositoryImpl`.
3. **Presentation Layer**:
   - `LibraryViewModel` exposing `uiState: StateFlow<LibraryUiState>` and explicit user actions.
   - `LibraryScreen` (container) replacing `LibraryPlaceholderScreen` in `MainNavigationScaffold`.
   - `LibraryContent` (stateless composable):
     - Search bar (`"Search Sahaba by name or trait..."`) with clear button.
     - Horizontally scrollable category filter chips.
     - 2-column adaptive `LazyVerticalGrid` of `SahabaCard`s displaying calligraphy avatar, name, epithet, and journey badge.
     - `SahabaDetailBottomSheet` displaying calligraphy badge, honorific Arabic and English titles, category, and comprehensive biography.
4. **DI & Localization**:
   - Koin registration in `AppModule.kt`.
   - Complete bilingual string definitions in `values/strings.xml` and `values-ar/strings.xml`.

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> The implementation MUST NOT include or introduce any of the following:
> - No network calls or remote API fetching (pure local offline JSON).
> - No direct navigation to the Journey Reader from Sahaba cards in this iteration (per user decision: display biographical bottom sheet only).
> - No SQL database creation for the static catalog (use bundled JSON data source).
> - No mock frameworks (e.g. Mockk/Mockito); use in-memory fake repositories in `commonTest`.

---

## 3. Domain Contracts & Pure Business Logic

### 3.1 Domain Models (`domain/model/`)
```kotlin
package com.aslmmovic.qurancompanion.domain.model

import androidx.compose.runtime.Immutable

@Immutable
enum class SahabaCategory {
    ALL,
    TEN_PROMISED,
    MOTHERS_OF_BELIEVERS,
    ANSAR,
    MUHAJIRUN
}

@Immutable
data class Sahaba(
    val id: String,
    val name: String,
    val arabicName: String,
    val initialLetterArabic: String,
    val epithet: String,
    val category: SahabaCategory,
    val journeyCount: Int,
    val bio: String
)
```

### 3.2 Repository Contract (`domain/repository/SahabaRepository.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import kotlinx.coroutines.flow.Flow

interface SahabaRepository {
    fun getSahabaCatalog(): Flow<List<Sahaba>>
    suspend fun getSahabaById(id: String): Result<Sahaba>
}
```

### 3.3 Use Case Contracts (`domain/usecase/`)
```kotlin
package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSahabaCatalogUseCase(
    private val repository: SahabaRepository
) {
    operator fun invoke(
        category: SahabaCategory = SahabaCategory.ALL,
        searchQuery: String = ""
    ): Flow<List<Sahaba>> {
        return repository.getSahabaCatalog().map { list ->
            list.filter { sahaba ->
                val matchesCategory = category == SahabaCategory.ALL || sahaba.category == category
                val matchesQuery = searchQuery.isBlank() ||
                    sahaba.name.contains(searchQuery, ignoreCase = true) ||
                    sahaba.arabicName.contains(searchQuery, ignoreCase = true) ||
                    sahaba.epithet.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }
    }
}

class GetSahabaDetailUseCase(
    private val repository: SahabaRepository
) {
    suspend operator fun invoke(id: String): Result<Sahaba> {
        return repository.getSahabaById(id)
    }
}
```

---

## 4. Presentation & State Machine Contract

### 4.1 UI State (`presentation/screens/library/LibraryUiState.kt`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.screens.library

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import org.jetbrains.compose.resources.StringResource

@Immutable
data class LibraryUiState(
    val searchQuery: String = "",
    val selectedCategory: SahabaCategory = SahabaCategory.ALL,
    val sahabaList: List<Sahaba> = emptyList(),
    val selectedSahabaDetail: Sahaba? = null,
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null
)
```

### 4.2 ViewModel Contract (`presentation/screens/library/LibraryViewModel.kt`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.screens.library

import androidx.lifecycle.ViewModel
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import kotlinx.coroutines.flow.StateFlow

abstract class BaseLibraryViewModel : ViewModel() {
    abstract val uiState: StateFlow<LibraryUiState>
    abstract fun onSearchQueryChanged(query: String)
    abstract fun onCategorySelected(category: SahabaCategory)
    abstract fun onSahabaSelected(sahaba: Sahaba)
    abstract fun onDismissDetail()
}
```

### 4.3 Screen & Content Decomposition
* **Container**: [`LibraryScreen.kt`](shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/library/LibraryScreen.kt)
  - Hoists state from `LibraryViewModel` via `koinViewModel()`.
  - Connects to `MainNavigationScaffold`.
* **Stateless UI**: [`LibraryContent.kt`](shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/presentation/screens/library/LibraryContent.kt)
  - Pure composable with `LibraryUiState` and callbacks.
* **Components**:
  - `SahabaCard`: Card with Arabic initial calligraphy avatar (`ElMessiriFontFamily`), full name, epithet, and journey badge.
  - `CategoryFilterChips`: Horizontal row of filter pills (*All*, *Ten Promised*, *Mothers of Believers*, *Ansar*, *Muhajirun*).
  - `SahabaDetailBottomSheet`: Modal bottom sheet displaying calligraphy emblem, English/Arabic titles, category badge, and complete biography.

---

## 5. Acceptance Criteria & Test Matrix (TDD Single Source of Truth)

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected State / Effect (Then) | Target Layer | Automated Test Function |
|:---|:---|:---|:---|:---|:---|
| **AC-01** | Valid JSON bundled catalog | `SahabaRepository.getSahabaCatalog()` called | Emits 16 fully mapped `Sahaba` domain entities | Data | `test_AC01_givenBundledJson_whenGetCatalog_thenReturnsMappedSahabaList()` |
| **AC-02** | Catalog loaded with all categories | `GetSahabaCatalogUseCase` invoked with `TEN_PROMISED` | Emits only companions with `TEN_PROMISED` category | Domain | `test_AC02_givenCatalog_whenFilterByCategory_thenReturnsOnlyMatchingCategory()` |
| **AC-03** | Catalog loaded | `GetSahabaCatalogUseCase` invoked with search query "Abu Bakr" | Emits only matching companions | Domain | `test_AC03_givenCatalog_whenSearchQueryApplied_thenReturnsMatchingSahaba()` |
| **AC-04** | Valid companion ID | `GetSahabaDetailUseCase` invoked with "abu_bakr" | Returns `Result.success(sahaba)` | Domain | `test_AC04_givenValidId_whenGetSahabaDetail_thenReturnsSahaba()` |
| **AC-05** | ViewModel initialized | Default state observed | `uiState` has `selectedCategory == ALL`, non-empty `sahabaList` | Presentation | `test_AC05_givenViewModelInit_thenDefaultStateIsPopulated()` |
| **AC-06** | ViewModel initialized | `onCategorySelected(ANSAR)` called | `uiState.selectedCategory == ANSAR` and list filtered | Presentation | `test_AC06_givenCategorySelection_whenChanged_thenStateFiltersList()` |
| **AC-07** | ViewModel initialized | `onSearchQueryChanged("Umar")` called | `uiState.searchQuery == "Umar"` and list filtered | Presentation | `test_AC07_givenSearchQuery_whenUpdated_thenStateFiltersList()` |
| **AC-08** | ViewModel initialized | `onSahabaSelected(sahaba)` called | `uiState.selectedSahabaDetail` is set to companion | Presentation | `test_AC08_givenSahabaCardClicked_thenSelectedSahabaDetailSet()` |
| **AC-09** | Detail sheet open | `onDismissDetail()` called | `uiState.selectedSahabaDetail == null` | Presentation | `test_AC09_givenDetailOpen_whenDismissed_thenSelectedSahabaDetailIsNull()` |

---

## 6. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `library_title` | "Library" | "المكتبة" |
| `library_search_placeholder` | "Search Sahaba by name or trait..." | "ابحث عن صحابي بالاسم أو السمة..." |
| `library_no_results` | "No companions found" | "لم يتم العثور على صحابة" |
| `library_journeys_suffix` | "JOURNEYS" | "رحلات" |
| `category_all` | "All" | "الكل" |
| `category_ten_promised` | "Ten Promised" | "العشرة المبشرون" |
| `category_mothers_of_believers` | "Mothers of Believers" | "أمهات المؤمنين" |
| `category_ansar` | "Ansar" | "الأنصار" |
| `category_muhajirun` | "Muhajirun" | "المهاجرون" |
| `sahaba_detail_close` | "Close" | "إغلاق" |

---

## 7. Atomic Implementation Checklist (`tasks.md`)

- [x] **Phase 1: Domain Contracts & Unit Tests (TDD Red)**
  - [x] Write `SahabaRepositoryTest` and `GetSahabaCatalogUseCaseTest` in `commonTest` (AC-01 to AC-04)
  - [x] Implement `Sahaba.kt`, `SahabaCategory.kt`, `SahabaRepository.kt`
  - [x] Implement `GetSahabaCatalogUseCase.kt`, `GetSahabaDetailUseCase.kt`
- [x] **Phase 2: Data Layer & Seed Catalog (TDD Green)**
  - [x] Create `files/en/sahaba.json` and `files/ar/sahaba.json`
  - [x] Implement `SahabaDto.kt` with mapper `toDomain()`
  - [x] Implement `SahabaLocalDataSource.kt` and `SahabaRepositoryImpl.kt`
  - [x] Run domain/data tests to pass
- [x] **Phase 3: Presentation Layer & Unit Tests (TDD Red -> Green)**
  - [x] Write `LibraryViewModelTest` in `commonTest` (AC-05 to AC-09)
  - [x] Implement `LibraryUiState.kt` and `LibraryViewModel.kt`
  - [x] Implement `SahabaCard.kt`, `CategoryFilterChips.kt`, `SahabaDetailBottomSheet.kt`
  - [x] Implement `LibraryScreen.kt` and `LibraryContent.kt`
  - [x] Connect `LibraryScreen` into `MainNavigationScaffold.kt`
- [x] **Phase 4: DI & Multiplatform Resources**
  - [x] Bind contracts in `AppModule.kt`
  - [x] Add all XML strings in `values/strings.xml` and `values-ar/strings.xml`
- [x] **Phase 5: Verification & Quality Audit**
  - [x] Run `./gradlew :shared:testAndroidHostTest`
  - [x] Run `arch-audit` to guarantee zero layer leaks
