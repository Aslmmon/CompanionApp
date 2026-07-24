package com.aslmmovic.qurancompanion.data.datasource

import android.content.Context

class AndroidKeyValueStorage(context: Context) : KeyValueStorage {
    private val prefs = context.getSharedPreferences("quran_companion_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = prefs.getBoolean(key, defaultValue)
    override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    override fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)
    override fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
}
