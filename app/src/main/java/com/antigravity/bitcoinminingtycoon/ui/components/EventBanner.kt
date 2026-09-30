package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.content.EventType
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import kotlin.math.roundToInt

@Composable
fun EventBanner(
    activeEvent: ActiveEventState,
    currentWallMillis: Long,
    modifier: Modifier = Modifier
) {
    val eventDef = Events.getById(activeEvent.eventId)
    val secondsRemaining = ((activeEvent.expiresAtWallMillis - currentWallMillis) / 1000L).coerceAtLeast(0L)
    val isPositive = eventDef?.type == EventType.AMBIENT_POSITIVE

    val accentColor = if (isPositive) AppColors.PrimaryCopper else AppColors.CriticalRed
    val title = eventDef?.title ?: activeEvent.eventId.uppercase()
    val badge = eventDef?.badgeText ?: ""
    val effectText = buildList {
        eventDef?.description?.let(::add)
        if (activeEvent.multiplier != 1.0) add("Hashrate ${formatModifier(activeEvent.multiplier)}")
        if (activeEvent.powerModifier != 1.0) add("Power draw ${formatModifier(activeEvent.powerModifier)}")
        if (activeEvent.heatModifier != 1.0) add("Heat load ${formatModifier(activeEvent.heatModifier)}")
    }.joinToString(" · ").ifBlank { "See event details for simulated market effect." }

    TycoonCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Active operational event: $title. $effectText. $secondsRemaining seconds remaining"
            },
        borderColor = accentColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isPositive) "EVENT BOOST" else "SYSTEM STRAIN",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    if (badge.isNotEmpty()) {
                        Text(
                            text = "[$badge]",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextHigh
                        )
                    }
                }
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextHigh
                )
                Text(
                    text = effectText,
                    fontSize = 11.sp,
                    color = AppColors.TextMedium,
                    maxLines = 3
                )
            }
            Text(
                text = "${secondsRemaining}s",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

private fun formatModifier(multiplier: Double): String {
    if (!multiplier.isFinite() || multiplier < 0.0) return "unavailable"
    val percent = (kotlin.math.abs(multiplier - 1.0) * 100.0).roundToInt()
    val sign = if (multiplier >= 1.0) "+" else "−"
    return "$sign$percent%"
}
