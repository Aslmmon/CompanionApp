# Quran Companion Developer & Agent Guidelines

This document provides a high-level overview of the project and serves as an index to our modular engineering guidelines.

---

## 1. Project Overview
This is a Kotlin Multiplatform (KMP) project utilizing Compose Multiplatform for shared UI across Android and iOS. The core logic is structured in the `:shared` module using a layered Clean Architecture:
* **data**: Data sources, DTOs, and Repository implementations.
* **domain**: Business logic (Domain Models, Repository Interfaces, Use Cases).
* **presentation**: ViewModels, Composable Screens, and Navigation.

---

## 2. Modular Guidelines Directory
Refer to these topic-specific guidelines for detailed rules, code conventions, and implementation instructions:
* 🏗️ **[Clean Architecture & Layer Isolation Guidelines](file:///.agents/rules/architecture.md)**
* 📱 **[Compose Multiplatform UI Guidelines](file:///.agents/rules/compose.md)**
* 🔌 **[Dependency Injection Guidelines (Koin)](file:///.agents/rules/di.md)**
* ⚡ **[Coroutines & Concurrency Guidelines](file:///.agents/rules/concurrency.md)**
* 🗺️ **[Navigation Architecture Guidelines](file:///.agents/rules/navigation.md)**
* 🧪 **[Testing Architecture & Guidelines](file:///.agents/rules/testing.md)**
* 📝 **[Naming Conventions Guidelines](file:///.agents/rules/naming_conventions.md)**

---

## 3. Core Developer Checklist

### Do
* **Recreate Host Activities**: Trigger full resource reloads (e.g. `activity.recreate()`) on dynamic configuration shifts such as language/locale switches.
* **Map Data Layers**: Fully map DTO/entity representations to domain models inside data layers using mapping extension functions (e.g., `toDomain()`) before returning.
* **Main Safety**: Always ensure all Use Cases and Repository interfaces are main-safe and run non-blocking.
* **Localization Resources**: Define all user-facing strings in Multiplatform XML resources (providing English in `values/strings.xml` and Arabic in `values-ar/strings.xml`).

### Don't
* **No Platform Imports in Domain**: Do not import Android SDK libraries (like `android.*`) or platform components in `commonMain` or the `domain` module.
* **No Repository Bypassing**: ViewModels must never directly consume Repositories; always encapsulate business logic in Use Cases.
* **No Raw Exceptions in UI**: Never propagate raw SQL, filesystem, or network exceptions up to ViewModels; handle exceptions at repository boundaries.
* **No Mocking Libraries**: Do not import mock frameworks (like Mockk or Mockito) in `commonTest`; write clean, in-memory fake classes instead.
* **No Unstable UI Params**: Avoid passing unstable collection types (such as raw, standard `List` objects) directly into stateless composables without stable keys or wrappers.
