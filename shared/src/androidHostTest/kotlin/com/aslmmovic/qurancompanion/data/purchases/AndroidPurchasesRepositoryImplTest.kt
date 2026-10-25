package com.aslmmovic.qurancompanion.data.purchases

import android.content.ContextWrapper
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AndroidPurchasesRepositoryImplTest {

    private class FakeKeyValueStorage : KeyValueStorage {
        private val booleans = mutableMapOf<String, Boolean>()
        private val ints = mutableMapOf<String, Int>()
        private val strings = mutableMapOf<String, String>()

        override fun getString(key: String): String? = strings[key]
        override fun putString(key: String, value: String) { strings[key] = value }
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue
        override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
        override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
        override fun putInt(key: String, value: Int) { ints[key] = value }
    }

    private val fakeContext = ContextWrapper(null)
    private val fakeStorage = FakeKeyValueStorage()

    @Test
    fun `purchaseProduct fails in release mode when unconfigured and never grants entitlements`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = false
        )

        val result = repository.purchaseProduct("sahaba_plus_annual")

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertFalse(repository.entitlementsFlow.value.isPatron)
        assertFalse(fakeStorage.getBoolean("is_patron_active", false))
    }

    @Test
    fun `restorePurchases fails in release mode when unconfigured`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = false
        )

        val result = repository.restorePurchases()

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertFalse(repository.entitlementsFlow.value.isPatron)
    }

    @Test
    fun `getProducts returns empty list in release mode when unconfigured`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = false
        )

        val products = repository.getProducts()

        assertTrue(products.isEmpty())
    }

    @Test
    fun `purchaseProduct succeeds in debug mode when unconfigured and grants patron`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = true
        )

        val result = repository.purchaseProduct("sahaba_plus_annual")

        assertTrue(result.isSuccess)
        val entitlements = result.getOrNull()
        assertTrue(entitlements?.isPatron == true)
        assertTrue(repository.entitlementsFlow.value.isPatron)
        assertTrue(fakeStorage.getBoolean("is_patron_active", false))
    }

    @Test
    fun `purchaseProduct succeeds in debug mode when unconfigured and grants supporter for tip`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = true
        )

        val result = repository.purchaseProduct("sahaba_tip_199")

        assertTrue(result.isSuccess)
        val entitlements = result.getOrNull()
        assertTrue(entitlements?.isSupporter == true)
        assertTrue(repository.entitlementsFlow.value.isSupporter)
        assertTrue(fakeStorage.getBoolean("is_supporter_active", false))
    }

    @Test
    fun `restorePurchases succeeds in debug mode when unconfigured`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = true
        )

        val result = repository.restorePurchases()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getProducts returns default mock products in debug mode when unconfigured`() = runTest {
        val repository = AndroidPurchasesRepositoryImpl(
            context = fakeContext,
            storage = fakeStorage,
            apiKey = "",
            isDebug = true
        )

        val products = repository.getProducts()

        assertEquals(3, products.size)
        assertTrue(products.any { it.id == "sahaba_plus_annual" })
        assertTrue(products.any { it.id == "sahaba_plus_monthly" })
        assertTrue(products.any { it.id == "sahaba_tip_199" })
    }
}
