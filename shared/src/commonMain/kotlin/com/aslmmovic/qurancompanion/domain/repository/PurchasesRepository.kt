package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import kotlinx.coroutines.flow.StateFlow

interface PurchasesRepository {
    val entitlementsFlow: StateFlow<UserEntitlements>
    suspend fun initialize()
    suspend fun getProducts(): List<SubscriptionProduct>
    suspend fun purchaseProduct(productId: String): Result<UserEntitlements>
    suspend fun restorePurchases(): Result<UserEntitlements>
}
