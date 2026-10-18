package com.aslmmovic.qurancompanion

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.preferredLanguages
import platform.UIKit.UIDevice

import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute

class IOSPlatform: Platform {

    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val languageCode: String
        get() {
            val preferred = platform.Foundation.NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
            return preferred.split("-").firstOrNull() ?: "en"
        }
    @OptIn(kotlin.experimental.ExperimentalNativeApi::class)
    override val isDebug: Boolean
        get() = kotlin.native.Platform.isDebugBinary
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentDayOfYear(): Int {
    val calendar = NSCalendar.currentCalendar
    return calendar.ordinalityOfUnit(
        smaller = NSCalendarUnitDay,
        inUnit = NSCalendarUnitYear,
        forDate = NSDate()
    ).toInt()
}

actual fun getCurrentDayOfWeek(): Int {
    val calendar = NSCalendar.currentCalendar
    val dayOfWeek = calendar.component(NSCalendarUnitWeekday, NSDate()).toInt()
    return if (dayOfWeek == 1) 7 else dayOfWeek - 1
}

actual fun getCurrentDateString(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(NSDate())
}

actual fun getTimeUntilMidnight(): Pair<Int, Int> {
    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    val hour = calendar.component(NSCalendarUnitHour, now).toInt()
    val minute = calendar.component(NSCalendarUnitMinute, now).toInt()
    val remainingMinutes = 1440 - (hour * 60 + minute)
    val hours = (remainingMinutes / 60).coerceIn(0, 24)
    val minutes = (remainingMinutes % 60).coerceIn(0, 59)
    return Pair(hours, minutes)
}

actual fun getCurrentHijriDateString(): String {
    return try {
        val calendar = NSCalendar.calendarWithIdentifier(platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura)
            ?: NSCalendar.calendarWithIdentifier(platform.Foundation.NSCalendarIdentifierIslamic)
            ?: NSCalendar.currentCalendar
        val formatter = NSDateFormatter()
        formatter.calendar = calendar
        formatter.dateFormat = "d MMMM y G"
        formatter.stringFromDate(NSDate()).uppercase()
    } catch (e: Exception) {
        "14 RAMADAN 1447 AH"
    }
}