package com.aslmmovic.qurancompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Fetch and apply current persisted locale
        val localeProvider: com.aslmmovic.qurancompanion.data.datasource.LocaleProvider = 
            org.koin.mp.KoinPlatform.getKoin().get()
        localeProvider.changeLocale(localeProvider.currentLocale)

        val userPreferencesRepository: com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository = 
            org.koin.mp.KoinPlatform.getKoin().get()

        // Monitor language changes dynamically and recreate the activity to reload resources configuration
        lifecycleScope.launch {
            var currentLanguage: String? = null
            userPreferencesRepository.getUserPreferences().collect { prefs ->
                if (currentLanguage != null && currentLanguage != prefs.preferredLanguage) {
                    recreate()
                }
                currentLanguage = prefs.preferredLanguage
            }
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}