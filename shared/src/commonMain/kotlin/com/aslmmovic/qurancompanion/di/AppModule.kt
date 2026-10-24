package com.aslmmovic.qurancompanion.di

import com.aslmmovic.qurancompanion.data.datasource.JourneyLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.ResourceJourneyLocalDataSource
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
import com.aslmmovic.qurancompanion.domain.usecase.GetTomorrowJourneyUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetWeeklyProgressUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.IncrementDebugDayOffsetUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RequestNotificationPermissionUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ScheduleDailyReminderUseCase
import com.aslmmovic.qurancompanion.domain.usecase.SchedulePeriodicReminderUseCase
import com.aslmmovic.qurancompanion.domain.usecase.TriggerImmediateNotificationUseCase
import com.aslmmovic.qurancompanion.presentation.screens.home.HomeViewModel
import com.aslmmovic.qurancompanion.presentation.screens.journey.JourneyViewModel
import com.aslmmovic.qurancompanion.presentation.screens.language.LanguageViewModel
import com.aslmmovic.qurancompanion.presentation.screens.settings.SettingsViewModel
import com.aslmmovic.qurancompanion.presentation.screens.splash.SplashViewModel
import com.aslmmovic.qurancompanion.presentation.viewmodel.AppViewModel
import com.aslmmovic.qurancompanion.data.datasource.SahabaLocalDataSource
import com.aslmmovic.qurancompanion.data.datasource.ResourceSahabaLocalDataSource
import com.aslmmovic.qurancompanion.data.repository.SahabaRepositoryImpl
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import com.aslmmovic.qurancompanion.domain.usecase.GetSahabaCatalogUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetSahabaDetailUseCase
import com.aslmmovic.qurancompanion.presentation.screens.library.LibraryViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // JSON parser — shared across data sources
    single { Json { ignoreUnknownKeys = true } }

    // Data layer — data sources
    single<JourneyLocalDataSource> { ResourceJourneyLocalDataSource(get()) }
    single<SahabaLocalDataSource> { ResourceSahabaLocalDataSource(get()) }

    // DateTime Provider
    single<com.aslmmovic.qurancompanion.domain.util.DateTimeProvider> { com.aslmmovic.qurancompanion.domain.util.SystemDateTimeProvider() }

    // Data layer — repositories
    // KeyValueStorage and LocaleProvider are provided by platform-specific modules
    single { UserPreferencesRepositoryImpl(get()) } bind UserPreferencesRepository::class
    single { JourneyRepositoryImpl(get(), get(), get(), get()) } bind JourneyRepository::class
    single { SahabaRepositoryImpl(get(), get()) } bind SahabaRepository::class

    // Domain layer — preferences
    single { GetUserPreferencesUseCase(get()) }
    single { SavePreferencesUseCase(get()) }

    // Domain layer — journey
    single { GetTodayJourneyUseCase(get()) }
    single { GetTomorrowJourneyUseCase(get()) }
    single { GetWeeklyProgressUseCase(get()) }
    single { IsJourneyCompletedUseCase(get()) }
    single { MarkJourneyCompletedUseCase(get()) }
    single { ResetJourneyUseCase(get()) }
    single { GetDebugDayOffsetUseCase(get()) }
    single { IncrementDebugDayOffsetUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.GetBookmarkedJourneyIdsUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.ToggleJourneyBookmarkUseCase(get()) }

    // Domain layer — sahaba
    single { GetSahabaCatalogUseCase(get()) }
    single { GetSahabaDetailUseCase(get()) }

    // Data layer — habits
    single<com.aslmmovic.qurancompanion.data.datasource.HabitLocalDataSource> {
        com.aslmmovic.qurancompanion.data.datasource.ResourceHabitLocalDataSource(get())
    }
    single {
        com.aslmmovic.qurancompanion.data.repository.HabitRepositoryImpl(get(), get(), get(), get(), get())
    } bind com.aslmmovic.qurancompanion.domain.repository.HabitRepository::class

    // Domain layer — habits
    single { com.aslmmovic.qurancompanion.domain.usecase.GetActiveHabitsUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.GetCuratedHabitsUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.ToggleHabitCompletionUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.AddHabitFromCatalogUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.CreateCustomHabitUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.RemoveHabitUseCase(get()) }

    // Domain layer — notifications
    single { ScheduleDailyReminderUseCase(get(), get(), get()) }
    single { SchedulePeriodicReminderUseCase(get(), get(), get()) }
    single { RequestNotificationPermissionUseCase(get()) }
    single { TriggerImmediateNotificationUseCase(get(), get(), get()) }

    // Domain layer — purchases & entitlements
    single { com.aslmmovic.qurancompanion.domain.usecase.GetEntitlementsUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.GetProductsUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.PurchaseProductUseCase(get()) }
    single { com.aslmmovic.qurancompanion.domain.usecase.RestorePurchasesUseCase(get()) }

    // Presentation layer
    viewModel { SplashViewModel() }
    viewModel { AppViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LanguageViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { JourneyViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LibraryViewModel(get()) }
    viewModel { com.aslmmovic.qurancompanion.presentation.screens.paywall.PaywallViewModel(get(), get(), get()) }
    viewModel {
        com.aslmmovic.qurancompanion.presentation.screens.habits.HabitsViewModel(
            get(), get(), get(), get(), get(), get()
        )
    }
}
