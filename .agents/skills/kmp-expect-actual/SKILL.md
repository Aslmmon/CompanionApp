---
name: kmp-expect-actual
version: 1.0.0
description: Guide for implementing expect/actual platform bridges in KMP projects.
tags: [kmp, expect, actual, platform, bridge]
applies_to: [commonMain, androidMain, iosMain]
---

> [!NOTE]
> **Status: Dormant** — No `expect`/`actual` declarations beyond `Platform.kt` currently exist in this project.
> Activate this skill only when adding new hardware APIs (audio, haptics, sensors, permissions).
> Adapt examples to Sahaba Companions domain before using — current examples use generic sensor patterns.

# KMP expect/actual Platform Bridge Guide

Use this skill when you need to expose platform-specific behaviour (hardware APIs, OS APIs, permissions) to `commonMain` via the `expect`/`actual` mechanism.

---

## 1. When to Use expect/actual

Use `expect`/`actual` only when:
- A feature requires a **platform-native API** unavailable in `commonMain` (e.g. audio session management on iOS, `SensorManager` on Android).
- You cannot achieve the same result through a Kotlin Multiplatform library (e.g. `kotlinx-coroutines`, `ktor`).
- The abstraction boundary is stable and unlikely to change across platforms.

Do **not** use `expect`/`actual` for:
- Business logic that belongs in `domain` or `data` layers.
- Anything achievable with Compose Multiplatform or KMP stdlib.

---

## 2. File Structure

```
shared/src/
  commonMain/kotlin/com/qurancompanion/shared/platform/
    PlatformApi.kt          ← expect declaration
  androidMain/kotlin/com/qurancompanion/shared/platform/
    PlatformApi.android.kt  ← actual for Android
  iosMain/kotlin/com/qurancompanion/shared/platform/
    PlatformApi.ios.kt      ← actual for iOS
```

---

## 3. Declaration Pattern

**commonMain** — declare the interface contract:
```kotlin
// PlatformApi.kt
package com.qurancompanion.shared.platform

expect class PlatformApi() {
    fun doSomething(): String
}
```

**androidMain** — implement with Android APIs:
```kotlin
// PlatformApi.android.kt
package com.qurancompanion.shared.platform

import android.os.Build

actual class PlatformApi actual constructor() {
    actual fun doSomething(): String = "Android ${Build.VERSION.SDK_INT}"
}
```

**iosMain** — implement with iOS APIs:
```kotlin
// PlatformApi.ios.kt
package com.qurancompanion.shared.platform

import platform.UIKit.UIDevice

actual class PlatformApi actual constructor() {
    actual fun doSomething(): String = UIDevice.currentDevice.systemVersion
}
```

---

## 4. Inject via Koin

Register the platform implementation in the platform-specific Koin module:

```kotlin
// androidMain — platformModule.android.kt
val platformModule = module {
    single { PlatformApi() }
}

// iosMain — platformModule.ios.kt
val platformModule = module {
    single { PlatformApi() }
}
```

Consume in `commonMain` through dependency injection — never instantiate directly.

---

## 5. Testing

- Test the `expect` contract with a fake in `commonTest`.
- Platform-specific behaviour is tested in `androidUnitTest` / `iosTest` source sets.
- Never import platform types in `commonTest`.
