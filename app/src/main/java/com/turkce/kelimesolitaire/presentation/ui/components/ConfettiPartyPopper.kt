package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val waveDelaySec: Float,
    val originXRatio: Float,
    val originYRatio: Float,
    val initialVx: Float,
    val initialVy: Float,
    val terminalVy: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val isCircle: Boolean,
    val rotationSpeed: Float,
    val flipSpeed: Float,
    val swayFrequency: Float,
    val swayAmplitude: Float,
    val initialRotation: Float
)

private val ConfettiColors = listOf(
    Color(0xFFFFD700), // Rich Gold
    Color(0xFF10B981), // Emerald Green
    Color(0xFFF97316), // Vivid Orange
    Color(0xFF38BDF8), // Sky Blue
    Color(0xFFEC4899), // Neon Pink
    Color(0xFFA855F7), // Royal Purple
    Color(0xFFEF4444), // Coral Red
    Color(0xFF34D399), // Mint Green
    Color(0xFF3B82F6), // Electric Blue
    Color(0xFFFDE047)  // Bright Yellow
)

@Composable
fun ConfettiPartyPopper(
    modifier: Modifier = Modifier,
    durationMillis: Int = 6500
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val animProgress = remember { Animatable(0f) }

    // Generate rich 3-wave confetti shower (130 particles total)
    val particles = remember {
        val list = mutableListOf<ConfettiPiece>()
        val rng = Random(System.currentTimeMillis())

        // --- Wave 1: Immediate dual party popper cannon blast (55 particles at t = 0) ---
        // Left cannon (28 particles): shoots up and towards center-right
        for (i in 0 until 28) {
            val angleDeg = rng.nextDouble(-82.0, -38.0).toFloat()
            val angleRad = (angleDeg * PI / 180f).toFloat()
            val speed = rng.nextDouble(750.0, 1400.0).toFloat()
            list.add(
                ConfettiPiece(
                    waveDelaySec = 0f,
                    originXRatio = rng.nextDouble(0.08, 0.16).toFloat(),
                    originYRatio = rng.nextDouble(0.80, 0.88).toFloat(),
                    initialVx = (speed * cos(angleRad)),
                    initialVy = (speed * sin(angleRad)),
                    terminalVy = rng.nextDouble(140.0, 260.0).toFloat(),
                    width = rng.nextDouble(10.0, 18.0).toFloat(),
                    height = rng.nextDouble(6.0, 11.0).toFloat(),
                    color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
                    isCircle = rng.nextFloat() < 0.22f,
                    rotationSpeed = rng.nextDouble(-480.0, 480.0).toFloat(),
                    flipSpeed = rng.nextDouble(3.5, 9.0).toFloat(),
                    swayFrequency = rng.nextDouble(2.2, 5.0).toFloat(),
                    swayAmplitude = rng.nextDouble(25.0, 60.0).toFloat(),
                    initialRotation = rng.nextDouble(0.0, 360.0).toFloat()
                )
            )
        }

        // Right cannon (27 particles): shoots up and towards center-left
        for (i in 0 until 27) {
            val angleDeg = rng.nextDouble(-142.0, -98.0).toFloat()
            val angleRad = (angleDeg * PI / 180f).toFloat()
            val speed = rng.nextDouble(750.0, 1400.0).toFloat()
            list.add(
                ConfettiPiece(
                    waveDelaySec = 0f,
                    originXRatio = rng.nextDouble(0.84, 0.92).toFloat(),
                    originYRatio = rng.nextDouble(0.80, 0.88).toFloat(),
                    initialVx = (speed * cos(angleRad)),
                    initialVy = (speed * sin(angleRad)),
                    terminalVy = rng.nextDouble(140.0, 260.0).toFloat(),
                    width = rng.nextDouble(10.0, 18.0).toFloat(),
                    height = rng.nextDouble(6.0, 11.0).toFloat(),
                    color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
                    isCircle = rng.nextFloat() < 0.22f,
                    rotationSpeed = rng.nextDouble(-480.0, 480.0).toFloat(),
                    flipSpeed = rng.nextDouble(3.5, 9.0).toFloat(),
                    swayFrequency = rng.nextDouble(2.2, 5.0).toFloat(),
                    swayAmplitude = rng.nextDouble(25.0, 60.0).toFloat(),
                    initialRotation = rng.nextDouble(0.0, 360.0).toFloat()
                )
            )
        }

        // --- Wave 2: Cascading golden confetti shower from top (40 particles, starts at t = 0.55s) ---
        for (i in 0 until 40) {
            list.add(
                ConfettiPiece(
                    waveDelaySec = rng.nextDouble(0.4, 0.8).toFloat(),
                    originXRatio = rng.nextDouble(0.05, 0.95).toFloat(),
                    originYRatio = rng.nextDouble(-0.12, -0.02).toFloat(),
                    initialVx = rng.nextDouble(-120.0, 120.0).toFloat(),
                    initialVy = rng.nextDouble(60.0, 140.0).toFloat(),
                    terminalVy = rng.nextDouble(130.0, 230.0).toFloat(),
                    width = rng.nextDouble(9.0, 17.0).toFloat(),
                    height = rng.nextDouble(6.0, 11.0).toFloat(),
                    color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
                    isCircle = rng.nextFloat() < 0.28f,
                    rotationSpeed = rng.nextDouble(-360.0, 360.0).toFloat(),
                    flipSpeed = rng.nextDouble(3.0, 8.0).toFloat(),
                    swayFrequency = rng.nextDouble(2.0, 4.5).toFloat(),
                    swayAmplitude = rng.nextDouble(30.0, 75.0).toFloat(),
                    initialRotation = rng.nextDouble(0.0, 360.0).toFloat()
                )
            )
        }

        // --- Wave 3: Shimmering victory rain (35 particles, starts at t = 1.3s) ---
        for (i in 0 until 35) {
            list.add(
                ConfettiPiece(
                    waveDelaySec = rng.nextDouble(1.1, 1.8).toFloat(),
                    originXRatio = rng.nextDouble(0.08, 0.92).toFloat(),
                    originYRatio = rng.nextDouble(-0.15, -0.03).toFloat(),
                    initialVx = rng.nextDouble(-90.0, 90.0).toFloat(),
                    initialVy = rng.nextDouble(50.0, 120.0).toFloat(),
                    terminalVy = rng.nextDouble(120.0, 210.0).toFloat(),
                    width = rng.nextDouble(9.0, 16.0).toFloat(),
                    height = rng.nextDouble(5.0, 10.0).toFloat(),
                    color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
                    isCircle = rng.nextFloat() < 0.32f,
                    rotationSpeed = rng.nextDouble(-300.0, 300.0).toFloat(),
                    flipSpeed = rng.nextDouble(2.5, 7.5).toFloat(),
                    swayFrequency = rng.nextDouble(1.8, 4.0).toFloat(),
                    swayAmplitude = rng.nextDouble(25.0, 65.0).toFloat(),
                    initialRotation = rng.nextDouble(0.0, 360.0).toFloat()
                )
            )
        }

        list
    }

    LaunchedEffect(Unit) {
        // Celebratory haptic and audio feedback on trigger
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            com.turkce.kelimesolitaire.presentation.util.GameSettingsManager.playCoinSound(context)
        } catch (_: Exception) {}

        // Confetti physics animation over 6.5 seconds
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
        )
    }

    val totalDurationSec = durationMillis / 1000f
    val currentSec = animProgress.value * totalDurationSec

    // Popper icon lifecycle: pops in, shakes with blast recoil, then smoothly dissolves into confetti shower
    val popperAlpha = when {
        currentSec < 0.12f -> (currentSec / 0.12f).coerceIn(0f, 1f)
        currentSec < 0.85f -> 1f
        currentSec < 1.45f -> ((1.45f - currentSec) / 0.60f).coerceIn(0f, 1f)
        else -> 0f
    }
    val popperRecoilScale = if (currentSec < 0.45f) {
        1f + 0.35f * sin((currentSec / 0.45f) * PI).toFloat()
    } else {
        1f
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fluttering Confetti Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val globalAlpha = when {
                currentSec < 5.0f -> 1f
                currentSec < 6.5f -> ((6.5f - currentSec) / 1.5f).coerceIn(0f, 1f)
                else -> 0f
            }

            if (globalAlpha > 0f) {
                particles.forEach { p ->
                    if (currentSec >= p.waveDelaySec) {
                        val localT = currentSec - p.waveDelaySec

                        val originX = p.originXRatio * size.width
                        val originY = p.originYRatio * size.height

                        // Gentle floaty vertical movement with air resistance
                        val currentY = if (p.initialVy < 0f) {
                            // Cannon burst upward with air drag deceleration, smoothly becoming gentle downward float
                            val burstDecay = (1f - exp(-2.2f * localT)) / 2.2f
                            originY + (p.initialVy * burstDecay) + (p.terminalVy * localT)
                        } else {
                            // Upper rain floating down steadily
                            originY + (p.terminalVy * localT)
                        }

                        // Horizontal motion with deceleration and sinusoidal wind sway
                        val xDecay = (1f - exp(-1.2f * localT)) / 1.2f
                        val currentX = originX + (p.initialVx * xDecay) +
                                (sin(localT * p.swayFrequency) * p.swayAmplitude)

                        // 3D paper flutter
                        val flipScaleY = abs(cos(localT * p.flipSpeed)).coerceIn(0.12f, 1f)
                        val currentRot = p.initialRotation + (localT * p.rotationSpeed)

                        // Only draw if on screen or close to bounds
                        if (currentY in -40f..(size.height + 40f) && currentX in -50f..(size.width + 50f)) {
                            withTransform({
                                rotate(degrees = currentRot, pivot = Offset(currentX, currentY))
                                scale(scaleX = 1f, scaleY = flipScaleY, pivot = Offset(currentX, currentY))
                            }) {
                                val pieceColor = p.color.copy(alpha = globalAlpha)
                                if (p.isCircle) {
                                    drawCircle(
                                        color = pieceColor,
                                        radius = p.width / 2f,
                                        center = Offset(currentX, currentY)
                                    )
                                } else {
                                    drawRoundRect(
                                        color = pieceColor,
                                        topLeft = Offset(currentX - p.width / 2f, currentY - p.height / 2f),
                                        size = Size(p.width, p.height),
                                        cornerRadius = CornerRadius(2.5f, 2.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Left Party Popper 🎉 (Fires and smoothly dissolves after blast)
        if (popperAlpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 120.dp)
                    .graphicsLayer {
                        scaleX = popperRecoilScale
                        scaleY = popperRecoilScale
                        rotationZ = 16f
                        alpha = popperAlpha
                    }
            ) {
                Text(text = "🎉", fontSize = 42.sp)
            }

            // Right Party Popper 🎉 (MIRRORED HORIZONTALLY so it points inwards symmetrically, then dissolves)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 28.dp, bottom = 120.dp)
                    .graphicsLayer {
                        scaleX = -popperRecoilScale // Perfect horizontal mirror flip!
                        scaleY = popperRecoilScale
                        rotationZ = -16f
                        alpha = popperAlpha
                    }
            ) {
                Text(text = "🎉", fontSize = 42.sp)
            }
        }
    }
}
