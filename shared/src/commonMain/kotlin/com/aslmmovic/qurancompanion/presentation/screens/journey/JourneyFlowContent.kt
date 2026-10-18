package com.aslmmovic.qurancompanion.presentation.screens.journey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.JourneySegmentedProgressBar
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.JourneyTopAppBar
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.SacredStepHeader
import com.aslmmovic.qurancompanion.presentation.screens.journey.components.StepBodyContent
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_loading
import qurancompanion.shared.generated.resources.journey_complete
import qurancompanion.shared.generated.resources.journey_continue

@Composable
fun JourneyFlowContent(
    journey: Journey?,
    currentStepIndex: Int,
    isBookmarked: Boolean,
    isLoading: Boolean,
    onBackPressed: () -> Unit,
    onToggleBookmark: () -> Unit,
    onNextStep: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = journey?.steps.orEmpty()
    val totalSteps = steps.size
    val isLastStep = totalSteps > 0 && currentStepIndex == totalSteps - 1
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        JourneyTopAppBar(
            title = journey?.title.orEmpty(),
            arabicVerse = journey?.arabicVerse,
            isBookmarked = isBookmarked,
            onBackPressed = onBackPressed,
            onToggleBookmark = onToggleBookmark
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Segmented Progress Bar (5 horizontal segments)
        if (totalSteps > 0) {
            JourneySegmentedProgressBar(
                totalSteps = totalSteps,
                currentStepIndex = currentStepIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Reading Content or Loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.home_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (steps.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        val sign = if (isRtl) -1 else 1
                        if (targetState > initialState) {
                            slideInHorizontally { sign * it } + fadeIn() togetherWith
                                    slideOutHorizontally { -sign * it } + fadeOut()
                        } else {
                            slideInHorizontally { -sign * it } + fadeIn() togetherWith
                                    slideOutHorizontally { sign * it } + fadeOut()
                        }
                    },
                    label = "step_content_slide"
                ) { stepIndex ->
                    val step = steps.getOrNull(stepIndex)
                    if (step != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SacredStepHeader()
                            Spacer(modifier = Modifier.height(16.dp))
                            StepBodyContent(
                                step = step,
                                companionName = journey?.person ?: journey?.title,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom Action Button
        if (totalSteps > 0 && !isLoading) {
            Button(
                onClick = if (isLastStep) onFinish else onNextStep,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .height(54.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLastStep)
                            stringResource(Res.string.journey_complete)
                        else
                            stringResource(Res.string.journey_continue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRtl) "←" else "→",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
