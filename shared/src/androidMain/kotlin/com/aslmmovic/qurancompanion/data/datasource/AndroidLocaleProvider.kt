package com.aslmmovic.qurancompanion.data.datasource

/**
 * Android implementation of [LocaleProvider].
 * Returns the current default locale's language code (e.g. "en", "ar").
 */
class AndroidLocaleProvider(
    private val context: android.content.Context,
    private val storage: KeyValueStorage
) : LocaleProvider {
    override val currentLocale: String
        get() = storage.getString("pref_preferred_language") ?: java.util.Locale.getDefault().language

    override fun changeLocale(locale: String) {
        val newLocale = java.util.Locale(locale)
        java.util.Locale.setDefault(newLocale)
        
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(newLocale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
