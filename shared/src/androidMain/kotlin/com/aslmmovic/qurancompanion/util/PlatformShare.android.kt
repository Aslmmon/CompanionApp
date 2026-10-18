package com.aslmmovic.qurancompanion.util

import android.content.Intent

actual fun shareText(text: String, title: String?) {
    val activity = AndroidActivityProvider.currentActivity ?: return
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        if (title != null) {
            putExtra(Intent.EXTRA_TITLE, title)
        }
    }
    val chooser = Intent.createChooser(sendIntent, title)
    activity.startActivity(chooser)
}
