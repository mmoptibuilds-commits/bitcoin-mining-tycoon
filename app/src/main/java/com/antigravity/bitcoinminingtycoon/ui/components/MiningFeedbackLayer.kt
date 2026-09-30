package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.util.NumberFormatPreference
import com.antigravity.bitcoinminingtycoon.util.NumberFormatter
import com.antigravity.bitcoinminingtycoon.viewmodel.GameplayFeedbackBus
import com.antigravity.bitcoinminingtycoon.viewmodel.MiningFeedbackEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.math.BigDecimal

private data class ActiveMiningDelta(
    val event: MiningFeedbackEvent,
    val alpha: androidx.compose.runtime.MutableFloatState
)

/** Short visible mining deltas; both collected events and active fade jobs stay capped at 24. */
@Composable
fun MiningFeedbackLayer(
    events: Flow<MiningFeedbackEvent>,
    numberFormat: NumberFormatPreference,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    batteryFriendly: Boolean = false
) {
    val visibleDeltas = remember { mutableStateListOf<ActiveMiningDelta>() }
    val fadeJobs = remember { mutableMapOf<Long, Job>() }

    val animationsEnabled = !reducedMotion && !batteryFriendly
    LaunchedEffect(events, animationsEnabled) {
        events.collect { event ->
            if (visibleDeltas.size >= GameplayFeedbackBus.MAX_PENDING_EVENTS) {
                val removed = visibleDeltas.removeAt(0)
                fadeJobs.remove(removed.event.sequence)?.cancel()
            }

            val active = ActiveMiningDelta(event, mutableFloatStateOf(1f))
            visibleDeltas += active
            fadeJobs[event.sequence]?.cancel()
            fadeJobs[event.sequence] = launch {
                delay(if (animationsEnabled) 290L else 340L)
                if (animationsEnabled) active.alpha.floatValue = 0f
                delay(if (animationsEnabled) 180L else 0L)
                visibleDeltas.remove(active)
                fadeJobs.remove(event.sequence)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        visibleDeltas.forEach { active ->
            val animatedAlpha = animateFloatAsState(
                targetValue = active.alpha.floatValue,
                animationSpec = tween(durationMillis = 180),
                label = "mine-delta-fade"
            )
            val alpha = if (animationsEnabled) animatedAlpha.value else 1f
            Surface(color = AppColors.PrimaryCopperDark, shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)) {
                Text(
                    text = "+${NumberFormatter.formatBtc(BigDecimal(active.event.btcDelta), numberFormat)}",
                    color = AppColors.PrimaryCopperHover,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha }.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
