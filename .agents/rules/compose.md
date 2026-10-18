# Compose Multiplatform UI Rules

These guidelines define UI development, performance optimization, and Compose Multiplatform standards for the project.

---

## 1. Screen-Content Pattern
Every screen implementation must be split into two composable structures:
1. **Stateful Screen Wrapper (`*Screen`)**:
   - Injects ViewModel via `koinViewModel()` (as a default parameter).
   - Collects ViewState flows using lifecycle-aware collection (`collectAsStateWithLifecycle()`).
   - Binds UI action lambdas and collects one-shot effects (`LaunchedEffect(viewModel.uiEffects)`).
2. **Stateless Screen Content (`*Content`)**:
   - Receives plain state representations (`UiState`) and action lambda callbacks.
   - Preview-friendly, fully independent of ViewModels, and unit-testable.

---

## 2. Localization
* Retrieve string values via `stringResource(Res.string.<id>)` from generated Compose Multiplatform resources.
* **No Hardcoded User-Facing Text**: Any new text presented to the user must be added to localization resource files:
  - English strings: `shared/src/commonMain/composeResources/values/strings.xml`
  - Arabic translations: `shared/src/commonMain/composeResources/values-ar/strings.xml`

---

## 3. Arabic Typography & Theme
* Apply theme tokens directly from `MaterialTheme.colorScheme` and `MaterialTheme.typography` (never hardcode raw colors or text styles).
* For Arabic or Quranic text, use `QuranArabicTextStyle` from `ui/theme/Type.kt` to accommodate tashkeel (vowel markings) line heights and font features.

---

## 4. Modern Stability & Strong Skipping Mode (Kotlin 2.4+)
* **Strong Skipping Active**: The Compose compiler has Strong Skipping Mode enabled by default. Unstable parameters are skipped automatically using fast $O(1)$ instance equality (`===`).
* **Never Add `@Immutable` or `@Stable` to Collection Models**: When writing or updating UI models containing collections (`List`, `Set`, `Map`), do NOT annotate them with `@Immutable` or `@Stable`, and do NOT wrap standard `List` in `ImmutableList`. Annotating models with collections forces expensive $O(N)$ structural equality (`.equals()`) comparisons on every recomposition pass, degrading scroll performance.
* **Flat Primitive Data Classes**: Data classes with primitive or `String` `val` properties in Compose modules are automatically inferred as stable by the compiler.
* **Domain Model Purity**: Domain models in `domain/model/` must NEVER import `androidx.compose.runtime.*`.

---

## 5. Lazy Layout Virtualization & Performance
* **Mandatory `key` AND `contentType`**: In `LazyColumn`, `LazyRow`, and `LazyVerticalGrid`, every item block MUST declare both a primitive `key` and a `contentType`:
  ```kotlin
  items(
      items = items,
      key = { it.id },
      contentType = { it.type }
  ) { item ->
      ItemCard(item = item)
  }
  ```
  Omitting `contentType` prevents Compose from reusing composition slot tables across different item types during fast scrolling.
* **No In-Item Index Lookups**: Replace `list.indexOf(item)` with `itemsIndexed` to eliminate $O(N^2)$ layout overhead.
* **Hoist Shared Flows**: Collect shared `Flow`s once at the screen level; never call `collectAsStateWithLifecycle()` per visible lazy item.
* **No Impression Side-Effects in Lazy Items**: Never trigger analytics in `LaunchedEffect` inside lazy items (items are pre-composed before entering the viewport).

---

## 6. Phase Deferral & Custom Drawing
* **Phase Deferral via Lambdas**: For rapidly changing state (offsets, scroll indicators, progress animations), defer state reads out of the Composition phase into Layout or Draw by using lambda-based modifiers:
  - Layout Phase: `Modifier.offset { IntOffset(x = state.x, y = 0) }`
  - Draw Phase: `Modifier.graphicsLayer { alpha = state.alpha }`
* **Avoid Decorative Canvas Allocations**: Do not allocate `Path`, `Paint`, or `Brush` objects inside composition or draw loops. Use `Modifier.drawWithCache` to cache drawing objects and recalculate only when size/inputs change.
* **Modifier Architecture**: Prefer `Modifier.Node` over legacy `Modifier.composed` for reusable custom modifiers.

---

## 7. Edge-to-Edge & System Insets
* Target SDK 35+ enforces edge-to-edge on Android. Handle system insets cleanly across Android and iOS:
  - Use `Scaffold` and consume `innerPadding`:
    ```kotlin
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding),
            contentPadding = innerPadding
        ) { /* items */ }
    }
    ```
  - For overlays or bottom sheets, apply `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.
  - Never hardcode fixed status bar or navigation bar padding in dp.

---

## 8. Adaptive & Multi-Device Layouts
* Support phone, foldable, and tablet screen sizes gracefully using flexible containers (`BoxWithConstraints`, `LazyVerticalGrid`, responsive weight distributions).
* **RTL Layout Direction**: Always use `Arrangement.Start`/`End`, `Alignment.Start`/`End`, and `start`/`end` paddings.
* Use `LocalLayoutDirection.current` to derive direction-aware offsets, animation slide directions (e.g. `slideInHorizontally { sign * it }`), and directional icon flipping (arrows/navigation).

---

## 9. Framework & Library Guardrails
* **No Alpha Compose Styles**: The experimental Compose Styles API (`androidx.compose.foundation.style`) is unsupported due to our dependency stability policy (`commonMain` requires stable/beta only) and lack of Material 3 support.
