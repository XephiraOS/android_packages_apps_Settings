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

package com.android.settings.widget.liquidglass.header

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Parallax Spring Collapsible Glass Header.
 *
 * Fluidly scales, collapses, and morphs hero visualizer cards into compact floating
 * frosted glass title bars as lists are scrolled, complete with physical spring dampening.
 */
@Composable
fun LiquidCollapsibleHeader(
    collapseProgress: Float, // 0f = expanded, 1f = collapsed
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 180.dp,
    collapsedHeight: Dp = 64.dp,
    isDark: Boolean = isSystemInDarkTheme(),
    expandedContent: @Composable () -> Unit,
    collapsedContent: (@Composable () -> Unit)? = null
) {
    val progress = collapseProgress.coerceIn(0f, 1f)

    val currentHeight = lerp(expandedHeight, collapsedHeight, progress)
    val cornerRadius = lerp(26.dp, 32.dp, progress)
    val refraction = lerp(14.dp, 8.dp, progress).value

    val expandedAlpha by animateFloatAsState(
        targetValue = (1f - progress * 1.6f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f),
        label = "expanded_alpha"
    )

    val collapsedAlpha by animateFloatAsState(
        targetValue = ((progress - 0.4f) * 1.66f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f),
        label = "collapsed_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(currentHeight)
            .pureLiquidGlass(
                shape = RoundedCornerShape(cornerRadius),
                refraction = refraction,
                isDark = isDark
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        // Expanded Hero Content
        if (expandedAlpha > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = expandedAlpha
                        scaleX = 1f - progress * 0.08f
                        scaleY = 1f - progress * 0.08f
                    }
            ) {
                expandedContent()
            }
        }

        // Collapsed Floating Compact Pill Content
        if (collapsedContent != null && collapsedAlpha > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = collapsedAlpha
                        scaleX = 0.95f + collapsedAlpha * 0.05f
                    },
                contentAlignment = Alignment.Center
            ) {
                collapsedContent()
            }
        }
    }
}
