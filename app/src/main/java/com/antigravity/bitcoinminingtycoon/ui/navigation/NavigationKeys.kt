package com.antigravity.bitcoinminingtycoon.ui.navigation

enum class RootTab(val title: String) {
    MINE("Mine"),
    HARDWARE("Hardware"),
    UPGRADES("Upgrades"),
    STATS("Stats")
}

sealed interface AppDestination {
    data object MainTabs : AppDestination
    data object Settings : AppDestination
    data object SatoshiTree : AppDestination
}
