---
trigger: always_on
---

# KMP Build Configuration Standards

## Version Catalog (libs.versions.toml)

- **ALL** dependencies in `gradle/libs.versions.toml`. Zero hardcoded version strings anywhere in build files.
- Groups must be in order: `[versions]` → `[libraries]` → `[plugins]`, each alphabetically sorted.
- Plugin aliases: `alias(libs.plugins.xxx)` — never `id("xxx") version "y.z"`

## Release Build (MANDATORY for every Android target)

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true       // REQUIRED — never false in production
        isShrinkResources = true     // REQUIRED alongside minification
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

## proguard-rules.pro Requirements

Every project using RevenueCat MUST include:
```proguard
-keep class com.revenuecat.** { *; }
```

Every project with kotlinx.serialization MUST include:
```proguard
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepattributes *Annotation*, Signature, Exception
```

## Dependency Stability Policy

- `commonMain` dependencies: **stable or beta ONLY**. No alpha.
- RC or alpha allowed only with explicit comment:
  ```toml
  # ALPHA: waiting for stable, tracking https://issuetracker.google.com/XXX
  navigationCompose = "2.8.0-alpha10"
  ```

## Minimum SDK Policy

- New KMP projects: `minSdk = 26` minimum (Android 8.0, API 26).
- `targetSdk` and `compileSdk` always on the latest **stable** Android API level.
- Justify any `minSdk` below 26 with a comment.

## Required Gradle Properties

Every KMP project root `gradle.properties` must contain:
```properties
org.gradle.configuration-cache=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
kotlin.code.style=official
android.nonTransitiveRClass=true
android.useAndroidX=true
kotlin.daemon.jvmargs=-Xmx3072M
```

## Module Structure

- Shared KMP logic: `:shared` module (library, `com.android.kotlin.multiplatform.library`)
- Android app: `:androidApp` module (application, `com.android.application`)
- `:androidApp` depends on `:shared` only — no direct dependency on shared internals.
