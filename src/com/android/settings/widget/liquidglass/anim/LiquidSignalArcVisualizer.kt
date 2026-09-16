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
        val maxRadius = minOf(width, height) * 0.45f

        // Central beacon dot
        drawCircle(
            color = if (isConnected) primaryColor else Color.Gray.copy(alpha = 0.4f),
            radius = 6f,
            center = center
        )

        if (isConnected) {
            // Ripple 1
            val radius1 = (pulse1 % 1f) * maxRadius
            val alpha1 = (1f - (pulse1 % 1f)).coerceIn(0f, 0.7f)
            drawCircle(
                color = primaryColor.copy(alpha = alpha1),
                radius = radius1,
                center = center,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Ripple 2
            val radius2 = (pulse2 % 1f) * maxRadius
            val alpha2 = (1f - (pulse2 % 1f)).coerceIn(0f, 0.7f)
            drawCircle(
                color = primaryColor.copy(alpha = alpha2),
                radius = radius2,
                center = center,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }
}
