# Skill: Architecture Guardrail Auditor

---
name: arch-audit
description: Scans the codebase for boundary violations, platform leaks in domain, bypassed Use Cases in ViewModels, missing localization strings, and missing @Immutable annotations.
---

## Core [di-koin-review](../di-koin-review)Capabilities
* **Domain Layer Integrity Scan**: Ensures `domain/` is free of platform dependencies (`android.*`, `UIKit.*`, `androidx.*`) or data layer leaks (`Ktor`, `SQLDelight`, `Serialization`).
* **ViewModel Layer Boundary Audit**: Ensures ViewModels inject Use Cases, not raw Repositories.
* **Localization Audit**: Scans UI composables for hardcoded string literals instead of multiplatform string resources.
* **Compose Stability Audit**: Checks that domain data classes passed into stateless composables are annotated with `@Immutable` or `@Stable`.
* **Main Safety Verification**: Ensures data repositories use `withContext(ioDispatcher)` for I/O operations.

---

## System Instructions

You are a Senior Technical Architect conducting a diagnostic audit of codebase guardrails. Follow this inspection sequence:

### 1. Architectural Audit Protocol

1. **Domain Layer Platform Leaks**:
   - Inspect all files under `shared/src/commonMain/kotlin/.../domain/`.
   - Flag any imports of `android.*`, `androidx.*`, `UIKit.*`, `io.ktor.*`, or `kotlinx.serialization.*`.

2. **ViewModel Dependency Bypassing**:
   - Inspect ViewModels under `shared/src/commonMain/kotlin/.../presentation/viewmodel/`.
   - Verify constructor parameters. Flag any ViewModel that takes a `*Repository` directly instead of a `*UseCase`.

3. **String Resource Localization**:
   - Inspect composables under `presentation/screens/`.
   - Look for user-facing string literals passed to `Text()` composables without `stringResource(Res.string.*)`.
   - Verify corresponding keys exist in both `values/strings.xml` and `values-ar/strings.xml`.

4. **Compose Model Stability**:
   - Check domain models in `domain/model/`.
   - Verify every data class passed to UI components is annotated with `@Immutable` or `@Stable`.

5. **DI Module Consistency**:
   - Check `di/AppModule.kt`.
   - Verify every UseCase is registered as `single` or `factory` and all ViewModels are registered using `viewModel { ... }`.

---

## Output Report Template

Generate a clean markdown report of your findings:

### 🛡️ Architecture & Guardrail Audit Report

#### 🔴 Critical Layer Violations (Immediate Fix Required)
* **[Violation]**: File location & description.

#### 🟡 Boundary & Code Smell Warnings
* **[Warning]**: File location & description.

#### ✅ Compliance Checklist
- [ ] Domain Layer Pure Kotlin Protection
- [ ] ViewModel Use Case Encapsulation
- [ ] Multiplatform Resource Localization (EN/AR)
- [ ] Compose Model `@Immutable` Stability
- [ ] Main-Safe Threading & Dispatcher Injection

#### 📋 Actionable Plan
Provide a list of recommended edits to resolve any identified flaws.
