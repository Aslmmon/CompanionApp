package com.aslmmovic.qurancompanion.data.purchases

import android.content.Context
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.model.SubscriptionProduct
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import com.aslmmovic.qurancompanion.domain.repository.PurchasesRepository
import com.aslmmovic.qurancompanion.util.AndroidActivityProvider
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidPurchasesRepositoryImpl(
    private val context: Context,
    private val storage: KeyValueStorage,
    private val apiKey: String = ""
) : PurchasesRepository {

    private val _entitlementsFlow = MutableStateFlow(loadCachedEntitlements())
    override val entitlementsFlow: StateFlow<UserEntitlements> = _entitlementsFlow.asStateFlow()

    @Volatile
    private var isConfigured: Boolean = false

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
        if (apiKey.isBlank()) {
            return
        }

        try {
            val config = PurchasesConfiguration.Builder(context, apiKey).build()
            Purchases.configure(config)
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                updateFromCustomerInfo(customerInfo)
            }
            isConfigured = true
            refreshCustomerInfo()
        } catch (_: Exception) {
            // Safe fallback if offline during startup
        }
    }

    override suspend fun getProducts(): List<SubscriptionProduct> {
        if (!isConfigured) {
            return defaultProducts
        }

        return try {
            val offerings = suspendCancellableCoroutine<Offerings?> { cont ->
                Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                    override fun onReceived(offerings: Offerings) {
                        cont.resume(offerings)
                    }

                    override fun onError(error: PurchasesError) {
                        cont.resume(null)
                    }
                })
            }

            val currentOffering = offerings?.current
            if (currentOffering == null || currentOffering.availablePackages.isEmpty()) {
                defaultProducts
            } else {
                mapOfferingsToProducts(currentOffering.availablePackages)
            }
        } catch (_: Exception) {
            defaultProducts
        }
    }

    override suspend fun purchaseProduct(productId: String): Result<UserEntitlements> {
        if (!isConfigured) {
            // Simulated development / mock purchase
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

        val activity = AndroidActivityProvider.currentActivity
            ?: return Result.failure(IllegalStateException("Active activity not found for billing flow"))

        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
                override fun onError(error: PurchasesError) {
                    cont.resume(Result.failure(Exception(error.message)))
                }

                override fun onReceived(offerings: Offerings) {
                    val pkg = offerings.current?.availablePackages?.find {
                        it.product.id == productId || it.identifier == productId
                    }

                    if (pkg == null) {
                        cont.resume(Result.failure(IllegalArgumentException("Package $productId not found in offerings")))
                        return
                    }

                    Purchases.sharedInstance.purchasePackage(
                        activity,
                        pkg,
                        object : PurchaseCallback {
                            override fun onCompleted(
                                storeTransaction: StoreTransaction,
                                customerInfo: CustomerInfo
                            ) {
                                updateFromCustomerInfo(customerInfo)
                                cont.resume(Result.success(_entitlementsFlow.value))
                            }

                            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                                if (userCancelled) {
                                    cont.resume(Result.failure(Exception("Purchase cancelled")))
                                } else {
                                    cont.resume(Result.failure(Exception(error.message)))
                                }
                            }
                        }
                    )
                }
            })
        }
    }

    override suspend fun restorePurchases(): Result<UserEntitlements> {
        if (!isConfigured) {
            return Result.success(_entitlementsFlow.value)
        }

        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    cont.resume(Result.failure(Exception(error.message)))
                }

                override fun onReceived(customerInfo: CustomerInfo) {
                    updateFromCustomerInfo(customerInfo)
                    cont.resume(Result.success(_entitlementsFlow.value))
                }
            })
        }
    }

    private fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                // Keep cached entitlements offline
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                updateFromCustomerInfo(customerInfo)
            }
        })
    }

    private fun updateFromCustomerInfo(customerInfo: CustomerInfo) {
        val isPatron = customerInfo.entitlements["patron"]?.isActive == true ||
            customerInfo.entitlements["plus"]?.isActive == true ||
            customerInfo.entitlements["premium"]?.isActive == true

        val isSupporter = customerInfo.entitlements["supporter"]?.isActive == true ||
            customerInfo.nonSubscriptionTransactions.any { it.productIdentifier.contains("tip") }

        val expiration = customerInfo.entitlements["patron"]?.expirationDate?.time
            ?: customerInfo.entitlements["plus"]?.expirationDate?.time

        val updated = UserEntitlements(
            isPatron = isPatron,
            isSupporter = isSupporter || _entitlementsFlow.value.isSupporter,
            expirationTimestamp = expiration
        )
        _entitlementsFlow.value = updated
        saveCachedEntitlements(updated)
    }

    private fun mapOfferingsToProducts(packages: List<Package>): List<SubscriptionProduct> {
        return packages.map { pkg ->
            val storeProduct = pkg.product
            val id = storeProduct.id
            val tier = when {
                id.contains("tip", ignoreCase = true) -> ProductTier.TIP
                id.contains("month", ignoreCase = true) -> ProductTier.MONTHLY
                else -> ProductTier.ANNUAL
            }
            SubscriptionProduct(
                id = id,
                tier = tier,
                formattedPrice = storeProduct.price.formatted,
                title = storeProduct.title,
                description = storeProduct.description,
                hasFreeTrial = storeProduct.period != null && tier == ProductTier.MONTHLY,
                effectiveMonthlyPrice = if (tier == ProductTier.ANNUAL) "$1.67/mo" else null
            )
        }
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
