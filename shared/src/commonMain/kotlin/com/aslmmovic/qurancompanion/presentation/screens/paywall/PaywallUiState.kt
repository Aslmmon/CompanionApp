package com.aslmmovic.qurancompanion.presentation.screens.paywall

import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct

data class PaywallUiState(
    val products: List<SubscriptionProduct> = emptyList(),
    val selectedProductId: String? = null,
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false,
    val errorMessage: String? = null
)
