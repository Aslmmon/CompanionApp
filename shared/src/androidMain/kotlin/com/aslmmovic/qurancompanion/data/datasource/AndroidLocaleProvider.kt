package com.aslmmovic.qurancompanion.data.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of [LocaleProvider].
 * Returns the current default locale's language code (e.g. "en", "ar").
 */
class AndroidLocaleProvider(
    private val context: android.content.Context,
    private val storage: KeyValueStorage
) : LocaleProvider {
    private val _currentLocale = MutableStateFlow(
        storage.getString("pref_preferred_language") ?: java.util.Locale.getDefault().language
    )

    override val currentLocale: String
        get() = _currentLocale.value

    override val currentLocaleFlow: StateFlow<String> = _currentLocale.asStateFlow()

    override fun changeLocale(locale: String) {
        val newLocale = java.util.Locale(locale)
        java.util.Locale.setDefault(newLocale)
        
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        _currentLocale.value = locale
    }
}
