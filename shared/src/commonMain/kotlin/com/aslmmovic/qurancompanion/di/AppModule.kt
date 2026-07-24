package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.repository.UserPreferencesRepositoryImpl
import com.aslmmovic.qurancompanion.domain.repository.UserPreferencesRepository
import com.aslmmovic.qurancompanion.domain.usecase.GetUserPreferencesUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SavePreferencesUseCase
import com.aslmmovic.qurancompanion.presentation.viewmodel.OnboardingViewModel
import com.aslmmovic.qurancompanion.presentation.navigation.NavigationManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Data layer — KeyValueStorage is provided by platform-specific modules
    single { UserPreferencesRepositoryImpl(get()) } bind UserPreferencesRepository::class

    // Domain layer
    single { GetUserPreferencesUseCase(get()) }
    single { SavePreferencesUseCase(get()) }
    single { NavigationManager() }
    // Presentation layer — provide NavigationManager singleton
    viewModel { OnboardingViewModel(get(), get(), get()) }
}
