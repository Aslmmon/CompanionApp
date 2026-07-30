# Compose Multiplatform UI Rules

These guidelines define UI development and Jetpack Compose standards for the project.

---

## 1. Screen-Content Pattern
Every screen implementation must be split into two composable structures:
1. **Stateful Screen Wrapper (`*Screen`)**:
   - Collects ViewState flows from the ViewModel using lifecycle-aware collection (e.g., `collectAsStateWithLifecycle()`).
   - Resolves and binds UI event handler lambdas.
   - Triggers one-shot UI events (like navigation or toast triggers).
2. **Stateless Screen Content (`*Content`)**:
   - Receives plain state representations and action lambda callbacks.
   - Must be preview-friendly, fully independent of ViewModels, and easy to test.

---

## 2. Localization
* Retrieve string values via `stringResource(Res.string.<id>)` from the generated Multiplatform resources.
* **No Hardcoded User-Facing Text**: Any new text presented to the user must be added to the localization resource files:
  - English strings: `shared/src/commonMain/composeResources/values/strings.xml`
  - Arabic translations: `shared/src/commonMain/composeResources/values-ar/strings.xml`

---

## 3. Arabic Typography & Theme
* Apply theme values directly from `MaterialTheme.colorScheme` and `MaterialTheme.typography` (do not hardcode raw colors or text styles).
* For Arabic or Quranic text, use `QuranArabicTextStyle` from `ui/theme/Type.kt` to accommodate tashkeel (vowel markings) line heights and font features.

---

## 4. Compose Compiler Stability
* Annotate domain models passed to stateless composables with `@Immutable` or `@Stable`.
* **Collection Stability**: Avoid passing raw Kotlin `List` collections directly to stateless composables if they change frequently; prefer wrapping them in a stable wrapper or using `kotlinx.collections.immutable`.
* **Stable Keys**: Always use stable keys in lazy layouts (`LazyColumn`, `LazyRow`).

---

## 5. Adaptive & Multi-Device Layouts
* Support phone, foldable, and tablet screen sizes gracefully.
* Use flexible layout containers (`BoxWithConstraints`, `LazyVerticalGrid`, or responsive weight distributions) for content cards.
* Enforce dynamic layout direction handling: use `LocalLayoutDirection.current` to derive direction-aware offsets, animation slide directions (e.g. `slideInHorizontally { sign * it }`), and icon mirroring for RTL (Arabic) vs LTR (English).

