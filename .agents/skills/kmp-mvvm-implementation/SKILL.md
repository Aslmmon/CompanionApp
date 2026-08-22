---
name: kmp-mvvm-implementation
description: >-
  Standard MVVM (Model-View-ViewModel) pattern implementation guide for KMP Compose Multiplatform.
  Includes code templates for UiState, UiEffect/Event, ViewModel with explicit action methods,
  Screen/Content container separation, and Turbine unit testing. Use when implementing or
  reviewing MVVM architecture in any KMP project.
---

# MVVM Implementation Guide for KMP

MVVM in KMP Compose Multiplatform structures the presentation layer into three clean components:
1. **UiState**: An immutable snapshot of what the screen displays.
2. **ViewModel**: Manages business logic, holds state in a `StateFlow`, handles async work via `viewModelScope`, and exposes explicit action methods.
3. **Screen Container & Content**: Screen handles ViewModel injection and side-effect handling; Content renders UI statelessly.

---

## Architecture Diagram

```
User Action (Callback)
     │
     ▼
[Screen Container] ──Explicit Method Call──▶ [ViewModel]
         ▲                                       │
         │                                       ├──State──▶ StateFlow ──▶ [Screen Container] ──▶ [Content Composable]
         │                                       │
         └──Effect (one-shot)────────────────────┘ SharedFlow ──▶ [Screen LaunchedEffect]
```

---

## Component 1: UiState & UiEffect

Define your screen's UI State and optional one-shot UI Effects in `presentation/[feature]/[Feature]UiState.kt`.

```kotlin
// presentation/[feature]/[Feature]UiState.kt
package com.aerobounce.presentation.feature

import androidx.compose.runtime.Immutable
import com.aerobounce.domain.model.FeatureItem

/**
 * Single source of truth for screen state.
 * @Immutable allows Compose smart recomposition optimizations.
 */
@Immutable
data class FeatureUiState(
    val items: List<FeatureItem> = emptyList(),
    val selectedItemId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

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

The ViewModel exposes the `UiState` via `StateFlow` and single-shot events via `SharedFlow`. It exposes **explicit public functions** for each user interaction.

```kotlin
// presentation/[feature]/[Feature]ViewModel.kt
package com.aerobounce.presentation.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerobounce.domain.repository.FeatureRepository
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
    private val repository: FeatureRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FeatureUiEffect>(extraBufferCapacity = 16)
    val effect: SharedFlow<FeatureUiEffect> = _effect.asSharedFlow()

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
            _effect.emit(FeatureUiEffect.NavigateToDetail(itemId))
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getItemsFlow()
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
// presentation/[feature]/[Feature]Screen.kt
package com.aerobounce.presentation.feature

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
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
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
private fun FeatureContent(
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
// commonTest/kotlin/com/aerobounce/presentation/feature/FeatureViewModelTest.kt
package com.aerobounce.presentation.feature

import app.cash.turbine.test
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class FeatureViewModelTest {

    private val fakeRepository = FakeFeatureRepository()
    private lateinit var viewModel: FeatureViewModel

    @BeforeTest
    fun setup() {
        viewModel = FeatureViewModel(fakeRepository)
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
        viewModel.effect.test {
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
2. **Explicit Actions**: ViewModel exposes explicit named functions for user actions (e.g. `onItemClicked()`), avoiding monolithic intent switch blocks.
3. **Immutable State**: Annotate `UiState` with `@Immutable` or `@Stable`.
4. **Single-Shot Events**: Use `SharedFlow` with `extraBufferCapacity = 16` for one-shot UI effects (navigation, sounds, snackbars).
5. **Stateless Content**: Content composables accept `UiState` snapshot + lambda callbacks `() -> Unit` and contain zero business logic.
