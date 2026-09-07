package com.aslmmovic.qurancompanion.data.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

/**
 * iOS implementation of [LocaleProvider].
 * Returns the preferred language code (e.g. "en", "ar").
 */
class IosLocaleProvider(private val storage: KeyValueStorage) : LocaleProvider {
    private val _currentLocale = MutableStateFlow(
        storage.getString("pref_preferred_language")
            ?: (NSLocale.preferredLanguages.firstOrNull() as? String)?.split("-")?.firstOrNull()
            ?: "en"
    )

    override val currentLocale: String
        get() = _currentLocale.value

    override val currentLocaleFlow: StateFlow<String> = _currentLocale.asStateFlow()

    override fun changeLocale(locale: String) {
        _currentLocale.value = locale
    }
}
