package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import com.antigravity.bitcoinminingtycoon.ui.theme.ShapeButton

enum class ButtonStyle {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE
}

@Composable
fun TycoonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    contentDescriptionText: String? = null
) {
    val containerColor = when (style) {
        ButtonStyle.PRIMARY -> AppColors.PrimaryCopper
        ButtonStyle.SECONDARY -> AppColors.SurfaceHigh
        ButtonStyle.DESTRUCTIVE -> AppColors.CriticalRed
    }

    val contentColor = when (style) {
        ButtonStyle.PRIMARY -> AppColors.Background
        ButtonStyle.SECONDARY -> AppColors.TextHigh
        ButtonStyle.DESTRUCTIVE -> AppColors.TextHigh
    }

    val disabledContainerColor = AppColors.SurfaceHigh
    val disabledContentColor = AppColors.TextDisabled

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (contentDescriptionText != null) {
                    Modifier.semantics { contentDescription = contentDescriptionText }
                } else Modifier
            ),
        shape = ShapeButton,
        border = BorderStroke(1.dp, if (enabled) AppColors.BorderSubtle else Color.Transparent),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}
