package com.aslmmovic.qurancompanion.presentation.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.reminder_time_6am
import qurancompanion.shared.generated.resources.reminder_time_8am
import qurancompanion.shared.generated.resources.reminder_time_8pm
import qurancompanion.shared.generated.resources.settings_daily_reminders
import qurancompanion.shared.generated.resources.settings_reminder_time

@Composable
fun ReminderSettingsSection(
    isReminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onToggleReminder: (Boolean) -> Unit,
    onUpdateReminderTime: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.settings_daily_reminders),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = onToggleReminder,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            if (isReminderEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.settings_reminder_time),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TimeOptionRow(
                    label = stringResource(Res.string.reminder_time_8am),
                    isSelected = reminderHour == 8 && reminderMinute == 0,
                    onClick = { onUpdateReminderTime(8, 0) }
                )

                TimeOptionRow(
                    label = stringResource(Res.string.reminder_time_6am),
                    isSelected = reminderHour == 6 && reminderMinute == 0,
                    onClick = { onUpdateReminderTime(6, 0) }
                )

                TimeOptionRow(
                    label = stringResource(Res.string.reminder_time_8pm),
                    isSelected = reminderHour == 20 && reminderMinute == 0,
                    onClick = { onUpdateReminderTime(20, 0) }
                )
            }
        }
    }
}
