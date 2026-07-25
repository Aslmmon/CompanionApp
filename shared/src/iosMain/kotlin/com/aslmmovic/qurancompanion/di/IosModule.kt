package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.datasource.IosKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.IosLocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import org.koin.dsl.module

val iosModule = module {
    single<KeyValueStorage> { IosKeyValueStorage() }
    single<LocaleProvider> { IosLocaleProvider() }
}
