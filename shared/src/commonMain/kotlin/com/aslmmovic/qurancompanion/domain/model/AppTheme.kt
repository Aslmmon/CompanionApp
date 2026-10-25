package com.aslmmovic.qurancompanion.domain.model

enum class AppTheme(val id: String, val isPremium: Boolean) {
    CLASSIC("classic", isPremium = false),
    MUSHAF_PARCHMENT("parchment", isPremium = true),
    ANDALUS_EMERALD("andalus", isPremium = true),
    DARK_OLED("oled", isPremium = true);

    companion object {
        fun fromId(id: String?): AppTheme = entries.find { it.id == id } ?: CLASSIC
    }
}
