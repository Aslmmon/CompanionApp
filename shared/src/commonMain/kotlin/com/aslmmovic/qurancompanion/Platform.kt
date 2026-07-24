package com.aslmmovic.qurancompanion

interface Platform {
    val name: String
    val languageCode: String
}

expect fun getPlatform(): Platform