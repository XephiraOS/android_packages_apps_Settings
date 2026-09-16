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

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Holographic Radiance Signal Arc Visualizer.
 *
 * Simulates expanding wireless electromagnetic radiance ripples radiating from an
 * active antenna or Wi-Fi source.
 */
@Composable
fun LiquidSignalArcVisualizer(
    isConnected: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "signal_radiance")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave_1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_wave_2"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val center = Offset(width / 2f, height / 2f)
        val maxRadius = minOf(width, height) * 0.46f

        if (isConnected) {
            // Central beacon soft aura
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = 28f
                ),
                radius = 28f,
                center = center
            )

            // Central beacon core
            drawCircle(
                color = primaryColor,
                radius = 6.5f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 2.5f,
                center = center
            )

            // Dynamic Radiance Ripples (3 phases: 0, 0.33, 0.66)
            val phases = listOf(pulse1, (pulse1 + 0.33f) % 1f, (pulse1 + 0.66f) % 1f)
            phases.forEachIndexed { index, p ->
                val radius = p * maxRadius
                val baseAlpha = (1f - p).coerceIn(0f, 0.65f)
                val strokeWidth = (3.5f - index * 0.6f).coerceAtLeast(1.8f)

                // Wave body
                drawCircle(
                    color = primaryColor.copy(alpha = baseAlpha * 0.8f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Specular crest hairline highlight
                if (radius > 8f) {
                    drawCircle(
                        color = Color.White.copy(alpha = baseAlpha * 0.5f),
                        radius = radius - 0.75f,
                        center = center,
                        style = Stroke(width = 1f, cap = StrokeCap.Round)
                    )
                }
            }
        } else {
            // Dormant state: elegant subtle radar ring
            drawCircle(
                color = Color.Gray.copy(alpha = 0.15f),
                radius = maxRadius * 0.7f,
                center = center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = Color.Gray.copy(alpha = 0.4f),
                radius = 5f,
                center = center
            )
        }
    }
}
