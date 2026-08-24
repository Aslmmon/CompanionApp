---
name: kmp-composable-architecture
description: >-
  Best practices for Jetpack Compose and Compose Multiplatform UI component composition.
  Focuses on the Container-Content pattern, single-responsibility composables, slot APIs,
  placing sub-composables in dedicated files, and avoiding monolithic files. Use when designing,
  building, or refactoring UI components.
---

# Composable Architecture & Component Decomposition Guide

Jetpack Compose and Compose Multiplatform code should be modular, readable, predictable, and easy to test and preview.
Avoid creating monolithic composables that handle state, layout, sub-views, and dialogs all in a single block of code or inside a single massive file.

---

## 1. Golden Rules of Composable Design

1. **Single Responsibility Principle**: Every composable function must have ONE clearly defined job (e.g. rendering a header, rendering an item row, or container layout).
2. **Dedicated Files for Sub-Composables**: Each reusable or distinct sub-composable **must be created in its own Kotlin file** in the feature directory (or a `components/` sub-package, e.g. `presentation/game/components/GameHud.kt`). Do NOT place all sub-composables inside the main screen file.
3. **Container vs Content Separation**:
   - **Screen Container**: Handles ViewModel injection (`koinViewModel()`), lifecycle state collection, LaunchedEffect side-effects, and passes state slices down.
   - **Screen Content**: Receives `@Immutable UiState` snapshot + lambda callbacks `() -> Unit`. Contains top-level structural layout (e.g. Scaffold / Column).
   - **Sub-Composables**: Small, stateless functions in their own dedicated files dedicated to specific UI sections.
4. **Max Line Limit**: If a single composable file or function exceeds **60–80 lines**, decompose it into separate component files.
5. **Slot API Pattern**: Custom layout cards, headers, and dialog containers should accept `@Composable () -> Unit` slot parameters instead of hardcoding inner content.
6. **Stateless Sub-Views**: Pass state data classes / primitives and explicit lambda callbacks `(Type) -> Unit` to child composables. Never pass the ViewModel or StateFlow down to sub-views.

---

## 2. Container vs Content Architecture & File Organization

```
presentation/[feature]/
├── [Feature]Screen.kt         ──▶ Screen Container (ViewModel injection, State collection, LaunchedEffect)
├── [Feature]Content.kt        ──▶ Pure Content Layout (Scaffold, LazyColumn)
└── components/
    ├── [Feature]Header.kt     ──▶ Dedicated file for Header sub-composable
    ├── [Feature]ItemRow.kt    ──▶ Dedicated file for Item Row sub-composable
    └── [Feature]Footer.kt     ──▶ Dedicated file for Footer sub-composable
```

---

## 3. Implementation Example

### Recommended Structure across Files

#### File 1: `presentation/game/GameScreen.kt` (Container)
```kotlin
@Composable
fun GameScreen(
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                GameUiEffect.NavigateHome -> onNavigateHome()
            }
        }
    }

    GameContent(
        uiState = uiState,
        onPauseClicked = viewModel::onPauseClicked,
        onItemClicked = viewModel::onItemClicked,
        onItemDeleted = viewModel::onItemDeleted,
        onDismissDialog = viewModel::onDismissDialog,
        onConfirmDialog = viewModel::onConfirmDialog,
        modifier = modifier,
    )
}
```

#### File 2: `presentation/game/GameContent.kt` (Top-Level Layout)
```kotlin
@Composable
fun GameContent(
    uiState: GameUiState,
    onPauseClicked: () -> Unit,
    onItemClicked: (String) -> Unit,
    onItemDeleted: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            GameHeader(
                score = uiState.score,
                lives = uiState.lives,
                onPauseClicked = onPauseClicked,
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GameItemList(
                items = uiState.items,
                onItemClicked = onItemClicked,
                onItemDeleted = onItemDeleted,
            )

            if (uiState.showConfirmDialog) {
                GameConfirmDialog(
                    onDismiss = onDismissDialog,
                    onConfirm = onConfirmDialog,
                )
            }
        }
    }
}
```

#### File 3: `presentation/game/components/GameHeader.kt` (Dedicated File)
```kotlin
package com.aerobounce.presentation.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameHeader(
    score: Int,
    lives: Int,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Score: $score", style = MaterialTheme.typography.titleMedium)
        Text(text = "Lives: $lives", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onPauseClicked) {
            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
        }
    }
}
```

#### File 4: `presentation/game/components/GameItemRow.kt` (Dedicated File)
```kotlin
package com.aerobounce.presentation.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GameItemRow(
    item: GameItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                Text(text = item.subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
```

---

## 4. Slot API Pattern for Reusable Containers

When creating custom container cards, dialog wrappers, or section boxes, use `@Composable () -> Unit` slots instead of hardcoding child layouts.

```kotlin
@Composable
fun AppSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
```

---

## 5. Checklist for Composables

- [ ] Is each sub-composable created in its own dedicated Kotlin file in `presentation/[feature]/components/` or `presentation/[feature]/`?
- [ ] Is the screen split into a Container Composable (`[Feature]Screen.kt`) and Content Composable (`[Feature]Content.kt`)?
- [ ] Are all sub-composable files under 60–80 lines long?
- [ ] Are child composables pure and stateless (accepting `@Immutable` data and lambda callbacks)?
- [ ] Is ViewModel injection limited ONLY to the top-level Container Composable?
- [ ] Are reusable layout containers using Slot APIs (`@Composable () -> Unit`)?
- [ ] Are items in `LazyColumn` / `LazyRow` using stable `key` parameter (e.g. `key = { it.id }`)?
