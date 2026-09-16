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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Liquid Glass Chromatic Prism & Refresh Rate Visualizer.
 *
 * Simulates optical refraction dispersion across a curved glass horizon with
 * flowing 120Hz photon particles.
 */
@Composable
fun LiquidPrismVisualizer(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "prism_refraction")

    val sweepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val center = Offset(width / 2f, height * 0.9f)
        val radius = width * 0.42f

        // Chromatic dispersion arc colors
        val colors = if (isDark) {
            listOf(
                Color(0xFF6366F1), // Indigo
                Color(0xFF8B5CF6), // Violet
                Color(0xFFEC4899), // Pink
                Color(0xFF38BDF8)  // Sky
            )
        } else {
            listOf(
                Color(0xFF38BDF8), // Celestial Cyan
                Color(0xFF818CF8), // Soft Lavender
                Color(0xFFFBBF24), // Amber Sunbeam
                Color(0xFF34D399)  // Emerald
            )
        }

        // Concentric refractive arcs
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        drawArc(
            brush = Brush.sweepGradient(colors, center),
            startAngle = 190f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 18f, cap = StrokeCap.Round)
        )

        // Flowing 120Hz Photon Particles
        val particleCount = 6
        for (i in 0 until particleCount) {
            val progress = (sweepPhase + (i.toFloat() / particleCount)) % 1f
            val angleDeg = 195f + progress * 150f
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val px = center.x + radius * Math.cos(angleRad).toFloat()
            val py = center.y + radius * Math.sin(angleRad).toFloat()

            drawCircle(
                color = Color.White.copy(alpha = 0.85f * (1f - progress * 0.5f)),
                radius = 4.5f,
                center = Offset(px, py)
            )
        }
    }
}
