package com.antigravity.bitcoinminingtycoon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ShapeCard = RoundedCornerShape(10.dp)
val ShapeButton = RoundedCornerShape(8.dp)
val ShapeBadge = RoundedCornerShape(6.dp)
val ShapeModal = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

val AppShapes = Shapes(
    small = ShapeBadge,
    medium = ShapeButton,
    large = ShapeCard
)
