# Navigation Architecture Guidelines

These guidelines define the navigation structures and flow events.

---

## 1. Navigation Graph
* The app's navigation graph is defined in `App.kt` using Jetpack Navigation Compose.
* Keep navigation structures localized in the shared presentation module.

---

## 2. Type-Safe Routes
* Define type-safe route strings inside the `AppRoute` sealed class/interface in `presentation/navigation/AppRoute.kt`.
* Do not pass raw, un-sanitized string values as routes throughout the presentation screens.

---

## 3. Side Effects & UI Events
* ViewModels must emit one-shot actions (like navigating, showing a toast, or dismissing a dialog) to a `SharedFlow<*UiEvent>`.
* Collect these events in the stateful Screen wrapper inside a `LaunchedEffect(vm)` to trigger navigation actions on the `navController`. Do not invoke `navController` actions directly from ViewModels.

## 4. ViewModel Injection in Nav Routes
* ViewModels are **not** passed as parameters into `*Screen` composables from `App.kt`.
* Each `composable { }` block calls `koinViewModel()` **inside** the Screen composable itself (as a default parameter), keeping `App.kt` routes as thin navigation coordinators.
* `LaunchedEffect(viewModel.uiEffects) { ... }` is declared **inside** the `*Screen` composable to collect one-shot navigation effects — never in `App.kt`.
* `App.kt` routes pass only navigation lambdas to each Screen: `onNavigateTo<Destination>: () -> Unit`.

## 5. AppRoute Sealed Class
* All routes are defined as `data object` entries inside `sealed class AppRoute(val route: String)` at `presentation/navigation/AppRoute.kt`.
* Never pass raw string literals as route arguments anywhere — always use `AppRoute.Xxx.route`.
* Typed argument routes (e.g. `detail/{id}`) must be added as sealed class subclasses with matching `navArguments` and `arguments` declarations.

## 6. Feature Package Co-location
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
