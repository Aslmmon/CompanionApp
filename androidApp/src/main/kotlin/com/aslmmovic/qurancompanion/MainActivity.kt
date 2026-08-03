package com.aslmmovic.qurancompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val localeProvider: LocaleProvider by inject()
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Fetch and apply current persisted locale
        localeProvider.changeLocale(localeProvider.currentLocale)

        // Monitor language changes dynamically and recreate the activity to reload resources configuration
        lifecycleScope.launch {
            var currentLanguage: String? = null
            getUserPreferencesUseCase().collect { prefs ->
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