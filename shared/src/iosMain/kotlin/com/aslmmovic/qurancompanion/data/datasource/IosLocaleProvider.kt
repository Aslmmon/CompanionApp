package com.aslmmovic.qurancompanion.data.datasource

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/**
 * iOS implementation of [LocaleProvider].
 * Returns the preferred language code (e.g. "en", "ar").
 */
class IosLocaleProvider : LocaleProvider {
    override val currentLocale: String
        get() {
            val preferred = NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
            return preferred.split("-").firstOrNull() ?: "en"
        }
}
