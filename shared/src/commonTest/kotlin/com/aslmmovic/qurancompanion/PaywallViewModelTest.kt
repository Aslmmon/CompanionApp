package com.aslmmovic.qurancompanion

import app.cash.turbine.test
import com.aslmmovic.qurancompanion.domain.model.ProductTier
import com.aslmmovic.qurancompanion.domain.usecase.GetProductsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.PurchaseProductUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RestorePurchasesUseCase
import com.aslmmovic.qurancompanion.fakes.FakePurchasesRepository
import com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallUiEffect
import com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakePurchasesRepository
    private lateinit var getProductsUseCase: GetProductsUseCase
    private lateinit var purchaseProductUseCase: PurchaseProductUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePurchasesRepository()
        getProductsUseCase = GetProductsUseCase(fakeRepository)
        purchaseProductUseCase = PurchaseProductUseCase(fakeRepository)
        restorePurchasesUseCase = RestorePurchasesUseCase(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PaywallViewModel {
        return PaywallViewModel(
            getProductsUseCase = getProductsUseCase,
            purchaseProductUseCase = purchaseProductUseCase,
            restorePurchasesUseCase = restorePurchasesUseCase
        )
    }

    @Test
    fun `init loads products and preselects annual tier`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.products.size)
        assertEquals("sahaba_plus_annual", state.selectedProductId)
    }

    @Test
    fun `onSelectProduct updates selected product`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSelectProduct("sahaba_plus_monthly")
        assertEquals("sahaba_plus_monthly", viewModel.uiState.value.selectedProductId)
    }

    @Test
    fun `onPurchaseClicked succeeds and emits PurchaseSuccess effect`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.onPurchaseClicked("sahaba_plus_annual")
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PaywallUiEffect.PurchaseSuccess)
            assertFalse(viewModel.uiState.value.isPurchasing)
            assertEquals(1, fakeRepository.purchaseCallCount)
        }
    }

    @Test
    fun `onPurchaseClicked failure emits ShowError effect`() = runTest(testDispatcher) {
        fakeRepository.shouldFailPurchase = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.onPurchaseClicked("sahaba_plus_annual")
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PaywallUiEffect.ShowError)
            assertFalse(viewModel.uiState.value.isPurchasing)
            assertNotNull(viewModel.uiState.value.errorMessage)
        }
    }

    @Test
    fun `onRestoreClicked succeeds and emits PurchaseSuccess effect`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.onRestoreClicked()
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PaywallUiEffect.PurchaseSuccess)
            assertFalse(viewModel.uiState.value.isRestoring)
            assertEquals(1, fakeRepository.restoreCallCount)
        }
    }

    @Test
    fun `onRestoreClicked failure emits ShowError effect`() = runTest(testDispatcher) {
        fakeRepository.shouldFailRestore = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.onRestoreClicked()
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PaywallUiEffect.ShowError)
            assertFalse(viewModel.uiState.value.isRestoring)
        }
    }

    @Test
    fun `onDismissClicked emits Dismiss effect`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiEffects.test {
            viewModel.onDismissClicked()
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is PaywallUiEffect.Dismiss)
        }
    }
}
