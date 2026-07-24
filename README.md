# Quran Companion

![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin%20Multiplatform-1.9.0-blue)
![Koin](https://img.shields.io/badge/DI-Koin-3.5.0-brightgreen)

## 🎯 Project Overview

**Quran Companion** is a Kotlin Multiplatform project that provides a modern, cross‑platform Quran experience.  The codebase targets **Android** and **iOS** while sharing the majority of the logic in the `shared` module.

- **Android** – Fully Jetpack‑Compose based UI.
- **iOS** – SwiftUI entry point with shared Compose UI for business logic.
- **Shared** – Common Kotlin code (UI, ViewModels, domain, data) compiled for both platforms.

## 🏗️ Architecture

| Layer            | Responsibility                                                                                 |
|------------------|-------------------------------------------------------------------------------------------------|
| **Presentation**| Jetpack Compose / SwiftUI screens, ViewModels (`OnboardingViewModel`).                         |
| **Navigation**   | Centralised `NavigationManager` exposing a `StateFlow<Screen>` for reactive navigation.        |
| **Domain**       | Use‑cases (`GetUserPreferencesUseCase`, `SavePreferencesUseCase`).                             |
| **Data**         | Repository implementation (`UserPreferencesRepositoryImpl`) backed by platform‑specific storage. |
| **DI**           | Koin modules (`appModule`, `navigationModule`).                                                 |

### Navigation

Navigation is handled by `NavigationManager` – a lightweight singleton that provides:
```kotlin
val currentScreen: StateFlow<Screen>
fun navigateTo(screen: Screen)
```
All ViewModels inject this manager via Koin, removing direct screen‑state handling.

## 📦 Dependency Injection (Koin)

The project uses **Koin 3.5** for DI.
```kotlin
val appModule = module {
    // Data
    single { UserPreferencesRepositoryImpl(get()) } bind UserPreferencesRepository::class

    // Domain
    single { GetUserPreferencesUseCase(get()) }
    single { SavePreferencesUseCase(get()) }

    // Navigation
    single { NavigationManager() }

    // Presentation
    viewModel { OnboardingViewModel(get(), get(), get()) }
}
```
The `MainActivity` starts Koin:
```kotlin
startKoin {
    androidContext(this@MainActivity)
    modules(appModule)
}
```

## 📁 Repository Structure

```
QuranCompanion/
├─ androidApp/           # Android application module
├─ iosApp/               # iOS Xcode project
├─ shared/               # Multiplatform shared code
│   ├─ src/commonMain/kotlin/
│   │   ├─ presentation/      # UI, ViewModels, NavigationManager
│   │   ├─ domain/            # Use‑cases, repository interfaces
│   │   ├─ data/              # Repository implementations
│   │   └─ di/                # Koin modules (AppModule, NavigationModule)
│   └─ src/...               # Platform‑specific source sets (androidMain, iosMain, jvmMain…)
└─ README.md               # This file
```

## ⚙️ Building & Running

### Android
```bash
# Assemble debug APK
./gradlew :androidApp:assembleDebug

# Run directly from Android Studio (Run > Run 'app')
```

### iOS
1. Open the `iosApp` folder in Xcode.
2. Select a simulator/device and press **Run**.

### Shared Unit Tests
```bash
# Android host tests
./gradlew :shared:testAndroidHostTest

# iOS simulator tests
./gradlew :shared:iosSimulatorArm64Test
```

## 🧪 Testing Strategy

- **Unit tests** cover domain use‑cases and repository logic.
- **UI tests** (Compose) validate onboarding flow and navigation state.
- **Integration tests** ensure Koin starts correctly and provides all dependencies.

## 🤝 Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/awesome‑feature`).
3. Write tests for new functionality.
4. Open a Pull Request describing the change.

## 📚 Resources
- Kotlin Multiplatform docs: <https://kotlinlang.org/docs/multiplatform.html>
- Koin DI guide: <https://insert-koin.io/docs/reference/koin-android/>
- Jetpack Compose: <https://developer.android.com/jetpack/compose>
- SwiftUI: <https://developer.apple.com/xcode/swiftui/>

---

*Happy coding!*