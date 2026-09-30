package com.antigravity.bitcoinminingtycoon.ui.facility

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** Original vector scene artwork; animation runs only while the facility screen is resumed. */
@Composable
fun FacilityScene(
    model: FacilitySceneModel,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    batteryFriendly: Boolean = false
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val animateFans = !reducedMotion && !batteryFriendly && isResumed && model.visibleUnitCount > 0
    var fanRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(animateFans) {
        if (!animateFans) {
            fanRotation = 0f
        } else {
            while (isActive) {
                fanRotation = (fanRotation + 18f) % 360f
                delay(80L)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.SurfaceLow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.stage.title, color = AppColors.TextHigh, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(model.stage.description, color = AppColors.TextMedium, fontSize = 11.sp)
                }
                if (model.visibleUnitCount > 0) {
                    Text(
                        text = if (model.visibleUnitCount == FacilityPresentation.MAX_VISIBLE_UNITS) "36+ units" else "${model.visibleUnitCount} units",
                        color = AppColors.PrimaryCopper,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(118.dp)) {
                Canvas(
                    modifier = Modifier.fillMaxWidth().height(118.dp).semantics {
                        contentDescription = "Facility scene: ${model.stage.title}. ${model.stage.description}"
                    }
                ) {
                    drawRoundRect(
                        color = AppColors.Background,
                        size = size,
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                    drawLine(
                        color = AppColors.BorderSubtle,
                        start = Offset(0f, size.height * 0.82f),
                        end = Offset(size.width, size.height * 0.82f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawStage(model.stage, model.visibleUnitCount, fanRotation)
                    model.previewStage?.let(::drawPreviewSilhouette)
                }
                model.previewStage?.let { preview ->
                    Text(
                        text = "Preview: ${preview.title}",
                        color = AppColors.TextMedium,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 5.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStage(stage: FacilityStage, unitCount: Int, fanRotation: Float) {
    val width = size.width
    val height = size.height
    val groundY = height * 0.82f
    val count = if (unitCount == 0 && stage == FacilityStage.SALVAGED_PC) 1 else unitCount
    if (count == 0) return

    val columns = count.coerceAtMost(8)
    val rows = ((count + columns - 1) / columns).coerceAtLeast(1)
    val cellWidth = width / (columns + 1f)
    val cellHeight = (groundY - 8f) / rows
    val unitWidth = (cellWidth * 0.72f).coerceIn(18f, 58f)
    val unitHeight = (cellHeight * 0.72f).coerceIn(12f, 50f)

    if (stage.stageIndex >= FacilityStage.ORBITAL_ARRAY.stageIndex) {
        drawCircle(AppColors.SurfaceHigh, radius = minOf(width, height) * 0.25f, center = Offset(width * 0.18f, height * 0.58f))
        drawArc(
            color = AppColors.BorderSubtle,
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(-width * 0.18f, height * 0.24f),
            size = Size(width * 0.72f, height * 0.62f),
            style = Stroke(width = 1.dp.toPx())
        )
    }

    repeat(count) { index ->
        val row = index / columns
        val column = index % columns
        val x = (column + 1f) * cellWidth
        val y = groundY - (row + 0.5f) * cellHeight
        drawFacilityUnit(stage, Offset(x - unitWidth / 2f, y - unitHeight / 2f), Size(unitWidth, unitHeight), fanRotation)
    }
}

private fun DrawScope.drawFacilityUnit(stage: FacilityStage, topLeft: Offset, unit: Size, rotation: Float) {
    val copper = AppColors.PrimaryCopper
    val copperDark = AppColors.PrimaryCopperDark
    val steel = AppColors.SurfaceHigh
    val outline = Stroke(width = 1.3.dp.toPx())
    val x = topLeft.x
    val y = topLeft.y
    val w = unit.width
    val h = unit.height

    when (stage) {
        FacilityStage.SALVAGED_PC -> {
            drawRoundRect(steel, Offset(x, y + h * 0.08f), Size(w * 0.66f, h * 0.66f), CornerRadius(2.dp.toPx()))
            drawRoundRect(copper, Offset(x + w * 0.07f, y + h * 0.15f), Size(w * 0.52f, h * 0.45f), CornerRadius(1.dp.toPx()), outline)
            drawRoundRect(copperDark, Offset(x + w * 0.70f, y + h * 0.22f), Size(w * 0.25f, h * 0.70f), CornerRadius(2.dp.toPx()))
        }
        FacilityStage.GPU_BENCH -> {
            drawRoundRect(steel, Offset(x, y + h * 0.24f), Size(w, h * 0.60f), CornerRadius(3.dp.toPx()), outline)
            drawFan(Offset(x + w * 0.35f, y + h * 0.52f), minOf(w, h) * 0.20f, rotation, copper)
            drawFan(Offset(x + w * 0.70f, y + h * 0.52f), minOf(w, h) * 0.20f, -rotation, copper)
        }
        FacilityStage.RIG_WORKSHOP -> {
            drawRoundRect(copper, Offset(x, y + h * 0.16f), Size(w, h * 0.63f), CornerRadius(2.dp.toPx()), outline)
            repeat(3) { board ->
                val boardX = x + w * (0.16f + board * 0.25f)
                drawRoundRect(steel, Offset(boardX, y + h * 0.29f), Size(w * 0.15f, h * 0.38f), CornerRadius(1.dp.toPx()))
                drawCircle(copper, radius = h * 0.055f, center = Offset(boardX + w * 0.075f, y + h * 0.48f))
            }
        }
        FacilityStage.ASIC_ROOM -> {
            drawRoundRect(steel, Offset(x, y), Size(w, h * 0.9f), CornerRadius(2.dp.toPx()))
            repeat(4) { row ->
                drawLine(copper, Offset(x + w * 0.12f, y + h * (0.2f + row * 0.17f)),
                    Offset(x + w * 0.88f, y + h * (0.2f + row * 0.17f)), 1.4.dp.toPx())
            }
        }
        FacilityStage.WAREHOUSE -> {
            drawRect(steel, Offset(x, y + h * 0.06f), Size(w, h * 0.78f))
            drawLine(copper, Offset(x + w * 0.12f, y + h * 0.16f), Offset(x + w * 0.88f, y + h * 0.16f), outline.width)
            repeat(3) { shelf ->
                val shelfY = y + h * (0.28f + shelf * 0.18f)
                drawLine(AppColors.BorderSubtle, Offset(x + w * 0.12f, shelfY), Offset(x + w * 0.88f, shelfY), outline.width)
                drawRoundRect(copperDark, Offset(x + w * 0.18f, shelfY - h * 0.10f), Size(w * 0.2f, h * 0.10f), CornerRadius(1.dp.toPx()))
                drawRoundRect(copperDark, Offset(x + w * 0.58f, shelfY - h * 0.10f), Size(w * 0.2f, h * 0.10f), CornerRadius(1.dp.toPx()))
            }
        }
        FacilityStage.ENERGY_CAMPUS -> {
            val tower = Path().apply {
                moveTo(x + w * 0.16f, y + h * 0.86f)
                lineTo(x + w * 0.28f, y + h * 0.12f)
                lineTo(x + w * 0.72f, y + h * 0.12f)
                lineTo(x + w * 0.84f, y + h * 0.86f)
                close()
            }
            drawPath(tower, steel, style = outline)
            drawLine(copper, Offset(x + w * 0.27f, y + h * 0.35f), Offset(x + w * 0.73f, y + h * 0.35f), outline.width)
            drawLine(copper, Offset(x + w * 0.22f, y + h * 0.61f), Offset(x + w * 0.78f, y + h * 0.61f), outline.width)
        }
        FacilityStage.FUSION_MEGAFARM -> {
            drawRoundRect(steel, Offset(x, y + h * 0.08f), Size(w, h * 0.78f), CornerRadius(6.dp.toPx()), outline)
            drawRoundRect(copperDark, Offset(x + w * 0.16f, y + h * 0.18f), Size(w * 0.68f, h * 0.58f), CornerRadius(10.dp.toPx()), outline)
            drawLine(copper, Offset(x + w * 0.5f, y + h * 0.18f), Offset(x + w * 0.5f, y + h * 0.76f), outline.width)
        }
        FacilityStage.ORBITAL_ARRAY -> {
            drawRoundRect(steel, Offset(x + w * 0.40f, y + h * 0.32f), Size(w * 0.22f, h * 0.38f), CornerRadius(2.dp.toPx()), outline)
            drawLine(copper, Offset(x + w * 0.4f, y + h * 0.50f), Offset(x + w * 0.08f, y + h * 0.50f), outline.width)
            drawLine(copper, Offset(x + w * 0.62f, y + h * 0.50f), Offset(x + w * 0.92f, y + h * 0.50f), outline.width)
            drawRect(copperDark, Offset(x, y + h * 0.24f), Size(w * 0.34f, h * 0.5f), style = outline)
            drawRect(copperDark, Offset(x + w * 0.66f, y + h * 0.24f), Size(w * 0.34f, h * 0.5f), style = outline)
        }
        FacilityStage.LUNAR_QUANTUM_BASE -> {
            drawCircle(steel, radius = minOf(w, h) * 0.28f, center = Offset(x + w * 0.32f, y + h * 0.65f), style = outline)
            drawCircle(copperDark, radius = minOf(w, h) * 0.28f, center = Offset(x + w * 0.69f, y + h * 0.65f), style = outline)
            drawLine(copper, Offset(x + w * 0.32f, y + h * 0.37f), Offset(x + w * 0.69f, y + h * 0.37f), outline.width)
        }
        FacilityStage.DYSON_SWARM -> {
            val center = Offset(x + w * 0.5f, y + h * 0.5f)
            drawCircle(copper, radius = minOf(w, h) * 0.13f, center = center)
            drawOval(copper.copy(alpha = 0.7f), Offset(x + w * 0.08f, y + h * 0.26f), Size(w * 0.84f, h * 0.48f), style = outline)
            drawCircle(steel, radius = h * 0.035f, center = Offset(x + w * 0.12f, center.y))
            drawCircle(steel, radius = h * 0.035f, center = Offset(x + w * 0.88f, center.y))
        }
    }
}

private fun DrawScope.drawFan(center: Offset, radius: Float, rotation: Float, color: androidx.compose.ui.graphics.Color) {
    drawCircle(color, radius = radius, center = center, style = Stroke(width = 1.dp.toPx()))
    rotate(rotation, center) {
        repeat(3) { blade ->
            val angle = Math.toRadians((blade * 120).toDouble())
            val end = Offset(
                center.x + (kotlin.math.cos(angle) * radius).toFloat(),
                center.y + (kotlin.math.sin(angle) * radius).toFloat()
            )
            drawLine(color, center, end, strokeWidth = 1.dp.toPx())
        }
    }
}

private fun DrawScope.drawPreviewSilhouette(stage: FacilityStage) {
    val color = AppColors.TextMedium.copy(alpha = 0.22f)
    val left = size.width * 0.87f
    val top = size.height * 0.18f
    val width = size.width * 0.08f
    val height = size.height * 0.48f
    drawRoundRect(color, Offset(left, top), Size(width, height), CornerRadius(2.dp.toPx()), Stroke(width = 1.dp.toPx()))
    when (stage) {
        FacilityStage.GPU_BENCH, FacilityStage.RIG_WORKSHOP, FacilityStage.ASIC_ROOM, FacilityStage.WAREHOUSE -> {
            drawLine(color, Offset(left - width * 0.18f, top + height * 0.4f), Offset(left + width * 1.18f, top + height * 0.4f), 1.dp.toPx())
        }
        FacilityStage.ORBITAL_ARRAY, FacilityStage.LUNAR_QUANTUM_BASE, FacilityStage.DYSON_SWARM -> {
            drawCircle(color, radius = width * 0.35f, center = Offset(left + width * 0.5f, top + height * 0.5f), style = Stroke(width = 1.dp.toPx()))
        }
        else -> Unit
    }
}
