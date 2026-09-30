package com.antigravity.bitcoinminingtycoon.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class RootTab(val title: String) {
    MINE("Mine"),
    HARDWARE("Hardware"),
    UPGRADES("Upgrades")
}

@Serializable
sealed interface AppDestination : NavKey {
    @Serializable
    data object Home : AppDestination

    @Serializable
    data object Stats : AppDestination

    @Serializable
    data object Settings : AppDestination

    @Serializable
    data object SatoshiTree : AppDestination
}
