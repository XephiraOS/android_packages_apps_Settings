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

package com.android.settings.widget.liquidglass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure Optical AGSL Liquid Glass Shader:
 * Simulates fluid meniscus surface curvature and edge light bending
 * without artificial chromatic dispersion, creating an ultra-clean
 * liquid transparent water/glass aesthetic.
 */
private const val PURE_LIQUID_GLASS_AGSL = """
uniform shader content;
uniform float2 size;
uniform float refraction;

half4 main(float2 coord) {
    float2 center = size * 0.5;
    float2 norm = (coord - center) / max(center.x, center.y);
    float dist = length(norm);
    
    // Pure optical liquid meniscus distortion
    float offset = pow(dist, 2.2) * refraction;
    float2 dir = normalize(norm + 0.00001);
    
    // Direct sampling to retain pure crystal transparency
    half4 color = content.eval(coord + dir * offset);
    
    // Specular white rim reflection
    float rim = smoothstep(0.65, 1.0, dist) * 0.35;
    return color + half4(rim, rim, rim, 0.0);
}
"""

/**
 * Applies a pure liquid transparent glass effect to a Composable surface.
 *
 * @param shape Corner clipping and border contour
 * @param refraction Fluid optical displacement magnitude
 * @param borderWidth Thickness of the specular glass rim
 */
@Composable
fun Modifier.pureLiquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    refraction: Float = 12f,
    borderWidth: Dp = 1.dp
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_reflection")
    val sweep by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_offset"
    )

    // Base pure monochrome liquid glass (clear translucent gradient + specular hairline border)
    val baseModifier = this
        .clip(shape)
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x70FFFFFF), // Specular light hit
                    Color(0x18FFFFFF), // Translucent edge
                    Color(0x40FFFFFF), // Soft rim reflection
                    Color(0x10FFFFFF)
                ),
                start = Offset(sweep, 0f),
                end = Offset(sweep + 350f, 500f)
            ),
            shape = shape
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x18FFFFFF), // 10% white top
                    Color(0x06FFFFFF)  // 2.5% white bottom (ultra clear liquid)
                )
            ),
            shape = shape
        )

    // Hardware AGSL RuntimeShader on Android 13+ (Tiramisu / VanillaIceCream / Baklava)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember {
            try {
                RuntimeShader(PURE_LIQUID_GLASS_AGSL)
            } catch (e: Throwable) {
                null
            }
        }
        if (shader != null) {
            baseModifier.graphicsLayer {
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("refraction", refraction)
                renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
            }
        } else {
            baseModifier
        }
    } else {
        baseModifier
    }
}
