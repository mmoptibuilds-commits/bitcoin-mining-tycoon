package com.antigravity.bitcoinminingtycoon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DisplayLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    color = AppColors.TextHigh
)

val DisplayMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    color = AppColors.TextHigh
)

val TitleMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    color = AppColors.TextHigh
)

val BodyMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = AppColors.TextMedium
)

val LabelSmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.5.sp,
    color = AppColors.TextMedium
)

val AppTypography = Typography(
    displayLarge = DisplayLarge,
    displayMedium = DisplayMedium,
    titleMedium = TitleMedium,
    bodyMedium = BodyMedium,
    labelSmall = LabelSmall
)
