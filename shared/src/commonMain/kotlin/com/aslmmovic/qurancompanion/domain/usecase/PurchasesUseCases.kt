package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import com.aslmmovic.qurancompanion.domain.repository.PurchasesRepository
import kotlinx.coroutines.flow.StateFlow

class GetEntitlementsUseCase(private val repository: PurchasesRepository) {
    operator fun invoke(): StateFlow<UserEntitlements> = repository.entitlementsFlow
}

class GetProductsUseCase(private val repository: PurchasesRepository) {
    suspend operator fun invoke(): List<SubscriptionProduct> = repository.getProducts()
}

class PurchaseProductUseCase(private val repository: PurchasesRepository) {
    suspend operator fun invoke(productId: String): Result<UserEntitlements> =
        repository.purchaseProduct(productId)
}

class RestorePurchasesUseCase(private val repository: PurchasesRepository) {
    suspend operator fun invoke(): Result<UserEntitlements> =
        repository.restorePurchases()
}
