package com.aslmmovic.qurancompanion.presentation.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.presentation.screens.habits.components.ActiveHabitsSection
import com.aslmmovic.qurancompanion.presentation.screens.habits.components.AddHabitBottomSheet
import com.aslmmovic.qurancompanion.presentation.screens.habits.components.AddHabitButton
import com.aslmmovic.qurancompanion.presentation.screens.habits.components.DailyProgressCard
import com.aslmmovic.qurancompanion.presentation.screens.habits.components.HabitDetailBottomSheet
import org.jetbrains.compose.resources.stringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.habits_subtitle
import qurancompanion.shared.generated.resources.habits_title

@Composable
fun HabitsContent(
    uiState: HabitsUiState,
    onToggleHabit: (String) -> Unit,
    onOpenAddSheet: () -> Unit,
    onCloseAddSheet: () -> Unit,
    onCatalogCategorySelected: (HabitCategory) -> Unit,
    onAddHabitFromCatalog: (String) -> Unit,
    onCreateCustomHabit: (String, String, HabitCategory) -> Unit,
    onSelectHabitDetail: (Habit) -> Unit,
    onDismissHabitDetail: () -> Unit,
    onRemoveHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Heading 1 - Deep Andalusian Emerald in Light / Warm Gold in Dark
            Text(
                text = stringResource(Res.string.habits_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtle philosophical subtitle
            Text(
                text = stringResource(Res.string.habits_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Daily Progress Card
            DailyProgressCard(
                progressPercentage = uiState.progressPercentage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Active Habits Checklist
            ActiveHabitsSection(
                habits = uiState.activeHabits,
                completedCount = uiState.completedCount,
                totalCount = uiState.totalCount,
                onToggleHabit = onToggleHabit,
                onSelectHabitDetail = onSelectHabitDetail
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dashed Add Habit Button
            AddHabitButton(
                onClick = onOpenAddSheet
            )

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Add Habit Bottom Sheet
        if (uiState.isAddSheetOpen) {
            AddHabitBottomSheet(
                catalogHabits = uiState.catalogHabits,
                selectedCategory = uiState.selectedCatalogCategory,
                onCategorySelected = onCatalogCategorySelected,
                onAddCatalogHabit = onAddHabitFromCatalog,
                onCreateCustomHabit = onCreateCustomHabit,
                onDismissRequest = onCloseAddSheet
            )
        }

        // Habit Detail Bottom Sheet
        uiState.selectedHabitForDetail?.let { habit ->
            HabitDetailBottomSheet(
                habit = habit,
                onRemoveHabit = onRemoveHabit,
                onDismissRequest = onDismissHabitDetail
            )
        }
    }
}
