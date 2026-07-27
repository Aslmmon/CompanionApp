package com.aslmmovic.qurancompanion.data.datasource

/**
 * Android implementation of [LocaleProvider].
 * Returns the current default locale's language code (e.g. "en", "ar").
 */
class AndroidLocaleProvider(private val storage: KeyValueStorage) : LocaleProvider {
    override val currentLocale: String
        get() = storage.getString("pref_preferred_language") ?: java.util.Locale.getDefault().language
}
