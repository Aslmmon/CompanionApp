package com.aslmmovic.qurancompanion.util

import com.aslmmovic.qurancompanion.domain.util.AppBuildInfoProvider
import kotlin.experimental.ExperimentalNativeApi
import platform.Foundation.NSBundle

class IosAppBuildInfoProvider : AppBuildInfoProvider {

    private val infoDictionary by lazy {
        NSBundle.mainBundle.infoDictionary
    }

    override val versionName: String
        get() = (infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "1.0.0"

    override val buildNumber: Long
        get() = (infoDictionary?.get("CFBundleVersion") as? String)?.toLongOrNull() ?: 1L

    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean
        get() = kotlin.native.Platform.isDebugBinary
}
