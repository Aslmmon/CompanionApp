package com.aslmmovic.qurancompanion.domain.model

enum class ProductTier {
    TIP,
    MONTHLY,
    ANNUAL
}

data class SubscriptionProduct(
    val id: String,
    val tier: ProductTier,
    val formattedPrice: String,
    val title: String,
    val description: String,
    val hasFreeTrial: Boolean = false,
    val effectiveMonthlyPrice: String? = null
)
