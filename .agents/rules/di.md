# Dependency Injection Guidelines (Koin)

These guidelines define the dependency injection rules using the Koin framework.

---

## 1. Module Declarations
* Register all common dependencies (Use Cases, Repositories, common Data Sources) in `di/AppModule.kt` (`appModule`).
* Register platform-specific overrides (like databases, key-value stores, activity contexts) in `di/AndroidModule.kt` (`androidModule`) and `di/IosModule.kt` (`iosModule`).

---

## 2. Retrieval Rules
* **ViewModels**: Retrieve ViewModels inside Composable screens using `koinViewModel()`.
* **Constructor Injection**: Use constructor injection for all non-UI layers (Data Sources, Repositories, Use Cases). Do not manually instantiate them or retrieve them from a global DI container within their definitions.
* **Top-Level Retrieval**: Use `koinInject()` only in top-level entry composables like `App()` or within platform entry points (like `MainActivity`). Do not inject dependencies using `koinInject()` or `get()` directly inside lower-level composables.
