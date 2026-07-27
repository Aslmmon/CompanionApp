# Project Overview
This is a Kotlin Multiplatform (KMP) project utilizing Compose Multiplatform for shared UI across Android and iOS. The core logic is structured in the `:shared` module using a layered Clean Architecture structure under `com.aslmmovic.qurancompanion`:
- **data**: Data sources, DTOs, and Repository implementations.
- **domain**: Business logic (Domain Models, Repository Interfaces, Use Cases).
- **presentation**: ViewModels, Composable Screens, and Navigation.

---

# Architecture Rules
- **Layer Isolation**: Keep `domain` completely free of platform-specific imports (e.g. `android.*`) and data layer dependencies.
- **Data flow**: Repositories and Use Cases must return Domain Models. Map DTOs to Domain Models in the data layer using extension functions (e.g., `toDomain()`).
- **Use Case Single Responsibility**: Encapsulate business logic in Use Case classes exposing a single `operator fun invoke()`.

---

# Feature Development
- **Package by Layer**: Organize code by layer packages (`data`, `domain`, `presentation`), not by feature. Place new files in the corresponding subpackages.
- **Use Case Grouping**: Do not create one file per Use Case. Group related Use Cases into files named after their domain namespace (e.g., `JourneyUseCases.kt`, `OnboardingUseCases.kt`).
- **Platform Abstraction**: Place device-specific implementations (e.g., key-value storage, system locale) in `androidMain` and `iosMain` source sets, referencing interfaces defined in `data/datasource/` or `domain/util/`.

---

# Jetpack Compose
- **Screen-Content Pattern**: Split screens into two composables:
  1. Stateful screen wrapper (`*Screen`): Receives the `ViewModel`, collects UI states using `collectAsStateWithLifecycle()`, and binds event handlers.
  2. Stateless content (`*Content`): Accepts raw states and lambda callbacks. Must be preview-friendly and easy to test.
- **Localization**: Retrieve string values via `stringResource(Res.string.<id>)` from the generated Multiplatform resources.
- **Typography & Theme**: Apply theme tokens from `MaterialTheme`. For Arabic or Quranic text, use `QuranArabicTextStyle` from `ui/theme/Type.kt` to accommodate tashkeel (vowel markings) line heights.

---

# Dependency Injection
- **Framework**: Koin is used for dependency injection.
- **Declaration**: Register common dependencies in `di/AppModule.kt` (`appModule`) and platform overrides in `di/AndroidModule.kt` / `di/IosModule.kt`.
- **Retrieval**: Use `koinViewModel()` inside Composables to fetch ViewModels. Use constructor injection for all non-UI layers. Use `koinInject()` only in top-level entry composables like `App()`.

---

# Navigation
- **Navigation Graph**: Defined in `App.kt` using Jetpack Navigation Compose.
- **Routes**: Define type-safe route strings inside the `AppRoute` sealed class in `presentation/navigation/AppRoute.kt`.
- **Side Effects**: ViewModels emit one-shot actions to a `SharedFlow<*UiEvent>`. Collect these in the Stateful Screen wrapper inside a `LaunchedEffect(vm)` to trigger `navController` actions.

---

# Design System
- **Theme Definitions**: Located in `ui/theme/`.
- **Dynamic Theming**: Support dark and light palettes by leveraging theme colors (e.g., `MaterialTheme.colorScheme.primary`) instead of hardcoding raw color values.

---

# Testing
- **Structure**: Tests are written in `commonTest` using the `kotlin.test` package.
- **Coroutines**: Test suspend functions with `runTest` and `StandardTestDispatcher()`. Call `advanceUntilIdle()` to execute scheduled tasks.
- **Fakes over Mocks**: Do not use mocking libraries (e.g., Mockk). Write simple, in-memory fakes (e.g., `FakeJourneyRepository`) in `commonTest` to stub dependencies.

---

# Performance
- **Flow Lifecycle**: Share StateFlows in ViewModels using `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)` to prevent resource leakage on configuration changes.
- **I/O Caching**: Implement in-memory caches in local data sources to avoid redundant resource parsing (e.g., JSON file reads).

---

# Naming Conventions
- **ViewModels**: `<Feature>ViewModel` (e.g., `HomeViewModel`)
- **Stateful Screens**: `<Feature>Screen` (e.g., `HomeScreen`)
- **Stateless Screens**: `<Feature>Content` (e.g., `HomeContent`)
- **One-Shot Events**: `<Feature>UiEvent` (e.g., `HomeUiEvent`)
- **Use Cases**: `<Verb><Subject>UseCase` (e.g., `GetTodayJourneyUseCase`)
- **Repositories**: `<Subject>Repository` (interface) and `<Subject>RepositoryImpl` (implementation)
- **Data Transfer Objects**: `<Model>Dto` (e.g., `JourneyDto`)

---

# Do
- Recreate host activities when configuration changes require a full resource reload (e.g., `MainActivity.recreate()` on locale switches).
- Map DTOs to Domain models within data layers before returning values.
- Declare custom preview composables to test layouts in multiple screen sizes.

---

# Don't
- Do not import `android.*` or target Android SDK components in `commonMain` code.
- Do not instantiate dependencies (like UseCases or Repositories) inside ViewModels or Screens; use dependency injection.
- Do not bypass Use Cases; ViewModels should interact with the domain layer through Use Cases rather than directly injecting Repositories.
