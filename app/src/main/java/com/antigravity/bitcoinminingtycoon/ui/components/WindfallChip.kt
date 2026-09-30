package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.Events
import com.antigravity.bitcoinminingtycoon.model.ActiveEventState
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@Composable
fun WindfallChip(
    windfallEvent: ActiveEventState,
    currentWallMillis: Long,
    onClaim: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val eventDef = Events.getById(windfallEvent.eventId)
    val secondsRemaining = ((windfallEvent.expiresAtWallMillis - currentWallMillis) / 1000L).coerceAtLeast(0L)
    val title = eventDef?.title ?: windfallEvent.eventId.uppercase()
    val badge = eventDef?.badgeText ?: "WINDFALL"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppColors.PrimaryCopperHover),
                onClick = { onClaim(windfallEvent.eventId) }
            )
            .semantics {
                role = Role.Button
                contentDescription = "$title windfall available! Tap to claim $badge. $secondsRemaining seconds remaining."
            },
        shape = RoundedCornerShape(8.dp),
        color = AppColors.SurfaceHigh,
        border = BorderStroke(1.5.dp, AppColors.PrimaryCopper)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "★",
                    fontSize = 16.sp,
                    color = AppColors.PrimaryCopper,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$title: TAP TO CLAIM!",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextHigh
                )
            }
            Text(
                text = "${secondsRemaining}s",
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopperHover
            )
        }
    }
}
