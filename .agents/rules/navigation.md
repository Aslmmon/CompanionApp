# Navigation Architecture Guidelines

These guidelines define the navigation structures, route declarations, and flow events.

---

## 1. Navigation Graph Localization
* The app's navigation graph is defined in `App.kt` using Jetpack Navigation Compose (`org.jetbrains.androidx.navigation:navigation-compose`).
* Keep all navigation structures localized in the shared presentation module.

---

## 2. Type-Safe Routes
* Define type-safe route strings inside the `AppRoute` sealed class in `presentation/navigation/AppRoute.kt`.
* Do not pass raw, un-sanitized string literals as routes throughout the presentation screens.
* All routes are defined as `data object` entries inside `sealed class AppRoute(val route: String)`.
* Typed argument routes (e.g. `detail/{id}`) must be added as sealed class subclasses with matching `navArguments` declarations.

---

## 3. Side Effects & One-Shot Navigation (`*UiEffect`)
* ViewModels must emit one-shot actions (like navigating, showing a toast, or dismissing a dialog) to a `SharedFlow<*UiEffect>` named `uiEffects`.
* Name them `<Feature>UiEffect` consistently (never `*UiEvent`).
* Collect these effects in the stateful Screen wrapper inside a `LaunchedEffect(viewModel.uiEffects)` to trigger navigation actions on the `navController`.
* Never invoke `navController` actions directly from ViewModels, and never pass `navController` into ViewModels or stateless Content composables.

---

## 4. ViewModel Injection in Nav Routes
* ViewModels are **not** passed as parameters into `*Screen` composables from `App.kt`.
* Each `composable { }` block calls `koinViewModel()` **inside** the Screen composable itself (as a default parameter), keeping `App.kt` routes as thin navigation coordinators.
* `LaunchedEffect(viewModel.uiEffects) { ... }` is declared **inside** the `*Screen` composable to collect one-shot navigation effects — never in `App.kt`.
* `App.kt` routes pass only navigation lambdas to each Screen: `onNavigateTo<Destination>: () -> Unit`.

---

## 5. Feature Package Co-location
* Each screen's composable files, ViewModel, UiState, and UiEffect must live together in `presentation/screens/<feature>/`:
  ```
  presentation/screens/home/
    HomeScreen.kt
    HomeContent.kt
    HomeViewModel.kt
    HomeUiState.kt
    HomeUiEffect.kt
    components/
  ```
* The `presentation/viewmodel/` package is reserved for the app-wide `AppViewModel` only.

---

## 6. Multiplatform Navigation Compatibility (No Navigation 3)
* **Strict Navigation 3 Prohibition**: Do NOT migrate the project or any screen to Android Jetpack Navigation 3 (`androidx.navigation3`). Navigation 3 is an Android-only experimental library that does not support Compose Multiplatform and would completely break the iOS target.
* All navigation must strictly use Jetpack Navigation Compose 2.9 Multiplatform.
