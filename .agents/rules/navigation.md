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
