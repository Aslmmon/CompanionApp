# Fix Infinite Recursion in SystemDateTimeProvider

The `SystemDateTimeProvider` class in `shared` module has a recursive call in `getCurrentDayOfYear()` and `getCurrentDayOfWeek()` because the member functions shadow the imported top-level functions with the same names.

## Proposed Changes

### [shared]

#### [MODIFY] [SystemDateTimeProvider.kt](file:///home/amohamed/AndroidStudioProjects/QuranCompanion/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/util/SystemDateTimeProvider.kt)

Use aliases for the imported top-level functions to avoid shadowing and infinite recursion.

## Verification Plan

### Automated Tests
- Run existing tests to ensure no regressions.
- Ideally, add a unit test for `SystemDateTimeProvider` if possible, although it relies on platform-specific `expect`/`actual` functions which might be hard to test in `commonTest` without mocking the top-level functions (which isn't easily possible in Kotlin). However, just ensuring it compiles and the recursion is gone is the primary goal.

### Manual Verification
- Deploy the app and verify it doesn't crash on startup or when these methods are called.
