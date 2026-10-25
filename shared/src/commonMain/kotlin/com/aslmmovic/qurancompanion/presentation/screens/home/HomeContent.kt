package com.aslmmovic.qurancompanion.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.presentation.screens.home.components.CuratedJourneyHeroCard
import com.aslmmovic.qurancompanion.presentation.screens.home.components.DateSubHeader
import com.aslmmovic.qurancompanion.presentation.screens.home.components.HomeBrandHeader
import com.aslmmovic.qurancompanion.presentation.screens.home.components.SunnahHabitTeaserCard
import com.aslmmovic.qurancompanion.presentation.screens.home.components.TenPromisedSeriesRow
import com.aslmmovic.qurancompanion.presentation.screens.home.components.WeeklyStreakCalendar

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onBeginClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSahabaClick: (Sahaba) -> Unit,
    onViewAllTenPromisedClick: () -> Unit,
    onToggleHabitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeBrandHeader(
                streakCount = uiState.streakCount,
                onNotificationClick = onNotificationClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            DateSubHeader(
                hijriDate = uiState.hijriDate
            )

            Spacer(modifier = Modifier.height(12.dp))

            CuratedJourneyHeroCard(
                journey = uiState.journey,
                isCompleted = uiState.isCompleted,
                isBookmarked = uiState.isBookmarked,
                onBeginClick = onBeginClick,
                onBookmarkClick = onBookmarkClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            WeeklyStreakCalendar(
                weeklyProgress = uiState.weeklyProgress,
                streakCount = uiState.streakCount,
                currentDayOfWeek = uiState.currentDayOfWeek
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.tenPromisedCompanions.isNotEmpty()) {
                TenPromisedSeriesRow(
                    companions = uiState.tenPromisedCompanions,
                    onSahabaClick = onSahabaClick,
                    onViewAllClick = onViewAllTenPromisedClick
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            SunnahHabitTeaserCard(
                habit = uiState.todayHabit,
                isCompleted = uiState.isHabitCompleted,
                onToggleClick = onToggleHabitClick
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
