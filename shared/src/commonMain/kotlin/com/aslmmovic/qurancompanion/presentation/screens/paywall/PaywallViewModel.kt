package com.aslmmovic.qurancompanion.presentation.screens.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.usecase.GetProductsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.PurchaseProductUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RestorePurchasesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallUiState(isLoading = true))
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<PaywallUiEffect>()
    val uiEffects: SharedFlow<PaywallUiEffect> = _uiEffects.asSharedFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val products = getProductsUseCase()
            val defaultSelected = products.firstOrNull { it.tier == ProductTier.ANNUAL }?.id
                ?: products.firstOrNull()?.id
            _uiState.update {
                it.copy(
                    products = products,
                    selectedProductId = defaultSelected,
                    isLoading = false
                )
            }
        }
    }

    fun onSelectProduct(productId: String) {
        _uiState.update { it.copy(selectedProductId = productId) }
    }

    fun onPurchaseClicked(productId: String? = null) {
        val targetId = productId ?: _uiState.value.selectedProductId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }
            val result = purchaseProductUseCase(targetId)
            _uiState.update { it.copy(isPurchasing = false) }
            result.fold(
                onSuccess = {
                    _uiEffects.emit(PaywallUiEffect.PurchaseSuccess)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Purchase failed"
                    _uiState.update { it.copy(errorMessage = msg) }
                    _uiEffects.emit(PaywallUiEffect.ShowError(msg))
                }
            )
        }
    }

    fun onRestoreClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, errorMessage = null) }
            val result = restorePurchasesUseCase()
            _uiState.update { it.copy(isRestoring = false) }
            result.fold(
                onSuccess = {
                    _uiEffects.emit(PaywallUiEffect.PurchaseSuccess)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Restore failed"
                    _uiState.update { it.copy(errorMessage = msg) }
                    _uiEffects.emit(PaywallUiEffect.ShowError(msg))
                }
            )
        }
    }

    fun onDismissClicked() {
        viewModelScope.launch {
            _uiEffects.emit(PaywallUiEffect.Dismiss)
        }
    }
}
