# SPEC-XXX: [Feature Name]

## Status
- [ ] Draft
- [ ] In Review
- [ ] Approved (Frozen)
- [ ] Implemented
- [ ] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Brief 2-3 sentence description of what this feature accomplishes.
- **User Story**: *As a [type of user], I want to [perform action] so that [obtain benefit].*
- **Success Metrics**: Explicit criteria to measure whether the feature is successful (e.g. daily retention, zero crashes, <100ms load time).

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
- [Item 1]
- [Item 2]
- [Item 3]

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> The implementation MUST NOT include or introduce any of the following:
- [Prohibited item or API 1]
- [Prohibited item or dependency 2]

---

## 3. Domain Contracts & Pure Business Logic

### 3.1 Domain Models (`domain/model/`)
```kotlin
package com.aslmmovic.qurancompanion.domain.model

@Immutable // or @Serializable if persisted
data class ExampleDomainModel(
    val id: String,
    val title: String,
    val timestamp: Long
)
```

### 3.2 Repository Interface Contract (`domain/repository/`)
```kotlin
package com.aslmmovic.qurancompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import com.aslmmovic.qurancompanion.domain.model.ExampleDomainModel

interface ExampleRepository {
    fun observeData(): Flow<List<ExampleDomainModel>>
    suspend fun getById(id: String): Result<ExampleDomainModel>
    suspend fun save(model: ExampleDomainModel): Result<Unit>
}
```

### 3.3 Use Case Contracts (`domain/usecase/`)
```kotlin
package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.ExampleDomainModel
import com.aslmmovic.qurancompanion.domain.repository.ExampleRepository

class GetExampleDataUseCase(
    private val repository: ExampleRepository
) {
    suspend operator fun invoke(id: String): Result<ExampleDomainModel> {
        return repository.getById(id)
    }
}
```

---

## 4. Presentation & State Machine Contract

### 4.1 UI State & Effects (`presentation/screens/<feature>/`)
```kotlin
package com.aslmmovic.qurancompanion.presentation.screens.example

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.ExampleDomainModel
import org.jetbrains.compose.resources.StringResource

/**
 * Immutable snapshot of everything the screen needs to render.
 */
@Immutable
data class ExampleUiState(
    val data: ExampleDomainModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null
)

/**
 * One-shot side effects: navigation, snackbars, sound triggers.
 * Emitted via SharedFlow and collected in LaunchedEffect inside the Screen container.
 */
sealed class ExampleUiEffect {
    data class ShowToast(val messageRes: StringResource) : ExampleUiEffect()
    data class NavigateToDetail(val id: String) : ExampleUiEffect()
}
```

### 4.2 ViewModel Contract (`presentation/screens/<feature>/ExampleViewModel.kt`)
* Extends `ViewModel()`, executes async operations via `viewModelScope`.
* Exposes `val uiState: StateFlow<ExampleUiState>`.
* Exposes `val uiEffects: SharedFlow<ExampleUiEffect>`.
* Exposes explicit action methods (e.g., `fun onRefresh()`, `fun onItemSelect(id: String)`).

### 4.3 Screen & Content Decomposition
* **Screen Container (`presentation/screens/<feature>/ExampleScreen.kt`)**: Uses `koinViewModel()`, collects `uiState` with lifecycle, collects `uiEffects` in `LaunchedEffect`, passes navigation lambdas.
* **Stateless Content (`presentation/screens/<feature>/ExampleContent.kt`)**: Pure UI receiving `uiState` and action callbacks; preview-ready with `@Preview`.

---

## 5. Acceptance Criteria & Test Matrix

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected State / Effect (Then) | Target Layer |
|:---|:---|:---|:---|:---|
| **AC-01** | Repository has cached data | ViewModel `init` executed | `UiState` transitions to `Content` | Presentation |
| **AC-02** | Repository fails with exception | `Refresh` action dispatched | `UiState` is `Error`, no crash | Presentation |
| **AC-03** | Valid DTO received from source | `toDomain()` mapping called | Returns mapped Domain Model | Data |
| **AC-04** | Use Case executed with valid ID | `invoke(id)` called | Returns `Result.success(model)` | Domain |

---

## 6. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `example_title` | "Feature Title" | "عنوان الميزة" |
| `example_error_generic` | "An unexpected error occurred" | "حدث خطأ غير متوقع" |
| `example_button_retry` | "Retry" | "إعادة المحاولة" |

---

## 7. Atomic Implementation Checklist (`tasks.md`)

- [ ] **Phase 1: Domain Layer**
  - [ ] Create `domain/model/` data classes
  - [ ] Define `domain/repository/` interface
  - [ ] Implement `domain/usecase/` use case classes
- [ ] **Phase 2: Data Layer**
  - [ ] Implement DTOs & `toDomain()` mappers
  - [ ] Implement Repository in `data/repository/`
- [ ] **Phase 3: Presentation Layer**
  - [ ] Define `UiState`, `UiAction`, `UiEffect`
  - [ ] Implement `ViewModel` with explicit action methods
  - [ ] Implement Container `Screen` + Stateless `Content`
- [ ] **Phase 4: DI & Resources**
  - [ ] Register dependencies in `AppModule.kt`
  - [ ] Add strings in `values/strings.xml` and `values-ar/strings.xml`
- [ ] **Phase 5: Automated Verification**
  - [ ] Unit tests for UseCases & ViewModel in `commonTest` (covering AC-01 to AC-04)
  - [ ] Run `arch-audit`
