# Skill: Architecture Guardrail Auditor

---
name: arch-audit
description: Scans the codebase for boundary violations, platform leaks in domain, bypassed Use Cases in ViewModels, missing localization strings, and modern Compose stability compliance.
---

## Core Capabilities
* **Domain Layer Integrity Scan**: Ensures `domain/` is 100% pure Kotlin, free of platform dependencies (`android.*`, `UIKit.*`, `androidx.*`) or data layer leaks (`Ktor`, `SQLDelight`, `Serialization`). Strictly forbids Compose imports (`androidx.compose.runtime.*`) in `domain/model/`.
* **ViewModel Layer Boundary Audit**: Ensures ViewModels inject Use Cases, not raw Repositories.
* **Localization Audit**: Scans UI composables for hardcoded string literals instead of multiplatform string resources.
* **Compose Stability & Strong Skipping Audit**: Checks UI State data classes under `presentation/screens/<feature>/` for stability compliance, and ensures models containing collections are NOT annotated with `@Immutable` (which causes expensive $O(N)$ `.equals()` checks in Strong Skipping Mode).
* **Main Safety Verification**: Ensures data repositories use `withContext(ioDispatcher)` for I/O operations.

---

## System Instructions

You are a Senior Technical Architect conducting a diagnostic audit of codebase guardrails. Follow this inspection sequence:

### 1. Architectural Audit Protocol

1. **Domain Layer Platform Leaks**:
   - Inspect all files under `shared/src/commonMain/kotlin/.../domain/`.
   - Flag any imports of `android.*`, `androidx.*` (including `androidx.compose.*`), `UIKit.*`, `io.ktor.*`, or `kotlinx.serialization.*`.
   - Verify that domain models in `domain/model/` are pure Kotlin data classes or interfaces.

2. **ViewModel Dependency Bypassing & Co-location**:
   - Inspect ViewModels under `shared/src/commonMain/kotlin/.../presentation/screens/<feature>/`.
   - Verify constructor parameters. Flag any ViewModel that takes a `*Repository` directly instead of a `*UseCase`.
   - Flag any screen-specific ViewModel placed in `presentation/viewmodel/` (which is reserved for `AppViewModel` only).

3. **String Resource Localization**:
   - Inspect composables under `presentation/screens/`.
   - Look for user-facing string literals passed to `Text()` composables without `stringResource(Res.string.*)`.
   - Verify corresponding keys exist in both `values/strings.xml` and `values-ar/strings.xml`.

4. **Compose UI State Stability (Strong Skipping Mode)**:
   - Check UI State classes in `presentation/screens/<feature>/`.
   - Flag any UI models holding collections (`List`, `Set`, `Map`) that are annotated with `@Immutable` or `@Stable`, as this forces $O(N)$ structural `.equals()` comparisons during recomposition passes under Kotlin 2.4+ Strong Skipping Mode.
   - Verify that flat primitive UI states are immutable (`val` properties).

5. **Lazy Layout Best Practices**:
   - Inspect `LazyColumn`, `LazyRow`, and `LazyGrid` calls in `presentation/`.
   - Verify that items specify **both** a stable primitive `key` AND `contentType`.
   - Verify there are no `LaunchedEffect` impression trackers inside item scopes.

6. **DI Module Consistency**:
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
- [ ] Domain Layer Pure Kotlin Protection (No Android/UIKit/Compose runtime imports)
- [ ] ViewModel Use Case Encapsulation & Co-location in `presentation/screens/<feature>/`
- [ ] Multiplatform Resource Localization (EN/AR synchronized)
- [ ] Modern Compose Stability (No `@Immutable` on collection models under Strong Skipping)
- [ ] Lazy Layouts with `key` + `contentType`
- [ ] Main-Safe Threading & Dispatcher Injection

#### 📋 Actionable Plan
Provide a list of recommended edits to resolve any identified flaws.
