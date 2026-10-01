package com.antigravity.bitcoinminingtycoon.ui

import android.content.pm.ApplicationInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IdentityUpdateTest {

    @Test
    fun installedCandidateKeepsNativePackageAndOfflineIdentity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)

        assertEquals("com.antigravity.bitcoinminingtycoon", context.packageName)
        assertEquals("1.2.0", packageInfo.versionName)
        assertEquals(2L, packageInfo.longVersionCode)
        assertEquals("Bitcoin Mining Tycoon", packageManager.getApplicationLabel(applicationInfo))
        assertNotNull(packageManager.getApplicationIcon(applicationInfo))
        assertFalse(applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0)
        assertFalse(
            packageInfo.requestedPermissions.orEmpty().contains("android.permission.INTERNET")
        )
    }
}
