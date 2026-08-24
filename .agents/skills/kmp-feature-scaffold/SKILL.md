---
name: kmp-feature-scaffold
description: >-
  Step-by-step runbook to scaffold a complete new feature in a KMP project
  following Clean Architecture + MVVM. Covers domain model, repository interface,
  UiState, ViewModel with explicit actions, Screen container split, Koin DI module, navigation, and tests.
  Use when adding any new screen or feature to a KMP Compose Multiplatform project.
---

# KMP Feature Scaffold Runbook

Use this runbook whenever you add a new feature to a KMP Compose Multiplatform project.
Follow the steps **in order** — each layer depends on the one before it.

---

## Step 1: Domain Model

Create `shared/src/commonMain/kotlin/com/[pkg]/domain/model/[Feature].kt`

```kotlin
@Serializable
data class MyFeatureData(
    val id: String,
    val value: String,
    // ...
)
```

Rules:
- Pure Kotlin — zero platform or Compose imports
- `@Serializable` if the model is persisted or sent over a network
- Use `data class` for entities, `sealed class`/`enum class` for state variants

---

## Step 2: Repository Interface

Create `domain/repository/[Feature]Repository.kt`

```kotlin
interface MyFeatureRepository {
    fun dataFlow(): Flow<List<MyFeatureData>>
    suspend fun getById(id: String): Result<MyFeatureData>
    suspend fun save(data: MyFeatureData): Result<Unit>
}
```

Rules:
- All async methods return `Result<T>` or `Flow<T>`
- Platform-neutral — no Android/iOS types
- Interface only — no implementation here

---

## Step 3: Data Implementation

Create `data/repository/[Feature]RepositoryImpl.kt`

```kotlin
class MyFeatureRepositoryImpl(
    private val dataSource: MyFeatureDataSource,
) : MyFeatureRepository {
    override fun dataFlow(): Flow<List<MyFeatureData>> = dataSource.observeAll()
    override suspend fun getById(id: String): Result<MyFeatureData> = runCatching {
        dataSource.findById(id) ?: error("Not found: $id")
    }
    override suspend fun save(data: MyFeatureData): Result<Unit> = runCatching {
        dataSource.upsert(data)
    }
}
```

---

## Step 4: Presentation UiState & UiEffect

Create `presentation/[feature]/[Feature]UiState.kt`

```kotlin
@Immutable
data class MyFeatureUiState(
    val items: List<MyFeatureData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MyFeatureUiEffect {
    data class NavigateToDetail(val id: String) : MyFeatureUiEffect
    data class ShowError(val message: String) : MyFeatureUiEffect
}
```

---

## Step 5: ViewModel

Create `presentation/[feature]/[Feature]ViewModel.kt`

```kotlin
class MyFeatureViewModel(
    private val repository: MyFeatureRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyFeatureUiState())
    val uiState: StateFlow<MyFeatureUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MyFeatureUiEffect>(extraBufferCapacity = 16)
    val effect: SharedFlow<MyFeatureUiEffect> = _effect.asSharedFlow()

    init {
        loadData()
    }

    fun onRefresh() {
        loadData()
    }

    fun onItemClicked(id: String) {
        viewModelScope.launch {
            _effect.emit(MyFeatureUiEffect.NavigateToDetail(id))
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.dataFlow()
                .catch { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { items -> _uiState.update { it.copy(items = items, isLoading = false) } }
        }
    }
}
```

---

## Step 6: Screen Container & Content Split

Create `presentation/[feature]/[Feature]Screen.kt`

```kotlin
@Composable
fun MyFeatureScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyFeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MyFeatureUiEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is MyFeatureUiEffect.ShowError -> { /* show snackbar */ }
            }
        }
    }

    MyFeatureContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onItemClicked = viewModel::onItemClicked,
        modifier = modifier,
    )
}

@Composable
private fun MyFeatureContent(
    uiState: MyFeatureUiState,
    onRefresh: () -> Unit,
    onItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pure UI — no business logic
}
```

---

## Step 7: Koin DI Module

Create `di/[Feature]Module.kt`

```kotlin
val myFeatureModule = module {
    single<MyFeatureRepository> { MyFeatureRepositoryImpl(get()) }
    viewModel { MyFeatureViewModel(get()) }
}
```

Register in your root Koin setup:
```kotlin
startKoin {
    modules(myFeatureModule, /* other modules */)
}
```

---

## Step 8: Navigation Route

Add to `presentation/navigation/AppNavigation.kt`:

```kotlin
sealed interface Screen {
    // ... existing screens
    data object MyFeature : Screen
}

// In routing when block:
is Screen.MyFeature -> MyFeatureScreen(
    onNavigateToDetail = { id -> currentScreen = Screen.Detail(id) }
)
```

---

## Step 9: Tests

Create `commonTest/kotlin/com/[pkg]/presentation/[feature]/[Feature]ViewModelTest.kt`

```kotlin
class MyFeatureViewModelTest {

    private val fakeRepository = FakeMyFeatureRepository()
    private lateinit var viewModel: MyFeatureViewModel

    @BeforeTest
    fun setup() {
        viewModel = MyFeatureViewModel(fakeRepository)
    }

    @Test
    fun myFeatureViewModel_onLoad_emitsItemsFromRepository() = runTest {
        fakeRepository.items = listOf(MyFeatureData(id = "1", value = "test"))
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.items.size)
        }
    }
}
```

---

## Checklist Before Marking Feature Complete

- [ ] Domain model created in `domain/model/`
- [ ] Repository interface in `domain/repository/`
- [ ] Repository impl in `data/repository/`
- [ ] `@Immutable MyFeatureUiState` and `MyFeatureUiEffect` created
- [ ] ViewModel using `viewModelScope`, exposes explicit action functions and `StateFlow`/`SharedFlow`
- [ ] Screen composable split into Container and pure stateless Content
- [ ] Koin module registered
- [ ] Navigation route added
- [ ] ViewModel test with happy path, error path, and effect tests
