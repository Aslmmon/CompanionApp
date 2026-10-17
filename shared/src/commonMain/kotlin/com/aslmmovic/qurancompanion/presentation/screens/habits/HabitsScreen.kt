package com.aslmmovic.qurancompanion.presentation.screens.habits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.habits_celebration_title

@Composable
fun HabitsScreen(
    modifier: Modifier = Modifier,
    viewModel: HabitsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val celebrationMessage = stringResource(Res.string.habits_celebration_title)

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HabitsUiEffect.ShowCelebration -> {
                    snackbarHostState.showSnackbar(celebrationMessage)
                }
                is HabitsUiEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HabitsContent(
            uiState = uiState,
            onToggleHabit = viewModel::onToggleHabit,
            onOpenAddSheet = viewModel::onOpenAddSheet,
            onCloseAddSheet = viewModel::onCloseAddSheet,
            onCatalogCategorySelected = viewModel::onCatalogCategorySelected,
            onAddHabitFromCatalog = viewModel::onAddHabitFromCatalog,
            onCreateCustomHabit = viewModel::onCreateCustomHabit,
            onSelectHabitDetail = viewModel::onSelectHabitForDetail,
            onDismissHabitDetail = viewModel::onDismissHabitDetail,
            onRemoveHabit = viewModel::onRemoveHabit
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}
