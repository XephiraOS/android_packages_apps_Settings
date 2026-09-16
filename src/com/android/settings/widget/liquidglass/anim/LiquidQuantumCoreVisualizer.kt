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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Liquid Glass Quantum Core & Gear Engine.
 *
 * Simulates interlocking counter-rotating precision glass gear rings representing
 * underlying system orchestration.
 */
@Composable
fun LiquidQuantumCoreVisualizer(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "system_core")

    val gearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_rot"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val center = Offset(width / 2f, height / 2f)
        val outerRadius = minOf(width, height) * 0.40f
        val innerRadius = outerRadius * 0.60f

        // ─── 1. OUTER COUNTER-CLOCKWISE GLASS RING ─────────────────
        drawCircle(
            color = Color.White.copy(alpha = 0.10f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 2.5f)
        )

        val outerTeethCount = 12
        for (i in 0 until outerTeethCount) {
            val angleDeg = -gearRotation + (i * (360f / outerTeethCount))
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val x1 = center.x + (outerRadius - 4f) * Math.cos(angleRad).toFloat()
            val y1 = center.y + (outerRadius - 4f) * Math.sin(angleRad).toFloat()
            val x2 = center.x + (outerRadius + 5f) * Math.cos(angleRad).toFloat()
            val y2 = center.y + (outerRadius + 5f) * Math.sin(angleRad).toFloat()

            drawLine(
                color = primaryColor.copy(alpha = 0.70f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
        }

        // ─── 2. INNER CLOCKWISE GLASS RING ─────────────────────────
        drawCircle(
            color = secondaryColor.copy(alpha = 0.25f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = 2f)
        )

        val innerTeethCount = 8
        for (i in 0 until innerTeethCount) {
            val angleDeg = gearRotation * 1.5f + (i * (360f / innerTeethCount))
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val x1 = center.x + (innerRadius - 3f) * Math.cos(angleRad).toFloat()
            val y1 = center.y + (innerRadius - 3f) * Math.sin(angleRad).toFloat()
            val x2 = center.x + (innerRadius + 4f) * Math.cos(angleRad).toFloat()
            val y2 = center.y + (innerRadius + 4f) * Math.sin(angleRad).toFloat()

            drawLine(
                color = secondaryColor.copy(alpha = 0.85f),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }

        // ─── 3. CENTRAL PULSATING QUANTUM NUCLEUS ──────────────────
        drawCircle(
            color = Color.White.copy(alpha = 0.90f),
            radius = 5f,
            center = center
        )
    }
}
