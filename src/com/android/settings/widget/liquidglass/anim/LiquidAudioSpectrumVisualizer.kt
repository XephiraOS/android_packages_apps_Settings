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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Liquid Glass Dynamic Audio Spectrum & Equalizer Visualizer.
 *
 * Renders 14 harmonic glass equalizer bars oscillating to simulated audio frequencies
 * with glowing vertical gradients and specular highlights.
 */
@Composable
fun LiquidAudioSpectrumVisualizer(
    isSilent: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_spectrum")

    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spectrum_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val barCount = 14
        val spacing = 6f
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(4f)

        val frequencies = listOf(
            1.2f, 2.1f, 1.6f, 2.8f, 3.4f, 2.2f, 1.8f,
            2.9f, 3.1f, 1.9f, 2.5f, 1.7f, 2.4f, 1.3f
        )
        val phaseOffsets = listOf(
            0.1f, 0.4f, 0.8f, 0.2f, 0.6f, 0.9f, 0.3f,
            0.7f, 0.15f, 0.55f, 0.85f, 0.35f, 0.65f, 0.25f
        )

        for (i in 0 until barCount) {
            val freq = frequencies[i]
            val offset = phaseOffsets[i]
            val oscillation = if (isSilent) {
                0.08f // Flatline dormant state
            } else {
                val wave = sin(animPhase * freq + offset * 2 * PI.toFloat())
                (abs(wave) * 0.75f + 0.15f).coerceIn(0.12f, 0.95f)
            }

            val barHeight = height * oscillation
            val x = i * (barWidth + spacing)
            val y = height - barHeight

            // Equalizer Bar
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.50f)
                    ),
                    startY = y,
                    endY = height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )

            // Specular tip sheen
            if (!isSilent) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.65f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, (barWidth * 0.8f).coerceAtMost(barHeight)),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
