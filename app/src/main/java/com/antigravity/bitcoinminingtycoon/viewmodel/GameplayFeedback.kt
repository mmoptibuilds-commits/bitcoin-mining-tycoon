package com.antigravity.bitcoinminingtycoon.viewmodel

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class MiningFeedbackEvent(val sequence: Long, val btcDelta: String)

/** Ephemeral visual feedback; slow scenes retain only a bounded tail of mining deltas. */
class GameplayFeedbackBus {
    private val mutableEvents = MutableSharedFlow<MiningFeedbackEvent>(
        replay = 0,
        extraBufferCapacity = MAX_PENDING_EVENTS,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = mutableEvents.asSharedFlow()

    fun publish(event: MiningFeedbackEvent) {
        mutableEvents.tryEmit(event)
    }

    companion object {
        const val MAX_PENDING_EVENTS = 24
    }
}
