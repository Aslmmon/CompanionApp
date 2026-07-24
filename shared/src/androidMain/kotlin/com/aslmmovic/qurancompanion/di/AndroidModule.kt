package com.aslmmovic.qurancompanion.di

import android.content.Context
import com.aslmmovic.qurancompanion.data.datasource.AndroidKeyValueStorage
import com.aslmmovic.qurancompanion.data.datasource.KeyValueStorage
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single<KeyValueStorage> { AndroidKeyValueStorage(context) }
}
