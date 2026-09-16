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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

data class StorageSegment(
    val name: String,
    val ratio: Float, // 0.0 to 1.0
    val color: Color
)

/**
 * Concentric Segmented Liquid Glass Ring Visualizer.
 *
 * Renders categorized storage arcs with specular gap highlights and a continuous
 * orbital shimmer rotation.
 */
@Composable
fun LiquidRingVisualizer(
    segments: List<StorageSegment>,
    strokeWidth: Float = 28f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "storage_ring_shimmer")
    val rotationOffset by infiniteTransition.animateFloat(
        initialValue = -90f,
        targetValue = 270f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val diameter = minOf(width, height) - strokeWidth * 2
        val topLeft = Offset((width - diameter) / 2f, (height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        // Draw track background (translucent frosted glass rim)
        drawArc(
            color = Color.White.copy(alpha = 0.08f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        var currentAngle = -90f
        val gapAngle = 4f
        val totalRatio = segments.sumOf { it.ratio.toDouble() }.toFloat().coerceAtMost(1f)

        for (segment in segments) {
            val sweep = (segment.ratio / 1f) * 360f - gapAngle
            if (sweep > 0f) {
                drawArc(
                    color = segment.color,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                currentAngle += sweep + gapAngle
            }
        }

        // Specular shimmer head along the ring orbit
        val shimmerLength = 40f
        drawArc(
            color = Color.White.copy(alpha = 0.45f),
            startAngle = rotationOffset,
            sweepAngle = shimmerLength,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth * 0.7f, cap = StrokeCap.Round)
        )
    }
}
