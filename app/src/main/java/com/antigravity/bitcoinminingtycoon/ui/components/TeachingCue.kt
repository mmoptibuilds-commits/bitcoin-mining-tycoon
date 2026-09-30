package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.presentation.DiscoveryCue
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@Composable
fun TeachingCue(
    cue: DiscoveryCue,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = cue.title,
                color = AppColors.TextHigh,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = cue.body, color = AppColors.TextMedium, fontSize = 13.sp, lineHeight = 19.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TycoonButton(
                    text = cue.actionLabel,
                    onClick = onAction,
                    style = ButtonStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                    contentDescriptionText = "${cue.actionLabel}. ${cue.title}"
                )
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Hide tip", color = AppColors.TextMedium)
                }
            }
        }
    }
}
