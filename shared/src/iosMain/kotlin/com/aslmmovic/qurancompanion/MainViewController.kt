package com.aslmmovic.qurancompanion

import androidx.compose.ui.window.ComposeUIViewController
import com.aslmmovic.qurancompanion.di.appModule
import com.aslmmovic.qurancompanion.di.iosModule
import org.koin.core.context.startKoin

fun MainViewController() = run {
    startKoin {
        modules(iosModule, appModule)
    }
    ComposeUIViewController { App() }
}