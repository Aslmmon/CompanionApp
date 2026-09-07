package com.aslmmovic.qurancompanion.data.datasource

import kotlinx.coroutines.flow.StateFlow

/**
 * Provides the current device locale code (e.g. "en", "ar").
 * Platform-specific implementations are registered via Koin's platform modules.
 */
interface LocaleProvider {
    val currentLocale: String
    val currentLocaleFlow: StateFlow<String>
    fun changeLocale(locale: String)
}
