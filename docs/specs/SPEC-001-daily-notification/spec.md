# SPEC-001: Daily Companion Reminder Notification (8:00 AM)

## Status
- [ ] Draft
- [ ] In Review (Spec-Driven Development)
- [x] Approved (Frozen)
- [x] Implemented
- [x] Verified

---

## 1. Executive Summary & Intent
- **Feature Overview**: Schedules a local system notification delivered everyday at **8:00 AM** to nudge users to explore their daily Islamic Sahaba journey.
- **User Story**: *As a Muslim user striving to maintain a daily habit, I want to receive a daily reminder at 8:00 AM with today's companion topic so that I never miss my daily learning journey.*
- **Success Metrics**: 
  - 100% offline local scheduling (zero network or backend dependency).
  - Reliable daily trigger at 8:00 AM local time across Android and iOS.
  - One-tap navigation directly into the app.

---

## 2. Scope Boundaries (Anti-Hallucination Guardrails)

### In-Scope (Explicit Deliverables)
- Default schedule set to **08:00 AM** (hour: 8, minute: 0).
- Toggle switch in Settings BottomSheet to enable/disable notifications.
- Option to select preferred reminder time presets (8:00 AM [Default], 1:00 PM, 8:00 PM).
- Dynamic notification content pulling today's Sahabi journey title and subtitle.
- Platform bridges:
  - Android: `AlarmManager` with `BroadcastReceiver` (`DailyReminderReceiver`) + NotificationChannel + Android 13 `POST_NOTIFICATIONS` permission handling.
  - iOS: `UNUserNotificationCenter` with `UNCalendarNotificationTrigger` repeating daily at 08:00 + notification authorization requests.
- Full localization in English and Arabic.

### Out-of-Scope / Non-Goals (Strict Prohibitions)
> [!IMPORTANT]
> The implementation MUST NOT include or introduce any of the following:
- No remote Push Notification services (FCM, OneSignal, APNs remote pushes).
- No backend server or network polling.
- No third-party notification SDKs (only standard Android SDK & Apple UserNotifications framework).
- No silent background fetches or waking tasks unrelated to the 8:00 AM local alarm.

---

## 3. Domain Contracts & Pure Business Logic

### 3.1 Domain Model (`domain/model/UserPreferences.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.model

data class UserPreferences(
    val reminderHour: Int = 8,      // Default: 8 AM
    val reminderMinute: Int = 0,    // Default: :00
    val isReminderEnabled: Boolean = true,
    val preferredLanguage: String? = null,
    val isDarkMode: Boolean? = null
)
```

### 3.2 Platform Abstraction Contract (`domain/util/NotificationScheduler.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.util

interface NotificationScheduler {
    fun scheduleDailyReminder(hour: Int, minute: Int, title: String, body: String)
    fun cancelDailyReminder()
    suspend fun requestNotificationPermission(): Boolean
}
```

### 3.3 Use Case Contracts (`domain/usecase/NotificationUseCases.kt`)
```kotlin
package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.domain.util.NotificationScheduler
import kotlinx.coroutines.flow.first

class ScheduleDailyReminderUseCase(
    private val notificationScheduler: NotificationScheduler,
    private val preferencesRepository: UserPreferencesRepository,
    private val journeyRepository: JourneyRepository
) {
    suspend operator fun invoke(customTitle: String? = null, customBody: String? = null) {
        val preferences = preferencesRepository.getUserPreferences().first()
        if (!preferences.isReminderEnabled) {
            notificationScheduler.cancelDailyReminder()
            return
        }

        val todayJourney = journeyRepository.getTodayJourney()
        val title = customTitle ?: if (todayJourney != null) {
            "Sahaba Companion: ${todayJourney.title}"
        } else {
            "Sahaba Companion"
        }
        val body = customBody ?: (todayJourney?.subtitle ?: "Discover today's journey with the Sahaba.")

        notificationScheduler.scheduleDailyReminder(
            hour = preferences.reminderHour,
            minute = preferences.reminderMinute,
            title = title,
            body = body
        )
    }
}

class RequestNotificationPermissionUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(): Boolean {
        return notificationScheduler.requestNotificationPermission()
    }
}
```

---

## 4. Presentation & Settings Contract

### 4.1 Settings BottomSheet (`presentation/screens/home/components/SettingsBottomSheet.kt`)
- **Toggle switch**: Triggers `onToggleReminder(Boolean)` $\rightarrow$ requests permission if enabling, saves preference, calls `ScheduleDailyReminderUseCase()`.
- **Time options**: 
  - `08:00 AM` (Default)
  - `01:00 PM` (Afternoon)
  - `08:00 PM` (Evening)
- Selection triggers `onUpdateReminderTime(hour, minute)` $\rightarrow$ updates preferences, reschedules alarm.

---

## 5. Acceptance Criteria & Test Matrix

| Scenario ID | Precondition (Given) | Trigger / Action (When) | Expected Result / State (Then) | Target Layer |
|:---|:---|:---|:---|:---|
| **AC-01** | App starts with default preferences | `HomeViewModel.init` runs | Schedules reminder for `hour = 8, minute = 0` with today's Sahaba title/subtitle | Presentation / Domain |
| **AC-02** | User disables notifications in Settings | `onToggleReminder(false)` called | `isReminderEnabled = false` saved, `cancelDailyReminder()` called | Domain / Data |
| **AC-03** | User re-enables notifications | `onToggleReminder(true)` called | `requestNotificationPermission()` invoked, alarm scheduled for 8:00 AM | Presentation / Platform |
| **AC-04** | User changes time to 8:00 PM | `onUpdateReminderTime(20, 0)` called | `reminderHour = 20` saved, rescheduled for 20:00 | Domain / Presentation |
| **AC-05** | Android alarm fires at 8:00 AM | `DailyReminderReceiver.onReceive()` triggered | Notification posted to `daily_reminder_channel` with tap intent to `MainActivity` | Android Platform |
| **AC-06** | iOS calendar trigger hits 8:00 AM | `UNCalendarNotificationTrigger` fires | Local banner shown with title & body | iOS Platform |

---

## 6. Multiplatform Localization Keys

| Key Identifier | English (`values/strings.xml`) | Arabic (`values-ar/strings.xml`) |
|:---|:---|:---|
| `settings_daily_reminders` | "Daily Reminders" | "التذكيرات اليومية" |
| `settings_reminder_time` | "Reminder Time" | "وقت التذكير" |
| `reminder_time_8am` | "8:00 AM (Morning)" | "8:00 صباحاً (صباحاً)" |
| `reminder_time_1pm` | "1:00 PM (Afternoon)" | "1:00 مساءً (ظهراً)" |
| `reminder_time_8pm` | "8:00 PM (Evening)" | "8:00 مساءً (مساءً)" |
| `notification_default_title` | "Sahaba Companion" | "رفيق الصحابة" |
| `notification_default_body` | "Time for your daily Sahabi discovery." | "حان وقت رحلتك اليومية مع الصحابة." |
