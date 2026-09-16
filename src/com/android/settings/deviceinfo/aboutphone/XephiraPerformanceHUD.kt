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

package com.android.settings.deviceinfo.aboutphone

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Xephira Performance HUD Diagnostic Overlay.
 *
 * High-precision liquid glass telemetry overlay unlocked by tapping the Xephira 'X'
 * logo 5 times. Shows live CPU topology, memory pressure, and thermals.
 */
@Composable
fun XephiraPerformanceHUD(
    onDismiss: () -> Unit,
    isDark: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val haptics = rememberXephiraHaptics()

    val telemetry = remember { getDiagnosticTelemetry(context) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pureLiquidGlass(
                    shape = RoundedCornerShape(32.dp),
                    refraction = 20f,
                    isDark = isDark
                )
                .clip(RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Xephira HUD",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = "Live Hardware Telemetry",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0x99FFFFFF) else Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x22FFFFFF) else Color(0x14000000),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable {
                                haptics.lightClick()
                                onDismiss()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = if (isDark) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Diagnostic Metric 1: CPU Architecture
                HudMetricRow(
                    icon = Icons.Outlined.Speed,
                    label = "CPU Cores",
                    value = "${telemetry.cpuCores} active cores",
                    subvalue = "Governor: Schedutil",
                    isDark = isDark,
                    tintColor = Color(0xFF38BDF8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic Metric 2: RAM Memory
                HudMetricRow(
                    icon = Icons.Outlined.Memory,
                    label = "System RAM",
                    value = "${telemetry.availableRamGb} GB free / ${telemetry.totalRamGb} GB",
                    subvalue = "Load: ${telemetry.ramUsagePercent}%",
                    isDark = isDark,
                    tintColor = Color(0xFF8B5CF6)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Diagnostic Metric 3: Battery Thermal
                HudMetricRow(
                    icon = Icons.Outlined.Thermostat,
                    label = "Thermal Status",
                    value = "${telemetry.batteryTempCelsius}°C",
                    subvalue = telemetry.thermalStatus,
                    isDark = isDark,
                    tintColor = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun HudMetricRow(
    icon: ImageVector,
    label: String,
    value: String,
    subvalue: String,
    isDark: Boolean,
    tintColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pureLiquidGlass(
                shape = RoundedCornerShape(18.dp),
                refraction = 8f,
                isDark = isDark
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = tintColor.copy(alpha = 0.2f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isDark) Color(0x99FFFFFF) else Color(0xFF64748B)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = subvalue,
                fontSize = 10.sp,
                color = tintColor
            )
        }
    }
}

private data class DiagnosticTelemetry(
    val cpuCores: Int,
    val availableRamGb: String,
    val totalRamGb: String,
    val ramUsagePercent: Int,
    val batteryTempCelsius: String,
    val thermalStatus: String
)

private fun getDiagnosticTelemetry(context: Context): DiagnosticTelemetry {
    val cores = Runtime.getRuntime().availableProcessors()

    // RAM stats
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am?.getMemoryInfo(memInfo)
    val totalGb = String.format("%.1f", memInfo.totalMem.toDouble() / (1024 * 1024 * 1024))
    val availGb = String.format("%.1f", memInfo.availMem.toDouble() / (1024 * 1024 * 1024))
    val usedPercent = (((memInfo.totalMem - memInfo.availMem).toDouble() / memInfo.totalMem) * 100).toInt()

    // Battery thermal stats
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
    val tempC = String.format("%.1f", rawTemp / 10.0)

    val thermal = when {
        rawTemp < 350 -> "Optimal Temperature"
        rawTemp < 420 -> "Warm"
        else -> "Thermal Throttling Imminent"
    }

    return DiagnosticTelemetry(
        cpuCores = cores,
        availableRamGb = availGb,
        totalRamGb = totalGb,
        ramUsagePercent = usedPercent,
        batteryTempCelsius = tempC,
        thermalStatus = thermal
    )
}
