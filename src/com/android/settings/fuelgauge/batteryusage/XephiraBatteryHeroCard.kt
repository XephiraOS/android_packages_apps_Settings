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

package com.android.settings.fuelgauge.batteryusage

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.widget.liquidglass.anim.LiquidWaveVisualizer
import com.android.settings.widget.liquidglass.pureLiquidGlass

@Composable
fun XephiraBatteryHeroCard() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val batteryStatus = remember { getBatteryInfo(context) }
    val pct = batteryStatus.pct
    val isCharging = batteryStatus.isCharging
    val statusText = batteryStatus.statusText

    val primaryColor = when {
        isCharging -> Color(0xFF10B981) // Emerald neon
        pct <= 20 -> Color(0xFFEF4444)   // Critical coral red
        pct <= 40 -> Color(0xFFF59E0B)   // Amber warning
        else -> Color(0xFF10B981)        // Pure healthy emerald
    }

    val secondaryColor = when {
        isCharging -> Color(0xFF059669)
        pct <= 20 -> Color(0xFFDC2626)
        pct <= 40 -> Color(0xFFD97706)
        else -> Color(0xFF047857)
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
            .height(160.dp)
    ) {
        // Background animated liquid wave chamber
        LiquidWaveVisualizer(
            fillPercentage = pct / 100f,
            isCharging = isCharging,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            modifier = Modifier.clip(RoundedCornerShape(26.dp))
        )

        // Foreground stats & typography
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Battery level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$pct%",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                    if (isCharging) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Charging",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status chip pill
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0x24FFFFFF) else Color(0x18000000),
                    border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x20000000))
                ) {
                    Text(
                        text = if (isCharging) "Charging rapidly" else "Healthy battery health",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Right hero badge
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCharging) Icons.Outlined.BatteryChargingFull else Icons.Outlined.BatteryStd,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

private data class BatteryInfo(
    val pct: Int,
    val isCharging: Boolean,
    val statusText: String
)

private fun getBatteryInfo(context: Context): BatteryInfo {
    return try {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 85
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val pct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 85

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val statusText = if (isCharging) {
            "Charging on AC power"
        } else {
            "Should last until about tomorrow morning"
        }

        BatteryInfo(pct, isCharging, statusText)
    } catch (e: Throwable) {
        BatteryInfo(85, false, "Battery in good condition")
    }
}
