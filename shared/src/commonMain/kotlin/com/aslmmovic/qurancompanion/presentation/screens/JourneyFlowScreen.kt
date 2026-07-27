package com.aslmmovic.qurancompanion.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.Journey
import com.aslmmovic.qurancompanion.domain.model.JourneyStep
import com.aslmmovic.qurancompanion.domain.model.StepType
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.home_loading
import qurancompanion.shared.generated.resources.journey_back
import qurancompanion.shared.generated.resources.journey_finish
import qurancompanion.shared.generated.resources.journey_next
import qurancompanion.shared.generated.resources.journey_step_of
import qurancompanion.shared.generated.resources.step_type_intro
import qurancompanion.shared.generated.resources.step_type_story
import qurancompanion.shared.generated.resources.step_type_key_lessons
import qurancompanion.shared.generated.resources.step_type_reflection
import qurancompanion.shared.generated.resources.step_type_action
import qurancompanion.shared.generated.resources.step_type_references
import androidx.compose.ui.draw.alpha

@Composable
fun JourneyFlowScreen(viewModel: JourneyViewModel) {
    val journey by viewModel.journey.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Progress map + step label
            val steps = journey?.steps.orEmpty()
            val totalSteps = steps.size
            
            if (totalSteps > 0) {
                StepProgressMap(
                    steps = steps,
                    currentStepIndex = currentStepIndex,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val currentStep = steps.getOrNull(currentStepIndex)
                Text(
                    text = "${stringResource(Res.string.journey_step_of, currentStepIndex + 1, totalSteps)}: ${
                        currentStep?.type?.let { getStepTypeLabel(it) }.orEmpty()
                    }",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            } else {
                Text(
                    text = stringResource(Res.string.home_loading),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step card — slides in/out horizontally on step change
            Box(modifier = Modifier.weight(1f)) {
                val currentStep = journey?.steps?.getOrNull(currentStepIndex)
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "step_content"
                ) { _ ->
                    if (currentStep != null) {
                        StepCard(step = currentStep)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom navigation buttons
            val isLastStep = journey?.steps?.let { currentStepIndex == it.size - 1 } ?: false

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = viewModel::onPreviousStep,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = "← ${stringResource(Res.string.journey_back)}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = if (isLastStep) viewModel::onFinish else viewModel::onNextStep,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isLastStep)
                            stringResource(Res.string.journey_finish)
                        else
                            "${stringResource(Res.string.journey_next)} →",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCard(step: JourneyStep) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Step type icon + badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = step.type.emoji, fontSize = 22.sp)
                }

                Column {
                    Text(
                        text = step.type.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            Text(
                text = step.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 28.sp,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepProgressMap(
    steps: List<JourneyStep>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStepIndex
            val isActive = index == currentStepIndex

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = index < steps.lastIndex)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isCompleted -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isCompleted -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.type.emoji,
                        fontSize = 16.sp,
                        modifier = Modifier.alpha(if (isActive || isCompleted) 1f else 0.5f)
                    )
                }

                if (index < steps.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                color = if (isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun getStepTypeLabel(type: StepType): String {
    return when (type) {
        StepType.INTRO -> stringResource(Res.string.step_type_intro)
        StepType.STORY -> stringResource(Res.string.step_type_story)
        StepType.KEY_LESSONS -> stringResource(Res.string.step_type_key_lessons)
        StepType.REFLECTION -> stringResource(Res.string.step_type_reflection)
        StepType.ACTION -> stringResource(Res.string.step_type_action)
        StepType.REFERENCES -> stringResource(Res.string.step_type_references)
    }
}


