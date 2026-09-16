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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Liquid Glass Biometric Resonance Scanner Engine.
 *
 * Simulates a dual-ring orbital biometric scanner with pulsating sonar ripples and
 * specular highlight sweeps.
 */
@Composable
fun LiquidBiometricVisualizer(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_scan")

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner_rotation"
    )

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val center = Offset(width / 2f, height / 2f)
        val outerRadius = minOf(width, height) * 0.44f
        val innerRadius = outerRadius * 0.68f

        // ─── 1. EXPANDING SONAR RIPPLE ──────────────────────────────
        val rippleRadius = innerRadius + (outerRadius - innerRadius) * pulsePhase
        val rippleAlpha = (1f - pulsePhase) * 0.45f
        drawCircle(
            color = primaryColor.copy(alpha = rippleAlpha),
            radius = rippleRadius,
            center = center,
            style = Stroke(width = 2.5f)
        )

        // ─── 2. STATIC BASE RIMS ────────────────────────────────────
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = 1.5f)
        )

        // ─── 3. ROTATING ORBITAL GLASS ARCS ─────────────────────────
        val outerTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)
        val outerSize = Size(outerRadius * 2f, outerRadius * 2f)

        // Segment 1
        drawArc(
            color = primaryColor.copy(alpha = 0.85f),
            startAngle = ringRotation,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = outerTopLeft,
            size = outerSize,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Segment 2 (opposite)
        drawArc(
            color = primaryColor.copy(alpha = 0.50f),
            startAngle = ringRotation + 180f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = outerTopLeft,
            size = outerSize,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Inner counter-rotating arc
        val innerTopLeft = Offset(center.x - innerRadius, center.y - innerRadius)
        val innerSize = Size(innerRadius * 2f, innerRadius * 2f)

        drawArc(
            color = Color.White.copy(alpha = 0.70f),
            startAngle = -ringRotation * 1.5f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = innerTopLeft,
            size = innerSize,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}
