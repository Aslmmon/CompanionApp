package com.aslmmovic.qurancompanion

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val languageCode: String
        get() = java.util.Locale.getDefault().language
}

actual fun getPlatform(): Platform = AndroidPlatform()