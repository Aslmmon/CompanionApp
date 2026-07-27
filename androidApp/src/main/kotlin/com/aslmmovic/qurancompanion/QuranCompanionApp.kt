package com.aslmmovic.qurancompanion

import android.app.Application
import com.aslmmovic.qurancompanion.di.androidModule
import com.aslmmovic.qurancompanion.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class QuranCompanionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@QuranCompanionApp)
            modules(androidModule(applicationContext), appModule)
        }
    }
}
