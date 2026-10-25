package com.aslmmovic.qurancompanion.util

/**
 * Platform bridge for launching the native system share sheet.
 */
expect fun shareText(text: String, title: String? = null)
