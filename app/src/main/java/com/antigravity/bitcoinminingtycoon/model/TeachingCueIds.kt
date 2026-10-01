package com.antigravity.bitcoinminingtycoon.model

/** Stable save identifiers for the short first-session cues. */
object TeachingCueIds {
    const val MINE_BITCOIN = "mine_bitcoin"
    const val SELL_BITCOIN = "sell_bitcoin"
    const val BUY_FIRST_MACHINE = "buy_first_machine"
    const val PASSIVE_MINING = "passive_mining"

    val ALL: Set<String> = setOf(MINE_BITCOIN, SELL_BITCOIN, BUY_FIRST_MACHINE, PASSIVE_MINING)
}
