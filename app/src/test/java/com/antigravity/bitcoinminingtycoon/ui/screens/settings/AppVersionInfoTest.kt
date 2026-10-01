package com.antigravity.bitcoinminingtycoon.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionInfoTest {

    @Test
    fun displayedVersionUsesInstalledPackageMetadata() {
        val info = AppVersionInfo(
            packageName = "com.antigravity.bitcoinminingtycoon",
            versionName = "1.2",
            versionCode = 3,
            minimumAndroidApi = 31,
            buildVariant = "debug"
        )

        assertEquals("1.2 (3)", info.versionLabel)
        assertEquals("com.antigravity.bitcoinminingtycoon", info.packageName)
        assertEquals(31, info.minimumAndroidApi)
    }
}
