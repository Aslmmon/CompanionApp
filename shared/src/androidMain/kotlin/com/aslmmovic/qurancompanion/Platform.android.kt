package com.aslmmovic.qurancompanion

import android.content.pm.ApplicationInfo
import android.os.Build
import com.aslmmovic.qurancompanion.util.AndroidActivityProvider

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val languageCode: String
        get() = java.util.Locale.getDefault().language
    override val isDebug: Boolean
        get() = AndroidActivityProvider.currentActivity?.applicationInfo?.let {
            (it.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } ?: false
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

actual fun getTimeUntilMidnight(): Pair<Int, Int> {
    val cal = java.util.Calendar.getInstance()
    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = cal.get(java.util.Calendar.MINUTE)
    val remainingMinutes = 1440 - (hour * 60 + minute)
    val hours = (remainingMinutes / 60).coerceIn(0, 24)
    val minutes = (remainingMinutes % 60).coerceIn(0, 59)
    return Pair(hours, minutes)
}

actual fun getCurrentHijriDateString(): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hijrahDate = java.time.chrono.HijrahDate.now()
            val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM y G", java.util.Locale.getDefault())
            hijrahDate.format(formatter).uppercase()
        } else {
            "14 RAMADAN 1447 AH"
        }
    } catch (e: Exception) {
        "14 RAMADAN 1447 AH"
    }
}