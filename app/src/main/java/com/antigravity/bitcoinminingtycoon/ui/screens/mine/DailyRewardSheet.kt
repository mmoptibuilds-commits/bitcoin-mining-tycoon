package com.antigravity.bitcoinminingtycoon.ui.screens.mine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.content.DailyRewards
import com.antigravity.bitcoinminingtycoon.ui.components.ButtonStyle
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonButton
import com.antigravity.bitcoinminingtycoon.ui.components.TycoonCard
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardSheet(
    currentDay: Int,
    canClaim: Boolean,
    millisUntilNextClaim: Long,
    onClaim: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hours = TimeUnit.MILLISECONDS.toHours(millisUntilNextClaim)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilNextClaim) % 60
    val cooldownText = "${hours}h ${minutes}m"

    val activeReward = DailyRewards.getForDay(currentDay)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        contentColor = AppColors.TextHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = if (canClaim) {
                        "Daily reward ready for Day $currentDay: ${activeReward.title}, ${activeReward.description}"
                    } else {
                        "Daily reward on cooldown: $cooldownText remaining until Day $currentDay reward"
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "OPERATIONAL ALLOCATION",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper
            )

            Text(
                text = "7-Day Continuous Cycle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextHigh
            )

            Text(
                text = "Missed days do not reset progress. Advance continuously through all 7 rewards.",
                fontSize = 12.sp,
                color = AppColors.TextMedium,
                fontFamily = FontFamily.Monospace
            )

            // 7 Days List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyRewards.TRACK.forEach { reward ->
                    val isClaimed = reward.dayNumber < currentDay
                    val isCurrent = reward.dayNumber == currentDay

                    val borderColor = when {
                        isCurrent && canClaim -> AppColors.PrimaryCopper
                        isCurrent -> AppColors.WarningAmber
                        isClaimed -> AppColors.BorderSubtle
                        else -> AppColors.BorderSubtle
                    }

                    val bgColor = when {
                        isCurrent -> AppColors.SurfaceHigh
                        else -> AppColors.Surface
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "DAY ${reward.dayNumber}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) AppColors.PrimaryCopper else AppColors.TextMedium
                                    )
                                    Text(
                                        text = reward.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClaimed) AppColors.TextDisabled else AppColors.TextHigh
                                    )
                                }
                                Text(
                                    text = reward.description,
                                    fontSize = 11.sp,
                                    color = if (isClaimed) AppColors.TextDisabled else AppColors.TextMedium,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            val badgeText = when {
                                isClaimed -> "[CLAIMED]"
                                isCurrent && canClaim -> "[READY]"
                                isCurrent -> "[$cooldownText]"
                                else -> "[LOCKED]"
                            }
                            val badgeColor = when {
                                isClaimed -> AppColors.PositiveGreen
                                isCurrent && canClaim -> AppColors.PrimaryCopper
                                isCurrent -> AppColors.WarningAmber
                                else -> AppColors.TextDisabled
                            }

                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TycoonButton(
                text = if (canClaim) "CLAIM DAY $currentDay REWARD" else "COOLDOWN: $cooldownText",
                onClick = onClaim,
                enabled = canClaim,
                style = ButtonStyle.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
