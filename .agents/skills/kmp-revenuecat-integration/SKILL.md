---
name: kmp-revenuecat-integration
description: >-
  RevenueCat KMP integration best practices for Compose Multiplatform projects.
  Covers SDK initialization in platform entry points, purchase flow with sealed
  Result types, entitlement checking, paywall presentation, the fake fallback pattern,
  and common bugs to avoid. Use when working on RevenueCat monetization in any KMP project.
---

# RevenueCat KMP Integration Guide

---

## Architecture Overview

```
Platform Entry Point (MainActivity / MainViewController)
    └── initializes RevenueCatPurchasesRepositoryImpl
            └── implements PurchasesRepository (domain interface)
                    └── exposes entitlementsFlow: StateFlow<UserEntitlements>
                            └── consumed by ViewModel → State → Screen
```

The domain layer (`PurchasesRepository` interface) knows nothing about RevenueCat.
The data layer (`RevenueCatPurchasesRepositoryImpl`) does the SDK wiring.

---

## Step 1: Domain Interface (platform-neutral)

```kotlin
// domain/repository/PurchasesRepository.kt
interface PurchasesRepository {
    val entitlementsFlow: StateFlow<UserEntitlements>
    suspend fun initialize()  // No platform API keys here — platform handles it
    suspend fun purchaseProduct(productId: ProductId): Result<UserEntitlements>
    suspend fun restorePurchases(): Result<UserEntitlements>
}
```

> Note: API keys are NOT parameters here. Platforms inject them during initialization.

---

## Step 2: Real Implementation (data layer)

```kotlin
// data/purchases/RevenueCatPurchasesRepositoryImpl.kt
class RevenueCatPurchasesRepositoryImpl(
    private val apiKey: String,
) : PurchasesRepository {

    private val _entitlementsFlow = MutableStateFlow(UserEntitlements())
    override val entitlementsFlow: StateFlow<UserEntitlements> = _entitlementsFlow.asStateFlow()

    @Volatile private var isConfigured = false

    override suspend fun initialize() {
        try {
            Purchases.configure(PurchasesConfiguration(apiKey))
            isConfigured = true
            refreshEntitlements()
        } catch (e: Exception) {
            println("RevenueCat init failed: ${e.message}")
        }
    }

    override suspend fun purchaseProduct(productId: ProductId): Result<UserEntitlements> {
        if (!isConfigured) return Result.failure(IllegalStateException("RevenueCat not initialized"))
        return try {
            val customerInfo = Purchases.sharedInstance.awaitPurchase(productId.identifier)
            updateEntitlements(customerInfo)
            Result.success(_entitlementsFlow.value)
        } catch (e: PurchasesException) {
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<UserEntitlements> {
        if (!isConfigured) return Result.failure(IllegalStateException("RevenueCat not initialized"))
        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            updateEntitlements(customerInfo)
            Result.success(_entitlementsFlow.value)
        } catch (e: PurchasesException) {
            Result.failure(e)
        }
    }

    private suspend fun refreshEntitlements() {
        try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
            updateEntitlements(customerInfo)
        } catch (e: Exception) {
            println("RevenueCat entitlement refresh failed: ${e.message}")
        }
    }

    private fun updateEntitlements(customerInfo: CustomerInfo) {
        _entitlementsFlow.value = UserEntitlements(
            isPremium = customerInfo.entitlements["premium"]?.isActive == true
        )
    }
}
```

---

## Step 3: Platform Initialization (entry points)

### Android — `MainActivity.onCreate`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize RevenueCat BEFORE setContent
        startKoin {
            androidContext(this@MainActivity)
            modules(appModule, androidModule)
        }

        lifecycleScope.launch {
            get<PurchasesRepository>().initialize()
        }

        setContent { App() }
    }
}
```

### iOS — `MainViewController.kt`

```kotlin
fun MainViewController(): UIViewController {
    startKoin {
        modules(appModule, iosModule)
    }
    return ComposeUIViewController { App() }
}
```

---

## Step 4: In-Memory Fake for Local Testing

```kotlin
// commonTest/kotlin/.../FakePurchasesRepository.kt  ← NOTE: commonTest, NOT commonMain
class FakePurchasesRepository : PurchasesRepository {
    private val _entitlementsFlow = MutableStateFlow(UserEntitlements())
    override val entitlementsFlow: StateFlow<UserEntitlements> = _entitlementsFlow.asStateFlow()

    var shouldFailPurchase = false
    var shouldFailRestore = false

    override suspend fun initialize() { /* no-op */ }

    override suspend fun purchaseProduct(productId: ProductId): Result<UserEntitlements> {
        if (shouldFailPurchase) return Result.failure(Exception("Simulated purchase failure"))
        val updated = _entitlementsFlow.value.copy(isPremium = true)
        _entitlementsFlow.value = updated
        return Result.success(updated)
    }

    override suspend fun restorePurchases(): Result<UserEntitlements> {
        if (shouldFailRestore) return Result.failure(Exception("Simulated restore failure"))
        return Result.success(_entitlementsFlow.value)
    }
}
```

---

## Step 5: Paywall UI Best Practices

```kotlin
@Composable
fun PaywallBottomSheet(
    products: List<StoreProduct>,
    onPurchaseProduct: (ProductId) -> Unit,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit,
) {
    products.forEach { product ->
        PaywallOptionCard(
            title = product.title,
            price = product.price.formatted,
            onClick = { onPurchaseProduct(ProductId.fromStoreProductId(product.id)) }
        )
    }
}
```

---

## Common Bugs Checklist

- [ ] **RevenueCat initialized before `setContent`** — not after, not inside a Composable
- [ ] **`purchaseProduct` calls the REAL SDK** — not the fake fallback in production
- [ ] **`onError` in SDK callbacks logs or emits error state** — never silent
- [ ] **`isConfigured` is `@Volatile`** — accessed from multiple coroutines
- [ ] **Prices displayed from `StoreProduct.price`** — never hardcoded strings
- [ ] **`FakePurchasesRepository` is in `commonTest/`** — never in `commonMain/` production code
- [ ] **`entitlementsFlow` subscribed in ViewModel** — UI always sees latest entitlements reactively
