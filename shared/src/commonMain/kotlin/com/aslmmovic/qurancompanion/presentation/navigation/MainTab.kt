package com.aslmmovic.qurancompanion.presentation.navigation

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import qurancompanion.shared.generated.resources.Res
import qurancompanion.shared.generated.resources.ic_tab_habits
import qurancompanion.shared.generated.resources.ic_tab_library
import qurancompanion.shared.generated.resources.ic_tab_settings
import qurancompanion.shared.generated.resources.ic_tab_today
import qurancompanion.shared.generated.resources.tab_habits
import qurancompanion.shared.generated.resources.tab_library
import qurancompanion.shared.generated.resources.tab_settings
import qurancompanion.shared.generated.resources.tab_today

/**
 * Navigation tabs for the persistent bottom bar scaffold.
 */
enum class MainTab(
    val route: String,
    val titleRes: StringResource,
    val iconRes: DrawableResource
) {
    Today("main/today", Res.string.tab_today, Res.drawable.ic_tab_today),
    Library("main/library", Res.string.tab_library, Res.drawable.ic_tab_library),
    Habits("main/habits", Res.string.tab_habits, Res.drawable.ic_tab_habits),
    Settings("main/settings", Res.string.tab_settings, Res.drawable.ic_tab_settings);

    companion object {
        val startTab: MainTab = Today
    }
}
