package com.antigravity.bitcoinminingtycoon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryCopper,
    onPrimary = AppColors.Background,
    primaryContainer = AppColors.PrimaryCopperDark,
    onPrimaryContainer = AppColors.PrimaryCopperHover,
    secondary = AppColors.SurfaceHigh,
    onSecondary = AppColors.TextHigh,
    background = AppColors.Background,
    onBackground = AppColors.TextHigh,
    surface = AppColors.Surface,
    onSurface = AppColors.TextHigh,
    surfaceVariant = AppColors.SurfaceLow,
    onSurfaceVariant = AppColors.TextMedium,
    outline = AppColors.BorderSubtle,
    outlineVariant = AppColors.BorderFocus,
    error = AppColors.CriticalRed,
    onError = AppColors.TextHigh
)

@Composable
fun BitcoinMiningTycoonTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
