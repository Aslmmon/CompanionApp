package com.aslmmovic.qurancompanion.data.purchases

import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import com.aslmmovic.qurancompanion.domain.repository.PurchasesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosPurchasesRepositoryImpl(
    private val storage: KeyValueStorage
) : PurchasesRepository {

    private val _entitlementsFlow = MutableStateFlow(loadCachedEntitlements())
    override val entitlementsFlow: StateFlow<UserEntitlements> = _entitlementsFlow.asStateFlow()

    private val defaultProducts = listOf(
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

    override suspend fun initialize() {
        // Platform RevenueCat setup for iOS in future phase
    }

    override suspend fun getProducts(): List<SubscriptionProduct> {
        return defaultProducts
    }

    override suspend fun purchaseProduct(productId: String): Result<UserEntitlements> {
        val current = _entitlementsFlow.value
        val updated = if (productId == "sahaba_tip_199") {
            current.copy(isSupporter = true)
        } else {
            current.copy(isPatron = true)
        }
        _entitlementsFlow.value = updated
        saveCachedEntitlements(updated)
        return Result.success(updated)
    }

    override suspend fun restorePurchases(): Result<UserEntitlements> {
        return Result.success(_entitlementsFlow.value)
    }

    private fun loadCachedEntitlements(): UserEntitlements {
        val isPatron = storage.getBoolean(KEY_IS_PATRON, false)
        val isSupporter = storage.getBoolean(KEY_IS_SUPPORTER, false)
        val exp = storage.getString(KEY_EXPIRATION)?.toLongOrNull()
        return UserEntitlements(
            isPatron = isPatron,
            isSupporter = isSupporter,
            expirationTimestamp = exp
        )
    }

    private fun saveCachedEntitlements(entitlements: UserEntitlements) {
        storage.putBoolean(KEY_IS_PATRON, entitlements.isPatron)
        storage.putBoolean(KEY_IS_SUPPORTER, entitlements.isSupporter)
        entitlements.expirationTimestamp?.let {
            storage.putString(KEY_EXPIRATION, it.toString())
        }
    }

    companion object {
        private const val KEY_IS_PATRON = "is_patron_active"
        private const val KEY_IS_SUPPORTER = "is_supporter_active"
        private const val KEY_EXPIRATION = "patron_expiration_date"
    }
}
