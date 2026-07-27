package com.aslmmovic.qurancompanion.data.datasource

/**
 * Android implementation of [LocaleProvider].
 * Returns the current default locale's language code (e.g. "en", "ar").
 */
class AndroidLocaleProvider : LocaleProvider {
    override val currentLocale: String
        get() = java.util.Locale.getDefault().language
}
