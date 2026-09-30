package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AppColors.TextHigh,
    subValue: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextMedium,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        if (subValue != null) {
            Text(
                text = subValue,
                fontSize = 12.sp,
                color = AppColors.TextMedium
            )
        }
    }
}
