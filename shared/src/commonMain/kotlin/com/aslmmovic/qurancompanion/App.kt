package com.aslmmovic.qurancompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.presentation.navigation.Screen
import com.aslmmovic.qurancompanion.presentation.screens.SetupScreen
import com.aslmmovic.qurancompanion.presentation.screens.WelcomeScreen
import com.aslmmovic.qurancompanion.presentation.viewmodel.OnboardingViewModel
import com.aslmmovic.qurancompanion.ui.theme.QuranArabicTextStyle
import com.aslmmovic.qurancompanion.ui.theme.QuranCompanionTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.bismillah
import qurancompanion.shared.generated.resources.loading_journey
import qurancompanion.shared.generated.resources.reminders_disabled
import qurancompanion.shared.generated.resources.reminders_enabled_at
import qurancompanion.shared.generated.resources.reminders_label
import qurancompanion.shared.generated.resources.reset_setup
import qurancompanion.shared.generated.resources.welcome_title

@Composable
fun App() {
    QuranCompanionTheme {
        val onboardingViewModel: OnboardingViewModel = koinViewModel()

        val currentScreen by onboardingViewModel.currentScreen.collectAsState()
        val preferences by onboardingViewModel.preferencesState.collectAsState()

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.Welcome -> {
                    WelcomeScreen(
                        onBeginClick = { onboardingViewModel.navigateToSetup() }
                    )
                }
                Screen.Setup -> {
                    SetupScreen(
                        preferences = preferences,
                        onReminderToggle = { onboardingViewModel.updateReminderEnabled(it) },
                        onTimeChange = { h, m -> onboardingViewModel.updateReminderTime(h, m) },
                        onCompleteClick = { onboardingViewModel.completeSetup() }
                    )
                }
                Screen.Home -> {
                    HomeScreen(
                        reminderTime = "${preferences.reminderHour.toString().padStart(2, '0')}:${preferences.reminderMinute.toString().padStart(2, '0')}",
                        isReminderEnabled = preferences.isReminderEnabled,
                        onResetClick = { onboardingViewModel.resetSetup() }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    reminderTime: String,
    isReminderEnabled: Boolean,
    onResetClick: () -> Unit
) {
    val reminderStatus = if (isReminderEnabled)
        stringResource(Res.string.reminders_enabled_at, reminderTime)
    else
        stringResource(Res.string.reminders_disabled)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.bismillah),
                    style = QuranArabicTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.loading_journey),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stringResource(Res.string.reminders_label)} $reminderStatus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onResetClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(Res.string.reset_setup))
        }
    }
}