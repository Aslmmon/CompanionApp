package com.aslmmovic.qurancompanion.domain.util

interface AppBuildInfoProvider {
    val versionName: String
    val buildNumber: Long
    val isDebug: Boolean
    val buildType: String
        get() = if (isDebug) "Debug" else "Release"
}
