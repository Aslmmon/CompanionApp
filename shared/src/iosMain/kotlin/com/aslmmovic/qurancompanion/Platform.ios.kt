package com.aslmmovic.qurancompanion

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val languageCode: String
        get() {
            val preferred = platform.Foundation.NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
            return preferred.split("-").firstOrNull() ?: "en"
        }
}

actual fun getPlatform(): Platform = IOSPlatform()