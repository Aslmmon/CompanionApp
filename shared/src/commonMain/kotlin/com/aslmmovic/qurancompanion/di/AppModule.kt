package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.repository.JourneyRepositoryImpl
import com.aslmmovic.qurancompanion.data.repository.UserPreferencesRepositoryImpl
import com.aslmmovic.qurancompanion.domain.repository.JourneyRepository
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.domain.usecase.GetTodayJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IsJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.MarkJourneyCompletedUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ResetJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.presentation.navigation.NavigationManager
import com.aslmmovic.qurancompanion.presentation.viewmodel.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.JourneyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Data layer — KeyValueStorage is provided by platform-specific modules
    single { UserPreferencesRepositoryImpl(get()) } bind UserPreferencesRepository::class
    single { JourneyRepositoryImpl(get()) } bind JourneyRepository::class

    // Domain layer — preferences
    single { GetUserPreferencesUseCase(get()) }
    single { SavePreferencesUseCase(get()) }

    // Domain layer — journey
    single { GetTodayJourneyUseCase(get()) }
    single { IsJourneyCompletedUseCase(get()) }
    single { MarkJourneyCompletedUseCase(get()) }
    single { ResetJourneyUseCase(get()) }

    // Navigation
    single { NavigationManager() }

    // Presentation layer
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { JourneyViewModel(get(), get(), get()) }
}
