package com.aslmmovic.qurancompanion.presentation.screens.habits

sealed interface HabitsUiEffect {
    data object ShowCelebration : HabitsUiEffect
    data class ShowToast(val message: String) : HabitsUiEffect
}
