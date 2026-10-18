# Testing Architecture & Guidelines

These guidelines define the project's testing principles, tools, and execution workflows.

---

## 1. Test Structure & Pure Kotlin Testing
* Write all unit tests under the `commonTest` source set using pure Kotlin `kotlin.test` (avoiding platform-specific test libraries or runners).
* Verify ViewModels by passing fakes into UseCases and injecting those UseCases into ViewModels.
* **TDD Subagent Production**: For all feature development and bug fixes, follow the [Unified TDD & SDD Workflow Guide & Skill](file:///.agents/skills/tdd-agent/SKILL.md) — the primary agent acts as orchestrator to formulate the plan/spec (requiring user approval first), then produces a dedicated subagent to execute the RED -> GREEN -> REFACTOR inner loop autonomously.

---

## 2. Coroutine Schedulers in Tests
* Test suspend functions utilizing `runTest` and `StandardTestDispatcher()`.
* **Scheduler Synchronization**: To prevent `Detected use of different schedulers` exceptions, bind the test dispatcher and repository/usecase instantiation to the same `testScheduler` (e.g. by passing the test scope's scheduler or creating dependencies dynamically inside the test body).
* Call `advanceUntilIdle()` to execute pending coroutines or flow collections before making assertions.
* When testing ViewModels, configure `Dispatchers.setMain(testDispatcher)` in `@BeforeTest` and `Dispatchers.resetMain()` in `@AfterTest`.

---

## 3. Flow & State Assertions with Turbine
* Use `app.cash.turbine:turbine` for `Flow`, `StateFlow`, and `SharedFlow<*UiEffect>` assertions:
  ```kotlin
  viewModel.uiState.test {
      val initial = awaitItem()
      assertTrue(initial.isLoading)
      val loaded = awaitItem()
      assertFalse(loaded.isLoading)
  }
  ```

---

## 4. Test Method Naming Convention
Follow the standard pattern: `[Subject]_[condition]_[expectedResult]`
```kotlin
homeViewModel_onRefresh_emitsLoadedState        // ✅
homeViewModel_onItemClicked_emitsNavigateEffect // ✅
testRefresh                                     // ❌ too vague
```

---

## 5. Fakes-Over-Mocks Philosophy & Testing Setup Guard
* **Strict Prohibition of Mocking Libraries**: Do NOT use mock frameworks (such as MockK, Mockito, or PowerMock) in `commonTest`.
* **Guard Against Android Testing Skills**: Never use Android CLI skills (such as `testing-setup`) to introduce MockK, Mockito, Hilt, or Robolectric into `shared/commonTest`.
* **In-Memory Fakes**: Write simple, in-memory double classes implementing repository or datasource interfaces (e.g., `FakeJourneyRepository`) under `commonTest`.
* Store mutable states (like lists or maps) inside fakes so tests can easily configure state scenarios.

---

## 6. Deterministic Testing
* Use `Random(seed = 42)` for any test requiring randomized inputs.
* Constants referenced in tests must use actual constants from source code.

---

## 7. Minimum Test Suite Per Feature
Every feature test file must cover:
- [ ] Happy path: normal operation and successful state emission
- [ ] Error path: repository/network failure and error state emission
- [ ] Edge cases: empty state, boundary values
- [ ] State transitions: verify each action produces expected state change
- [ ] One-shot effects: verify correct `UiEffect` emission via Turbine
