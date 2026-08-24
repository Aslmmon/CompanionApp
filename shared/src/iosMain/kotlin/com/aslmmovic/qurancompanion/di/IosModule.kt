package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.datasource.IosKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.IosLocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import com.aslmmovic.qurancompanion.util.IosNotificationScheduler
import org.koin.dsl.bind
import org.koin.dsl.module

val iosModule = module {
    single<KeyValueStorage> { IosKeyValueStorage() }
    single<LocaleProvider> { IosLocaleProvider(get()) }
    single { IosNotificationScheduler() } bind NotificationScheduler::class
}
