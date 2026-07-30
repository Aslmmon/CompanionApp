# Architectural Layer Isolation & Data Flow Rules

These guidelines define the Clean Architecture standards for the project.

---

## 1. Layer Isolation
* The `:shared` module is structured into `data`, `domain`, and `presentation` packages.
* **Domain Layer Protection**: The `domain` layer must remain completely free of platform-specific imports (e.g. `android.*`, `UIKit.*`) and data layer/third-party library dependencies (such as Ktor, SQLDelight, or Serialization).
* Only standard Kotlin libraries are allowed in the `domain` layer (e.g., coroutines, collections).

---

## 2. Data Flow & DTO Mapping
* Repositories and Use Cases must only return pure Domain Models (located in `domain/model/`).
* **DTO Mapping**: Data Transfer Objects (DTOs), database entities, and API responses (e.g. `*Dto`) must remain private to their respective data source or repository implementations.
* Map DTOs to Domain Models inside the data layer using extension functions (typically `toDomain()`) before returning them to the domain layer.

---

## 3. Use Case Single Responsibility
* Encapsulate specific business logic workflows inside Use Case classes.
* Every Use Case should expose a single operational method:
  ```kotlin
  operator fun invoke(): Result<DomainModel>
  ```
* Avoid grouping multiple unrelated operations under a single Use Case.
* **Grouping Files**: Do not create one file per Use Case. Group related Use Cases into files named after their domain namespace (e.g., `JourneyUseCases.kt`, `OnboardingUseCases.kt`).

---

## 4. Platform Abstraction Interfaces in Domain
* Time, date, and other pure system utilities that have no platform SDK dependency (beyond the Kotlin standard library) belong in `domain/util/` as pure Kotlin interfaces.
  - Example: `DateTimeProvider` → `domain/util/DateTimeProvider.kt`
  - Example: `SystemDateTimeProvider` (commonMain impl) → `domain/util/SystemDateTimeProvider.kt`
* Platform-specific services that _require_ Android/iOS SDK (like locale management, shared preferences) belong as interfaces in `data/datasource/` with platform implementations in `androidMain`/`iosMain`.
  - Example: `LocaleProvider` → `data/datasource/LocaleProvider.kt`
  - Example implementations: `AndroidLocaleProvider`, `IosLocaleProvider`
* **Exception — LocaleProvider in ViewModels**: `LocaleProvider` is injected into ViewModels (`HomeViewModel`, `LanguageViewModel`) to call `changeLocale()` after saving preferences. This is a pragmatic exception to strict layer isolation, acceptable because there is no UI-facing domain abstraction for locale switching. Avoid creating new such exceptions; extract a proper use case if the logic grows.

---

## 5. Database & Persistence Isolation
* Databases, filesystems, and key-value stores (e.g. `KeyValueStorage`) are technical details of the data layer.
* Any database schemas, table adapters, or key structures must not be exposed outside of the `data/` source set.

* All `KeyValueStorage` keys must be defined as `private const val` inside the repository/class's `companion object`, never scattered as raw string literals.
  ```kotlin
  // ✅ Do
  companion object {
      private const val KEY_PREFERRED_LANGUAGE = "pref_preferred_language"
  }
  // ❌ Don't
  storage.putString("pref_preferred_language", value)
  ```

---

## 6. Error & Result Shielding
* Repositories and Use Cases must not propagate raw, platform/network-specific exceptions (e.g., `IOException`, `SerializationException`, `SqlException`) to ViewModels or the UI.
* Wrap data operations in a clean, domain-specific `Result` type or return safe fallbacks where appropriate.
