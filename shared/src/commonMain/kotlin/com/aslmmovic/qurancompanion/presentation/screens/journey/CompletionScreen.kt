package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aslmmovic.qurancompanion.util.shareText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.completion_share_description
import qurancompanion.shared.generated.resources.completion_share_template

@Composable
fun CompletionScreen(
    onNavigateToHome: () -> Unit,
    viewModel: JourneyViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEffects) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                JourneyUiEffect.NavigateToHome          -> onNavigateToHome()
                JourneyUiEffect.NavigateToCompletion    -> { /* already here */ }
                is JourneyUiEffect.ShowBookmarkFeedback -> { /* no-op in completion */ }
            }
        }
    }

    val shareTemplate = stringResource(Res.string.completion_share_template)
    val shareTitle = stringResource(Res.string.completion_share_description)

    CompletionContent(
        journey = uiState.journey,
        hoursUntilNextJourney = uiState.hoursUntilMidnight,
        minutesUntilNextJourney = uiState.minutesUntilMidnight,
        onReturnHome = viewModel::onReturnHome,
        onShareQuote = { quote, author ->
            val formattedShareText = shareTemplate
                .replace("%1\$s", quote)
                .replace("%2\$s", author)
            shareText(text = formattedShareText, title = shareTitle)
        }

    )
}
