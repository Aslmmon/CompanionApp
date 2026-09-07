package com.aslmmovic.qurancompanion.presentation.screens.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_app_logo
import qurancompanion.shared.generated.resources.shalat

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                SplashUiEffect.NavigateNext -> onNavigateNext()
            }
        }
    }

    SplashContent(
        painter = painterResource(Res.drawable.shalat),
    )
}
