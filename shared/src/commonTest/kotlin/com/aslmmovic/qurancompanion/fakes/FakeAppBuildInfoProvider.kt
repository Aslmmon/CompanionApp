package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.util.AppBuildInfoProvider

class FakeAppBuildInfoProvider(
    override var versionName: String = "1.0.0",
    override var buildNumber: Long = 1L,
    override var isDebug: Boolean = false
) : AppBuildInfoProvider
