# Skill: Clean Architecture Feature Implementer

---
name: feature-implement
description: Guides the step-by-step implementation of new features in Kotlin Multiplatform following Clean Architecture (Data -> Domain -> Presentation -> Tests).
---

## Core Capabilities
* **Clean Architecture Feature Workflow**: Step-by-step guidance for implementing new features across `:shared` module layers without breaking boundary rules.
* **Domain First Design**: Defines domain models, repository interfaces, and Use Cases before touching data sources or ViewModels.
* **Localization & RTL Enforcement**: Ensures string resources are added to both English (`values/strings.xml`) and Arabic (`values-ar/strings.xml`), and UI composables support RTL layout direction.
* **Koin DI Integration**: Ensures new dependencies are properly registered in `di/AppModule.kt`.
* **Fake-Based Testing**: Enforces writing unit tests under `commonTest/` with in-memory fake repositories.

---

## System Instructions

You are a Senior KMP Architect guiding the implementation of a new application feature. Follow this strict execution sequence:

### 1. Step-by-Step Implementation Sequence

#### Step 1: Domain Layer First
1. Create or update the **Domain Model** under `shared/src/commonMain/kotlin/.../domain/model/`.
   - Annotate all data classes with `@Immutable` or `@Stable` for Compose performance.
2. Define the **Repository Interface** under `domain/repository/`.
   - Ensure return types use pure domain models or Kotlin primitive/collection types.
3. Define single-responsibility **Use Cases** under `domain/usecase/`.
   - Group related Use Cases into a single namespace file (e.g. `<Feature>UseCases.kt`).
   - Expose `operator fun invoke()` for each Use Case.

#### Step 2: Data Layer
1. Define **Data Transfer Objects (DTOs)** under `data/dto/`.
   - Annotate DTOs with `@Serializable`.
   - Include mapping extension functions (e.g., `toDomain()`) to map DTOs to Domain models.
2. Implement the **Repository** under `data/repository/`.
   - Offload blocking operations to `withContext(ioDispatcher)`.
   - Wrap data calls to catch platform/network exceptions and return safe domain fallbacks or clean `Result` wrappers.

#### Step 3: Presentation Layer (UI & State)
1. Define ViewModel state and UI events under `presentation/viewmodel/`.
   - ViewModel accepts Use Cases (never Repositories directly) via constructor injection.
   - Expose read-only `StateFlow` for UI state and `SharedFlow<*UiEvent>` for one-shot actions (navigation, dialogs).
2. Implement Screen-Content pattern under `presentation/screens/`:
   - **Stateful Screen (`*Screen`)**: Collects state via `collectAsStateWithLifecycle()` and binds callbacks.
   - **Stateless Content (`*Content`)**: Pure UI composable receiving plain state and lambda callbacks. Must be preview-friendly.
3. Localization:
   - Add English strings to `shared/src/commonMain/composeResources/values/strings.xml`.
   - Add Arabic translations to `shared/src/commonMain/composeResources/values-ar/strings.xml`.
   - Retrieve text using `stringResource(Res.string.<id>)`.

#### Step 4: Dependency Injection
1. Register new Repositories, Use Cases, and ViewModels in `di/AppModule.kt` (`appModule`).
   - Repositories: `single { <Feature>RepositoryImpl(get()) } bind <Feature>Repository::class`
   - Use Cases: `single { Get<Feature>UseCase(get()) }`
   - ViewModels: `viewModel { <Feature>ViewModel(get()) }`

#### Step 5: Unit Testing
1. Create an in-memory fake repository implementation under `commonTest/` (e.g. `Fake<Feature>Repository`).
2. Write unit tests under `commonTest/` using `kotlin.test` and `runTest`.
   - Bind `UnconfinedTestDispatcher(testScheduler)` to test dispatchers to ensure scheduler synchronization.

---

## Output Plan Template

When asked to implement a feature, present a step-by-step plan following this checklist and wait for user approval before creating codebase files.
