---
name: kmp-mvvm-implementation
description: >-
  Standard MVVM (Model-View-ViewModel) pattern implementation guide for KMP Compose Multiplatform.
  Includes code templates for UiState, UiEffect, ViewModel with explicit action methods and UseCases,
  Screen/Content container separation, and Turbine unit testing. Use when implementing or reviewing
  MVVM architecture in any KMP project.
---

# KMP MVVM Architecture Guide

---

## Architecture Flow

```
User Action ──▶ ViewModel.onAction() ──▶ UseCase.invoke() ──▶ Repository
                      │
                      ├── updates _uiState (StateFlow<UiState>) ──▶ ScreenContent
                      └── emits _uiEffects (SharedFlow<UiEffect>) ──▶ LaunchedEffect in ScreenContainer
```

---

## Component 1: UiState & UiEffect

Define your screen's UI State and optional one-shot UI Effects in `presentation/screens/[feature]/[Feature]UiState.kt` and `[Feature]UiEffect.kt`.

```kotlin
// presentation/screens/[feature]/[Feature]UiState.kt
package com.aslmmovic.qurancompanion.presentation.screens.feature

import com.aslmmovic.qurancompanion.domain.model.FeatureItem

/**
 * Single source of truth for screen state.
 * Flat data classes with primitive properties are inferred stable automatically.
 * Under Strong Skipping Mode (Kotlin 2.0+), do NOT add @Immutable when holding collections (List),
 * as O(N) structural equals() degrades scrolling performance compared to O(1) instance equality (===).
 */
data class FeatureUiState(
    val items: List<FeatureItem> = emptyList(),
    val selectedItemId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
```

```kotlin
// presentation/screens/[feature]/[Feature]UiEffect.kt
package com.aslmmovic.qurancompanion.presentation.screens.feature

/**
 * One-shot side effects (navigation, snackbars, sound/haptic triggers).
 * Emitted via SharedFlow and consumed once in LaunchedEffect.
 */
sealed interface FeatureUiEffect {
    data class NavigateToDetail(val id: String) : FeatureUiEffect
    data class ShowSnackbar(val message: String) : FeatureUiEffect
}
```

---

## Component 2: ViewModel

The ViewModel exposes the `UiState` via `StateFlow` and single-shot side effects via `SharedFlow<*UiEffect>`. ViewModels **must consume UseCases**, never repositories directly.

```kotlin
// presentation/screens/[feature]/[Feature]ViewModel.kt
package com.aslmmovic.qurancompanion.presentation.screens.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.usecase.GetFeatureItemsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeatureViewModel(
    private val getFeatureItemsUseCase: GetFeatureItemsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<FeatureUiEffect>(extraBufferCapacity = 16)
    val uiEffects: SharedFlow<FeatureUiEffect> = _uiEffects.asSharedFlow()

    init {
        loadItems()
    }

    // -- Explicit Public Action Methods --

    fun onRefresh() {
        loadItems()
    }

    fun onItemClicked(itemId: String) {
        _uiState.update { it.copy(selectedItemId = itemId) }
        viewModelScope.launch {
            _uiEffects.emit(FeatureUiEffect.NavigateToDetail(itemId))
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getFeatureItemsUseCase()
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
                .collect { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
        }
    }
}
```

---

## Component 3: Screen Container & Content Split

Separate the top-level **Screen Container** (handles ViewModel injection & lifecycle side-effects) from the **Content Composable** (pure rendering).

```kotlin
// presentation/screens/[feature]/[Feature]Screen.kt
package com.aslmmovic.qurancompanion.presentation.screens.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen Container Composable.
 * Stateful: Injects ViewModel via Koin, collects state, handles one-shot effects.
 */
@Composable
fun FeatureScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-shot side effects
    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is FeatureUiEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is FeatureUiEffect.ShowSnackbar -> { /* show snackbar */ }
            }
        }
    }

    FeatureContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onItemClicked = viewModel::onItemClicked,
        onDismissError = viewModel::onDismissError,
        modifier = modifier,
    )
}

/**
 * Content Composable.
 * Pure rendering — accepts UiState snapshot and explicit lambda callbacks.
 */
@Composable
fun FeatureContent(
    uiState: FeatureUiState,
    onRefresh: () -> Unit,
    onItemClicked: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pure UI arrangement using child sub-composables
}
```

---

## Testing MVVM ViewModels

Test state transitions and effect emissions using `Turbine` in `commonTest/`.

```kotlin
// commonTest/kotlin/com/aslmmovic/qurancompanion/presentation/screens/feature/FeatureViewModelTest.kt
package com.aslmmovic.qurancompanion.presentation.screens.feature

import app.cash.turbine.test
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class FeatureViewModelTest {

    private val fakeUseCase = FakeGetFeatureItemsUseCase()
    private lateinit var viewModel: FeatureViewModel

    @BeforeTest
    fun setup() {
        viewModel = FeatureViewModel(fakeUseCase)
    }

    @Test
    fun featureViewModel_onItemClicked_updatesSelectedIdAndEmitsEffect() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(null, initial.selectedItemId)

            viewModel.onItemClicked("item-123")
            val updated = awaitItem()
            assertEquals("item-123", updated.selectedItemId)
        }
    }

    @Test
    fun featureViewModel_onItemClicked_emitsNavigationEffect() = runTest {
        viewModel.uiEffects.test {
            viewModel.onItemClicked("item-123")
            val effect = awaitItem()
            assertEquals(FeatureUiEffect.NavigateToDetail("item-123"), effect)
        }
    }
}
```

---

## Key Rules Summary

1. **State Ownership**: ViewModel owns state via `MutableStateFlow`, exposes read-only `StateFlow<UiState>`.
2. **Consume Use Cases**: ViewModels must never directly inject Repositories.
3. **Explicit Actions**: ViewModel exposes explicit named functions for user actions (e.g. `onItemClicked()`), avoiding monolithic intent switch blocks.
4. **Strong Skipping Stability**: Do NOT add `@Immutable` on classes with collection properties (`List`, `Set`, `Map`).
5. **Single-Shot Side Effects**: Name them `*UiEffect` and expose via `uiEffects: SharedFlow<*UiEffect>`.
6. **Stateless Content**: Content composables accept `UiState` snapshot + lambda callbacks `() -> Unit` and contain zero business logic.
