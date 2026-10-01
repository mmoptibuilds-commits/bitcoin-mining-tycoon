package com.antigravity.bitcoinminingtycoon.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.bitcoinminingtycoon.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

private data class FloatingParticle(
    val id: Long,
    val text: String,
    val animOffset: Animatable<Float, *>,
    val animAlpha: Animatable<Float, *>,
    var animationJob: Job? = null
)

private const val MAX_FLOATING_PARTICLES = 24

@Composable
fun CoreMineButton(
    onMineClick: () -> Unit,
    modifier: Modifier = Modifier,
    manualHashrateText: String = "+10 H/s",
    reducedMotion: Boolean = false,
    batteryFriendly: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val particles = remember { mutableStateListOf<FloatingParticle>() }

    val scale by animateFloatAsState(
        targetValue = FeedbackMotionPolicy.pressScale(isPressed, reducedMotion),
        animationSpec = tween(durationMillis = FeedbackMotionPolicy.PRESS_TRANSITION_MILLIS),
        label = "mine-press-compression"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .scale(scale)
            .semantics {
                contentDescription = "Mine Bitcoin manually. Generates $manualHashrateText"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                onMineClick()

                if (FeedbackMotionPolicy.allowsCosmeticMotion(reducedMotion, batteryFriendly)) {
                    if (particles.size >= MAX_FLOATING_PARTICLES) {
                        val oldest = particles.removeAt(0)
                        oldest.animationJob?.cancel()
                    }
                    val p = FloatingParticle(
                        id = System.nanoTime(),
                        text = manualHashrateText,
                        animOffset = Animatable(0f),
                        animAlpha = Animatable(1f)
                    )
                    particles.add(p)
                    p.animationJob = scope.launch {
                        try {
                            launch { p.animOffset.animateTo(-40f, tween(400)) }
                            launch { p.animAlpha.animateTo(0f, tween(400)) }
                        } finally {
                            particles.remove(p)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4f

            // Outer dark graphite base
            drawCircle(
                color = AppColors.Surface,
                radius = outerRadius,
                center = center
            )

            // Outer perimeter copper ring
            drawCircle(
                color = if (isPressed) AppColors.PrimaryCopperHover else AppColors.PrimaryCopper,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Concentric fin ring 1
            drawCircle(
                color = AppColors.BorderSubtle,
                radius = outerRadius * 0.78f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Concentric fin ring 2
            drawCircle(
                color = AppColors.BorderSubtle,
                radius = outerRadius * 0.58f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Center silicon die core
            val dieSize = outerRadius * 0.70f
            drawRect(
                color = AppColors.SurfaceHigh,
                topLeft = Offset(center.x - dieSize / 2f, center.y - dieSize / 2f),
                size = Size(dieSize, dieSize)
            )
            drawRect(
                color = AppColors.PrimaryCopperDark,
                topLeft = Offset(center.x - dieSize / 2f, center.y - dieSize / 2f),
                size = Size(dieSize, dieSize),
                style = Stroke(width = 1.5f)
            )
        }

        // Inner text inside core
        Text(
            text = "MINE",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPressed) AppColors.PrimaryCopperHover else AppColors.PrimaryCopper,
            letterSpacing = 2.sp
        )

        // Floating particles on tap
        particles.forEach { p ->
            Text(
                text = p.text,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = AppColors.PrimaryCopper.copy(alpha = p.animAlpha.value),
                modifier = Modifier.offset { IntOffset(0, p.animOffset.value.toInt()) }
                    .testTag("mine-feedback-particle")
            )
        }
    }
}
