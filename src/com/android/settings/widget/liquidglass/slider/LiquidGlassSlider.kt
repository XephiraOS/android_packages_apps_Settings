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

package com.android.settings.widget.liquidglass.slider

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Kyant0-inspired Fluid Liquid Glass Slider.
 *
 * Renders a tactile glass chamber containing fluid with animated meniscus curvature,
 * physical drag inertia, specular crest reflections, and continuous haptic ticks.
 */
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    icon: ImageVector? = null,
    title: String = "",
    primaryColor: Color = Color(0xFF6366F1),
    isDark: Boolean = isSystemInDarkTheme()
) {
    val haptics = rememberXephiraHaptics()
    var widthPx by remember { mutableFloatStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var lastHapticStep by remember { mutableFloatStateOf(-1f) }

    val normalized = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    // Animated fluid fill width with gentle spring physics
    val animatedNormalized by animateFloatAsState(
        targetValue = normalized,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
        label = "fluid_fill"
    )

    // Fluid meniscus inertia tilt during drag
    val dragTilt by animateFloatAsState(
        targetValue = if (isDragging) 8f else 0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "drag_tilt"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pureLiquidGlass(
                shape = RoundedCornerShape(28.dp),
                refraction = 12f,
                isDark = isDark
            )
            .clip(RoundedCornerShape(28.dp))
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val newNorm = (offset.x / widthPx).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                    haptics.tick()
                    onValueChange(newValue)
                }
            }
            .pointerInput(valueRange) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        haptics.gesturePulse()
                        val newNorm = (offset.x / widthPx).coerceIn(0f, 1f)
                        val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    },
                    onDragEnd = {
                        isDragging = false
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newNorm = (change.position.x / widthPx).coerceIn(0f, 1f)
                        val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)

                        // 5% step interval haptic tick
                        val stepIndex = (newNorm * 20f).toInt().toFloat()
                        if (stepIndex != lastHapticStep) {
                            lastHapticStep = stepIndex
                            haptics.tick()
                        }

                        onValueChange(newValue)
                    }
                )
            }
    ) {
        // ─── 1. LIQUID FILL CHAMBER ─────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillWidth = size.width * animatedNormalized
            if (fillWidth > 2f) {
                val fillPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(fillWidth, 0f)
                    // Fluid meniscus curved meniscus edge
                    quadraticBezierTo(
                        fillWidth + dragTilt,
                        size.height / 2f,
                        fillWidth,
                        size.height
                    )
                    lineTo(0f, size.height)
                    close()
                }

                // Colored fluid body gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.35f else 0.45f),
                            primaryColor.copy(alpha = if (isDark) 0.75f else 0.85f)
                        ),
                        startX = 0f,
                        endX = fillWidth
                    )
                )

                // Specular top crest hairline
                drawLine(
                    color = Color.White.copy(alpha = 0.7f),
                    start = Offset(0f, 1.5f),
                    end = Offset(fillWidth, 1.5f),
                    strokeWidth = 2f
                )

                // Fluid meniscus edge specular rim
                val meniscusPath = Path().apply {
                    moveTo(fillWidth, 0f)
                    quadraticBezierTo(
                        fillWidth + dragTilt,
                        size.height / 2f,
                        fillWidth,
                        size.height
                    )
                }
                drawPath(
                    path = meniscusPath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // ─── 2. LABELS, ICON & VALUE PERCENTAGE OVERLAY ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            val pct = (normalized * 100f).toInt()
            Text(
                text = "$pct%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xD0FFFFFF) else Color(0xFF334155)
            )
        }
    }
}
