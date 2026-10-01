package com.antigravity.bitcoinminingtycoon.viewmodel

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameplayFeedbackBusTest {

    @Test
    fun slowFeedbackConsumerKeepsOnlyTheLatestTwentyFourPendingEvents() = runTest {
        val bus = GameplayFeedbackBus()
        val firstEventReceived = CompletableDeferred<Unit>()
        val resumeCollector = CompletableDeferred<Unit>()
        val received = mutableListOf<Long>()
        val collector = launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
            bus.events.collect { event ->
                received += event.sequence
                if (received.size == 1) {
                    firstEventReceived.complete(Unit)
                    resumeCollector.await()
                }
            }
        }

        bus.publish(MiningFeedbackEvent(sequence = 0L, btcDelta = "0.00000001"))
        runCurrent()
        assertTrue(firstEventReceived.isCompleted)
        repeat(40) { index ->
            bus.publish(MiningFeedbackEvent(sequence = index + 1L, btcDelta = "0.00000001"))
        }

        resumeCollector.complete(Unit)
        runCurrent()
        collector.cancelAndJoin()

        assertEquals(listOf(0L) + (17L..40L).toList(), received)
        assertEquals(GameplayFeedbackBus.MAX_PENDING_EVENTS, received.size - 1)
    }
}
