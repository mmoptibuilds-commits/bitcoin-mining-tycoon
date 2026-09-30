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
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@Composable
fun EventBanner(
    activeEvent: ActiveEventState,
    currentWallMillis: Long,
    modifier: Modifier = Modifier
) {
    val eventDef = Events.getById(activeEvent.eventId)
    val secondsRemaining = ((activeEvent.expiresAtWallMillis - currentWallMillis) / 1000L).coerceAtLeast(0L)
    val isPositive = activeEvent.multiplier >= 1.0 && activeEvent.powerModifier <= 1.0 && activeEvent.heatModifier <= 1.0

    val accentColor = if (isPositive) AppColors.PrimaryCopper else AppColors.CriticalRed
    val title = eventDef?.title ?: activeEvent.eventId.uppercase()
    val badge = eventDef?.badgeText ?: ""

    TycoonCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "Active operational event: $title, effect: $badge, $secondsRemaining seconds remaining"
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
