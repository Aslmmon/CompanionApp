package com.aslmmovic.qurancompanion

import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.model.UserEntitlements
import com.aslmmovic.qurancompanion.domain.usecase.GetEntitlementsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetProductsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.PurchaseProductUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RestorePurchasesUseCase
import com.aslmmovic.qurancompanion.fakes.FakePurchasesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurchasesUseCasesTest {

    private lateinit var fakeRepository: FakePurchasesRepository
    private lateinit var getEntitlementsUseCase: GetEntitlementsUseCase
    private lateinit var getProductsUseCase: GetProductsUseCase
    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    @BeforeTest
    fun setUp() {
        fakeRepository = FakePurchasesRepository()
        getEntitlementsUseCase = GetEntitlementsUseCase(fakeRepository)
        getProductsUseCase = GetProductsUseCase(fakeRepository)
        purchaseProductUseCase = PurchaseProductUseCase(fakeRepository)
        restorePurchasesUseCase = RestorePurchasesUseCase(fakeRepository)
    }

    @Test
    fun getEntitlementsUseCase_emitsInitialCachedEntitlementsImmediately() = runTest {
        val cached = UserEntitlements(isPatron = true, isSupporter = true)
        val repo = FakePurchasesRepository(initialEntitlements = cached)
        val useCase = GetEntitlementsUseCase(repo)

        val entitlements = useCase().first()
        assertTrue(entitlements.isPatron)
        assertTrue(entitlements.isSupporter)
    }

    @Test
    fun getProductsUseCase_returnsAvailableProductTiers() = runTest {
        val products = getProductsUseCase()
        assertEquals(3, products.size)
        assertEquals(ProductTier.TIP, products[0].tier)
        assertEquals(ProductTier.MONTHLY, products[1].tier)
        assertEquals(ProductTier.ANNUAL, products[2].tier)
    }

    @Test
    fun purchaseProductUseCase_annualSubscription_updatesPatronEntitlement() = runTest {
        assertFalse(getEntitlementsUseCase().first().isPatron)

        val result = purchaseProductUseCase("sahaba_plus_annual")
        assertTrue(result.isSuccess)
        val entitlements = result.getOrThrow()
        assertTrue(entitlements.isPatron)
        assertFalse(entitlements.isSupporter)
        assertTrue(getEntitlementsUseCase().first().isPatron)
    }

    @Test
    fun purchaseProductUseCase_tip_updatesSupporterEntitlement() = runTest {
        assertFalse(getEntitlementsUseCase().first().isSupporter)

        val result = purchaseProductUseCase("sahaba_tip_199")
        assertTrue(result.isSuccess)
        val entitlements = result.getOrThrow()
        assertFalse(entitlements.isPatron)
        assertTrue(entitlements.isSupporter)
        assertTrue(getEntitlementsUseCase().first().isSupporter)
    }

    @Test
    fun purchaseProductUseCase_failure_propagatesError() = runTest {
        fakeRepository.shouldFailPurchase = true

        val result = purchaseProductUseCase("sahaba_plus_annual")
        assertTrue(result.isFailure)
        assertFalse(getEntitlementsUseCase().first().isPatron)
    }

    @Test
    fun restorePurchasesUseCase_success_returnsCurrentEntitlements() = runTest {
        fakeRepository.updateEntitlements(UserEntitlements(isPatron = true))

        val result = restorePurchasesUseCase()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isPatron)
        assertEquals(1, fakeRepository.restoreCallCount)
    }

    @Test
    fun restorePurchasesUseCase_failure_propagatesError() = runTest {
        fakeRepository.shouldFailRestore = true

        val result = restorePurchasesUseCase()
        assertTrue(result.isFailure)
    }
}
