package com.antigravity.bitcoinminingtycoon.ui.screens.settings

data class AppVersionInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minimumAndroidApi: Int,
    val buildVariant: String
) {
    val versionLabel: String get() = "$versionName ($versionCode)"
}
