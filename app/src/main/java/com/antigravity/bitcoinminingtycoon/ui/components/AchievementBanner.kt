package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.AchievementDefinition
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun AchievementBanner(
    achievement: AchievementDefinition?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(achievement?.id) {
        if (achievement != null) {
            delay(4000L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = achievement != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (achievement != null) {
            TycoonCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Achievement unlocked: ${achievement.title}. ${achievement.description}"
                    },
                borderColor = AppColors.PrimaryCopper
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "[ACHIEVEMENT UNLOCKED]",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryCopper
                        )
                        Text(
                            text = achievement.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextHigh,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = achievement.description,
                            fontSize = 12.sp,
                            color = AppColors.TextMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
