package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.DailyReflectionCard
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.NextJourneyCountdown
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.completion_done_button
import qurancompanion.shared.generated.resources.completion_heading
import qurancompanion.shared.generated.resources.completion_peace_subtitle
import qurancompanion.shared.generated.resources.ic_check

@Composable
fun CompletionContent(
    journey: Journey?,
    hoursUntilNextJourney: Int,
    minutesUntilNextJourney: Int,
    onReturnHome: () -> Unit,
    onShareQuote: (quote: String, author: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Circular Checkmark Badge with smooth scale-in animation
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Heading & Subtitle
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) +
                        slideInVertically(
                            animationSpec = tween(600, delayMillis = 100),
                            initialOffsetY = { it / 4 }
                        )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.completion_heading),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(Res.string.completion_peace_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Daily Reflection Card, Action Button, and Next Journey Countdown
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                        slideInVertically(
                            animationSpec = tween(600, delayMillis = 200),
                            initialOffsetY = { it / 3 }
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DailyReflectionCard(
                        quote = journey?.heroQuote.orEmpty(),
                        person = journey?.person ?: journey?.title.orEmpty(),
                        dayNumber = journey?.dayNumber ?: 1,
                        onShareClick = {
                            onShareQuote(
                                journey?.heroQuote.orEmpty(),
                                journey?.person ?: journey?.title.orEmpty()
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onReturnHome,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.completion_done_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    NextJourneyCountdown(
                        hoursRemaining = hoursUntilNextJourney,
                        minutesRemaining = minutesUntilNextJourney
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Preview
@Composable
fun CompletionContentPreview() {
    CompletionContent(
        journey = null,
        hoursUntilNextJourney = 14,
        minutesUntilNextJourney = 22,
        onReturnHome = {},
        onShareQuote = { _, _ -> }
    )
}
