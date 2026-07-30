# Coroutines & Concurrency Guidelines

These guidelines define the concurrency and coroutine management rules.

---

## 1. Main Safety
* All Use Cases and Repository interfaces must be **main-safe** (safe to call from the main thread without blocking the UI).

---

## 2. Background execution
* Data sources and repositories performing blocking I/O or intensive CPU tasks (such as parsing large JSON files, database queries, file writing) must offload execution using `withContext(ioDispatcher)` (injecting `Dispatchers.IO` or a multiplatform equivalent provider).
* When exposing flows, make sure to specify the upstream execution context using `.flowOn(ioDispatcher)` to guarantee background safety.

---

## 3. Scope & Dispatcher Injection
* **No Hardcoded Scopes**: Do not hardcode lifecycle scopes (like `CoroutineScope(Dispatchers.Main)`) or dispatchers (like `Dispatchers.IO`) inside data or domain layers.
* Always inject `CoroutineDispatcher` or `CoroutineScope` through constructor parameters to allow easy swapping with test doubles (e.g., `StandardTestDispatcher` / `UnconfinedTestDispatcher`) in unit tests.
