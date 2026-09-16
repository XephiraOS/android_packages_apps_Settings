/*
 * Copyright (C) 2026 XephiraOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.widget.liquidglass.anim

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure Liquid Glass Fluid Wave Engine.
 *
 * Simulates physical hydrodynamic fluid wave behavior with dual harmonic
 * phase-shifted sine waves, interactive liquid level, and rising micro-bubbles.
 */
@Composable
fun LiquidWaveVisualizer(
    fillPercentage: Float, // 0.0f to 1.0f
    isCharging: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_waves")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_2"
    )

    val bubbleOffset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_offset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width <= 0f || height <= 0f) return@Canvas

        val clampedFill = fillPercentage.coerceIn(0.05f, 0.98f)
        val waterLevel = height * (1f - clampedFill)

        val waveAmp1 = 14f
        val waveAmp2 = 10f

        // ─── 1. BACKGROUND FLUID WAVE (DEEP TINT) ──────────────────────────
        val bgPath = Path().apply {
            moveTo(0f, height)
            lineTo(0f, waterLevel)

            var x = 0f
            while (x <= width) {
                val progress = x / width
                val y = waterLevel + waveAmp1 * sin((progress * 2 * PI.toFloat() * 1.2f) + phase1)
                lineTo(x, y)
                x += 8f
            }

            lineTo(width, height)
            close()
        }

        drawPath(
            path = bgPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.28f),
                    secondaryColor.copy(alpha = 0.12f)
                ),
                startY = waterLevel - waveAmp1,
                endY = height
            )
        )

        // ─── 2. FOREGROUND FLUID WAVE (LUMINOUS SPECULAR LIQUID) ───────────
        val fgPath = Path().apply {
            moveTo(0f, height)
            lineTo(0f, waterLevel)

            var x = 0f
            while (x <= width) {
                val progress = x / width
                val y = waterLevel + waveAmp2 * sin((progress * 2 * PI.toFloat() * 1.5f) + phase2)
                lineTo(x, y)
                x += 8f
            }

            lineTo(width, height)
            close()
        }

        drawPath(
            path = fgPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.45f),
                    primaryColor.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                startY = waterLevel - waveAmp2,
                endY = height
            )
        )

        // ─── 3. CHARGING RISING MICRO-BUBBLES ──────────────────────────────
        if (isCharging) {
            val bubbleXs = listOf(0.2f, 0.35f, 0.5f, 0.65f, 0.8f)
            val bubbleRadii = listOf(4f, 6f, 5f, 7f, 4f)
            val bubbleOffsets = listOf(0.1f, 0.45f, 0.2f, 0.7f, 0.35f)

            for (i in bubbleXs.indices) {
                val currentOffset = (bubbleOffset + bubbleOffsets[i]) % 1f
                val bubbleY = height - (height - waterLevel) * (1f - currentOffset)
                val bubbleX = width * bubbleXs[i] + sin(currentOffset * 4 * PI.toFloat()) * 8f

                if (bubbleY > waterLevel) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.55f * (1f - currentOffset)),
                        radius = bubbleRadii[i],
                        center = Offset(bubbleX, bubbleY)
                    )
                }
            }
        }
    }
}
