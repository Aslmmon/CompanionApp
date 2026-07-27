package com.aslmmovic.qurancompanion

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.preferredLanguages
import platform.UIKit.UIDevice

import platform.Foundation.NSCalendarUnitWeekday

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val languageCode: String
        get() {
            val preferred = platform.Foundation.NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
            return preferred.split("-").firstOrNull() ?: "en"
        }
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