package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import com.aslmmovic.qurancompanion.domain.repository.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePurchasesRepository(
    initialEntitlements: UserEntitlements = UserEntitlements()
) : PurchasesRepository {

    private val _entitlementsFlow = MutableStateFlow(initialEntitlements)
    override val entitlementsFlow: StateFlow<UserEntitlements> = _entitlementsFlow.asStateFlow()

    var mockProducts: List<SubscriptionProduct> = listOf(
        SubscriptionProduct(
            id = "sahaba_tip_199",
            tier = ProductTier.TIP,
            formattedPrice = "$1.99",
            title = "Supporter Tip",
            description = "One-time contribution"
        ),
        SubscriptionProduct(
            id = "sahaba_plus_monthly",
            tier = ProductTier.MONTHLY,
            formattedPrice = "$2.99",
            title = "Monthly Patron",
            description = "Monthly patronage with 7-day trial",
            hasFreeTrial = true
        ),
        SubscriptionProduct(
            id = "sahaba_plus_annual",
            tier = ProductTier.ANNUAL,
            formattedPrice = "$19.99",
            title = "Annual Patron",
            description = "Annual patronage - Best Value",
            effectiveMonthlyPrice = "$1.67/mo"
        )
    )

    var shouldFailPurchase = false
    var shouldFailRestore = false
    var purchaseCallCount = 0
    var restoreCallCount = 0

    fun updateEntitlements(entitlements: UserEntitlements) {
        _entitlementsFlow.value = entitlements
    }

    override suspend fun initialize() {
        // In-memory no-op
    }

    override suspend fun getProducts(): List<SubscriptionProduct> {
        return mockProducts
    }

    override suspend fun purchaseProduct(productId: String): Result<UserEntitlements> {
        purchaseCallCount++
        if (shouldFailPurchase) {
            return Result.failure(Exception("Purchase failed or cancelled"))
        }

        val current = _entitlementsFlow.value
        val updated = when (productId) {
            "sahaba_tip_199" -> current.copy(isSupporter = true)
            else -> current.copy(isPatron = true)
        }
        _entitlementsFlow.value = updated
        return Result.success(updated)
    }

    override suspend fun restorePurchases(): Result<UserEntitlements> {
        restoreCallCount++
        if (shouldFailRestore) {
            return Result.failure(Exception("Restore failed"))
        }
        return Result.success(_entitlementsFlow.value)
    }
}
