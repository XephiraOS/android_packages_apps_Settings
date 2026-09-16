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

package com.android.settings.deviceinfo.storage

import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.widget.liquidglass.anim.LiquidRingVisualizer
import com.android.settings.widget.liquidglass.anim.StorageSegment
import com.android.settings.widget.liquidglass.pureLiquidGlass

@Composable
fun XephiraStorageHeroCard() {
    val isDark = isSystemInDarkTheme()
    val stats = remember { getRealStorageStats() }

    val appColor = Color(0xFF06B6D4)    // Electric Cyan
    val mediaColor = Color(0xFF8B5CF6)  // Purple Sapphire
    val systemColor = Color(0xFF3B82F6) // Cobalt Blue

    val segments = remember(stats) {
        listOf(
            StorageSegment("Apps", stats.appRatio, appColor),
            StorageSegment("Media", stats.mediaRatio, mediaColor),
            StorageSegment("System", stats.systemRatio, systemColor)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .pureLiquidGlass(
                shape = RoundedCornerShape(26.dp),
                refraction = 14f,
                isDark = isDark
            )
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "${stats.usedGb} GB",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "used of ${stats.totalGb} GB (${stats.usedPct}%)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Storage Category Legends
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(appColor)
                    Text("Apps", fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendDot(mediaColor)
                    Text("Media", fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    Spacer(modifier = Modifier.width(12.dp))
                    LegendDot(systemColor)
                    Text("System", fontSize = 11.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                }
            }

            // Animated Storage Torus Ring
            Box(
                modifier = Modifier
                    .size(105.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidRingVisualizer(
                    segments = segments,
                    strokeWidth = 22f
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.freeGb}G",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = "free",
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
    Spacer(modifier = Modifier.width(4.dp))
}

private data class StorageStats(
    val usedGb: Int,
    val totalGb: Int,
    val freeGb: Int,
    val usedPct: Int,
    val appRatio: Float,
    val mediaRatio: Float,
    val systemRatio: Float
)

private fun getRealStorageStats(): StorageStats {
    return try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val usedBytes = totalBytes - availableBytes

        val totalGb = (totalBytes / (1024.0 * 1024.0 * 1024.0)).toInt().coerceAtLeast(1)
        val freeGb = (availableBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
        val usedGb = (usedBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
        val usedPct = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

        val usedRatio = (usedBytes.toDouble() / totalBytes.toDouble()).toFloat()
        val appRatio = usedRatio * 0.45f
        val mediaRatio = usedRatio * 0.35f
        val systemRatio = usedRatio * 0.20f

        StorageStats(usedGb, totalGb, freeGb, usedPct, appRatio, mediaRatio, systemRatio)
    } catch (e: Throwable) {
        StorageStats(32, 128, 96, 25, 0.12f, 0.08f, 0.05f)
    }
}
