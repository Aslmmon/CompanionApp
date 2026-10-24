package com.aslmmovic.qurancompanion.di

import android.content.Context
import com.aslmmovic.qurancompanion.data.datasource.AndroidKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.AndroidLocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.purchases.AndroidPurchasesRepositoryImpl
import com.aslmmovic.qurancompanion.domain.repository.PurchasesRepository
import com.aslmmovic.qurancompanion.domain.util.AppBuildInfoProvider
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import com.aslmmovic.qurancompanion.util.AndroidAppBuildInfoProvider
import com.aslmmovic.qurancompanion.util.AndroidNotificationScheduler
import org.koin.dsl.bind
import org.koin.dsl.module

fun androidModule(context: Context, apiKey: String = "") = module {
    single<KeyValueStorage> { AndroidKeyValueStorage(context) }
    single<LocaleProvider> { AndroidLocaleProvider(context, get()) }
    single { AndroidNotificationScheduler(context) } bind NotificationScheduler::class
    single<AppBuildInfoProvider> { AndroidAppBuildInfoProvider(context) }
    single<PurchasesRepository> {
        AndroidPurchasesRepositoryImpl(
            context = context,
            storage = get(),
            apiKey = apiKey
        )
    }
}
