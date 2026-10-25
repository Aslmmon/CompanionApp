package com.aslmmovic.qurancompanion.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

actual fun shareText(text: String, title: String?) {
    val activityController = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )
    val rootViewController = getRootViewController() ?: return
    rootViewController.presentViewController(
        viewControllerToPresent = activityController,
        animated = true,
        completion = null
    )
}

@Suppress("DEPRECATION")
private fun getRootViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var topController = window?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}
