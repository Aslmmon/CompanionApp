package com.aslmmovic.qurancompanion.di

import android.content.Context
import com.aslmmovic.qurancompanion.data.datasource.AndroidKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.AndroidLocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<KeyValueStorage> { AndroidKeyValueStorage(context) }
    single<LocaleProvider> { AndroidLocaleProvider(get()) }
}
