package com.antigravity.bitcoinminingtycoon.ui.screens.upgrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

/** Small, stable track heading used above each grouped list of upgrades. */
@Composable
fun UpgradeTrackHeading(
    groupLabel: String,
    subgroup: UpgradeSubgroup,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (groupLabel != "All tracks" && groupLabel != subgroup.category.label) {
                Text(
                    text = groupLabel.uppercase(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryCopper
                )
            }
            Text(
                text = subgroup.category.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextHigh
            )
        }
        Text(
            text = "${subgroup.upgrades.size} upgrades",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = AppColors.TextMedium
        )
    }
}
