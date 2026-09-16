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
 * Liquid Glass App Orbit Constellation Engine.
 *
 * Simulates multiple orbiting glass application nodes circulating a central
 * application hub with specular orbital paths.
 */
@Composable
fun LiquidAppOrbitVisualizer(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "app_orbit")

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val center = Offset(width / 2f, height / 2f)
        val orbitRadius = minOf(width, height) * 0.38f

        // ─── 1. ORBITAL GLASS TRACK ─────────────────────────────────
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = orbitRadius,
            center = center,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )

        // ─── 2. CENTRAL HUB ─────────────────────────────────────────
        drawCircle(
            color = primaryColor.copy(alpha = 0.25f),
            radius = 16f,
            center = center
        )
        drawCircle(
            color = primaryColor,
            radius = 6f,
            center = center
        )

        // ─── 3. ORBITING SATELLITE NODES ────────────────────────────
        val nodeAngles = listOf(0f, 120f, 240f)
        val nodeColors = listOf(primaryColor, secondaryColor, Color(0xFF38BDF8))
        val nodeRadii = listOf(8f, 7f, 9f)

        for (i in nodeAngles.indices) {
            val angleDeg = orbitAngle + nodeAngles[i]
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val nx = center.x + orbitRadius * Math.cos(angleRad).toFloat()
            val ny = center.y + orbitRadius * Math.sin(angleRad).toFloat()

            // Outer node glow
            drawCircle(
                color = nodeColors[i].copy(alpha = 0.35f),
                radius = nodeRadii[i] * 1.8f,
                center = Offset(nx, ny)
            )

            // Inner solid glass bead
            drawCircle(
                color = nodeColors[i],
                radius = nodeRadii[i],
                center = Offset(nx, ny)
            )

            // Specular bead highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = nodeRadii[i] * 0.35f,
                center = Offset(nx - nodeRadii[i] * 0.3f, ny - nodeRadii[i] * 0.3f)
            )
        }
    }
}
