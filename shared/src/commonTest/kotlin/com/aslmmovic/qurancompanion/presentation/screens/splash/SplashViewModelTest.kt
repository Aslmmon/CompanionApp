package com.aslmmovic.qurancompanion.presentation.screens.splash

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SplashViewModel emits NavigateNext after exactly 3000ms delay`() = runTest(testDispatcher) {
        val viewModel = SplashViewModel()
        viewModel.uiEffects.test {
            testScheduler.advanceTimeBy(2999)
            expectNoEvents()

            testScheduler.advanceTimeBy(1)
            val effect = awaitItem()
            assertEquals(SplashUiEffect.NavigateNext, effect)
            assertEquals(3000L, testScheduler.currentTime)
            expectNoEvents()
        }
    }
}
