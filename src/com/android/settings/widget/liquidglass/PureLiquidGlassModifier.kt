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
 * Pure Optical AGSL Liquid Glass Shader (Inspired by Kyant0/AndroidLiquidGlass):
 * Uses Signed Distance Field (SDF) rounded rectangle geometry and analytic
 * surface normals to calculate physical liquid meniscus refraction, edge light
 * bending, and specular white rim reflections without chromatic aberration.
 */
private const val KYANT_LIQUID_GLASS_AGSL = """
uniform shader content;
uniform float2 size;
uniform float radius;
uniform float refraction;

float sdRoundedRect(float2 coord, float2 halfSize, float r) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(r));
    float outside = length(max(cornerCoord, 0.0)) - r;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float r) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(r));
    if (cornerCoord.x > 0.0 || cornerCoord.y > 0.0) {
        return sign(coord) * normalize(max(cornerCoord, 0.0001));
    } else {
        if (cornerCoord.x > cornerCoord.y) {
            return float2(sign(coord.x), 0.0);
        } else {
            return float2(0.0, sign(coord.y));
        }
    }
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centered = coord - halfSize;
    float d = sdRoundedRect(centered, halfSize, radius);
    
    // Liquid meniscus refraction zone near the boundary
    float meniscusWidth = max(radius * 1.5, 24.0);
    float t = clamp(-d / meniscusWidth, 0.0, 1.0);
    
    // Smooth fluid meniscus curve (highest displacement near edge)
    float displacement = pow(1.0 - t, 2.5) * refraction;
    float2 grad = gradSdRoundedRect(centered, halfSize, radius);
    float2 refractedCoord = coord - grad * displacement;
    
    half4 color = content.eval(refractedCoord);
    
    // Specular white rim reflection along the outer glass perimeter
    float rim = smoothstep(-6.0, 0.0, d) * smoothstep(2.0, -1.0, d) * 0.38;
    return color + half4(rim, rim, rim, 0.0);
}
"""

/**
 * Applies a pure liquid transparent glass effect to a Composable surface.
 *
 * @param shape Corner clipping and border contour
 * @param cornerRadius Radius in Dp used for SDF curvature
 * @param refraction Fluid optical displacement magnitude
 * @param borderWidth Thickness of the specular glass rim
 */
@Composable
fun Modifier.pureLiquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    cornerRadius: Dp = 26.dp,
    refraction: Float = 14f,
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
                    Color(0x75FFFFFF), // Specular light hit
                    Color(0x1AFFFFFF), // Translucent edge
                    Color(0x45FFFFFF), // Soft rim reflection
                    Color(0x12FFFFFF)
                ),
                start = Offset(sweep, 0f),
                end = Offset(sweep + 380f, 520f)
            ),
            shape = shape
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x1AFFFFFF), // 10% white top
                    Color(0x07FFFFFF)  // 2.7% white bottom (ultra clear liquid)
                )
            ),
            shape = shape
        )

    // Hardware AGSL RuntimeShader on Android 13+ (Tiramisu / VanillaIceCream / Baklava)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember {
            try {
                RuntimeShader(KYANT_LIQUID_GLASS_AGSL)
            } catch (e: Throwable) {
                null
            }
        }
        if (shader != null) {
            baseModifier.graphicsLayer {
                val rPx = cornerRadius.toPx()
                shader.setFloatUniform("size", size.width, size.height)
                shader.setFloatUniform("radius", rPx)
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
