package com.aslmmovic.qurancompanion

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val languageCode: String
        get() = java.util.Locale.getDefault().language
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentDayOfYear(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)

actual fun getCurrentDayOfWeek(): Int {
    val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    return if (dayOfWeek == java.util.Calendar.SUNDAY) 7 else dayOfWeek - 1
}

actual fun getCurrentDateString(): String {
    val cal = java.util.Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        cal.get(java.util.Calendar.YEAR),
        cal.get(java.util.Calendar.MONTH) + 1,
        cal.get(java.util.Calendar.DAY_OF_MONTH)
    )
}