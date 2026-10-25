package com.aslmmovic.qurancompanion.presentation.screens.paywall

sealed interface PaywallUiEffect {
    data object Dismiss : PaywallUiEffect
    data object PurchaseSuccess : PaywallUiEffect
    data class ShowError(val message: String) : PaywallUiEffect
}
