# Walkthrough - Fix Infinite Recursion in SystemDateTimeProvider

I have fixed an infinite recursion issue in `SystemDateTimeProvider.kt` where the member functions were calling themselves instead of the intended top-level platform functions.

## Changes

### [shared]

#### [SystemDateTimeProvider.kt](file:///home/amohamed/AndroidStudioProjects/QuranCompanion/shared/src/commonMain/kotlin/com/aslmmovic/qurancompanion/domain/util/SystemDateTimeProvider.kt)

Modified the file to use import aliases for `getCurrentDayOfYear` and `getCurrentDayOfWeek`. This disambiguates the calls within the class methods, ensuring they call the platform-specific implementations rather than recursing.

```kotlin
import com.aslmmovic.qurancompanion.getCurrentDayOfWeek as platformGetCurrentDayOfWeek
import com.aslmmovic.qurancompanion.getCurrentDayOfYear as platformGetCurrentDayOfYear

class SystemDateTimeProvider : DateTimeProvider {
    override fun getCurrentDayOfYear(): Int = platformGetCurrentDayOfYear()
    override fun getCurrentDayOfWeek(): Int = platformGetCurrentDayOfWeek()
}
```

## Verification Results

### Automated Tests
- Executed `:shared:allTests`.
- All tests passed (19 tests).

### Manual Verification
- The fix prevents the `StackOverflowError` reported in the crash log by breaking the infinite recursion loop.
