# Testing Architecture & Guidelines

These guidelines define the project's testing principles.

---

## 1. Test Structure
* Write unit tests under the `commonTest` source set using the pure Kotlin `kotlin.test` library (avoiding platform-specific test libraries).
* Verify ViewModels by passing fakes into UseCases and injecting those UseCases into ViewModels.

---

## 2. Coroutine Schedulers in Tests
* Test suspend functions utilizing `runTest` and `StandardTestDispatcher()`.
* **Scheduler Synchronization**: To prevent `Detected use of different schedulers` exceptions, bind the test dispatcher and repository/usecase instantiation to the same `testScheduler` (e.g. by passing the test scope's scheduler or creating dependencies dynamically inside the test body).
* Call `advanceUntilIdle()` to execute pending coroutines or flow collections before making assertions.

---

## 3. Fakes-Over-Mocks Philosophy
* **No Mocking Libraries**: Do not use mocking frameworks (such as MockK, Mockito, or PowerMock).
* **In-Memory Fakes**: Write simple, in-memory double classes implementing repository or datasource interfaces (e.g., `FakeJourneyRepository`) under `commonTest`.
* Store mutable states (like lists or variables) inside fakes so tests can easily configure state conditions.
