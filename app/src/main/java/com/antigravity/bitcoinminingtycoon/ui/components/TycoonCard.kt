package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.ui.theme.ShapeCard

@Composable
fun TycoonCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.Surface,
    borderColor: Color = AppColors.BorderSubtle,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ShapeCard,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}
