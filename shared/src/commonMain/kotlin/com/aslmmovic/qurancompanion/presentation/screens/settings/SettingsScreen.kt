package com.aslmmovic.qurancompanion.presentation.screens.settings

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallBottomSheet
import com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallUiEffect
import com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallViewModel
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.paywall_success_message
import qurancompanion.shared.generated.resources.reminder_set_today
import qurancompanion.shared.generated.resources.reminder_set_tomorrow

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    paywallViewModel: PaywallViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paywallUiState by paywallViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPaywallSheet by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                SettingsUiEffect.NavigateBack -> onNavigateBack()
                SettingsUiEffect.OpenPaywall -> {
                    showPaywallSheet = true
                }
                is SettingsUiEffect.ShowReminderConfirmation -> {
                    val message = if (effect.isToday) {
                        getString(Res.string.reminder_set_today, effect.formattedTime)
                    } else {
                        getString(Res.string.reminder_set_tomorrow, effect.formattedTime)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    LaunchedEffect(paywallViewModel.uiEffects) {
        paywallViewModel.uiEffects.collect { effect ->
            when (effect) {
                PaywallUiEffect.Dismiss -> {
                    showPaywallSheet = false
                }
                PaywallUiEffect.PurchaseSuccess -> {
                    showPaywallSheet = false
                    val successMsg = getString(Res.string.paywall_success_message)
                    snackbarHostState.showSnackbar(successMsg)
                }
                is PaywallUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SettingsContent(
            uiState = uiState,
            onToggleTheme = viewModel::onToggleTheme,
            onSelectTheme = viewModel::onSelectTheme,
            onLanguageSelected = viewModel::onLanguageSelected,
            onUpdateReminderTime = viewModel::onUpdateReminderTime,
            onToggleReminder = viewModel::onToggleReminder,
            onOpenPaywall = viewModel::onOpenSupportCard,
            onSimulateNextDay = viewModel::onSimulateNextDay,
            onTriggerNotification = viewModel::onTriggerNotification,
            onBackClick = viewModel::onBackClick,
            modifier = Modifier.fillMaxSize()
        )

        if (showPaywallSheet) {
            PaywallBottomSheet(
                uiState = paywallUiState,
                onSelectProduct = paywallViewModel::onSelectProduct,
                onPurchaseClicked = { paywallViewModel.onPurchaseClicked() },
                onRestoreClicked = paywallViewModel::onRestoreClicked,
                onDismissRequest = { showPaywallSheet = false }
            )
        }

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
