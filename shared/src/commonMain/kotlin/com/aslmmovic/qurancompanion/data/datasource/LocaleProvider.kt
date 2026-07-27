package com.aslmmovic.qurancompanion.data.datasource

/**
 * Provides the current device locale code (e.g. "en", "ar").
 * Platform-specific implementations are registered via Koin's platform modules.
 */
interface LocaleProvider {
    val currentLocale: String
    fun changeLocale(locale: String)
}
