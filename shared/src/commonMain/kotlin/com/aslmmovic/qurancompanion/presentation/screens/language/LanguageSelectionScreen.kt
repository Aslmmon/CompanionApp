package com.aslmmovic.qurancompanion.presentation.screens.language

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguageSelectionScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LanguageViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                LanguageUiEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    LanguageSelectionContent(
        onLanguageSelected = viewModel::selectLanguage,
    )
}
