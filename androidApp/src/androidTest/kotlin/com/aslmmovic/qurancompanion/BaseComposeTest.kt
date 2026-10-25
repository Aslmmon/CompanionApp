package com.aslmmovic.qurancompanion

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aslmmovic.qurancompanion.di.TestKoinModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@RunWith(AndroidJUnit4::class)
abstract class BaseComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    open fun setUp() {
        TestKoinModule.reset()
        loadKoinModules(TestKoinModule.module)
    }

    @After
    open fun tearDown() {
        unloadKoinModules(TestKoinModule.module)
    }
}
