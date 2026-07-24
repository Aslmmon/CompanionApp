package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.datasource.IosKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import org.koin.dsl.module

val iosModule = module {
    single<KeyValueStorage> { IosKeyValueStorage() }
}
