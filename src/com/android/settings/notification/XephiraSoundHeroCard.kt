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

package com.android.settings.notification

import android.content.Context
import android.media.AudioManager
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
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.widget.liquidglass.anim.LiquidAudioSpectrumVisualizer
import com.android.settings.widget.liquidglass.pureLiquidGlass

@Composable
fun XephiraSoundHeroCard() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val audioInfo = remember { getRingerInfo(context) }
    val ringerMode = audioInfo.mode
    val isSilent = ringerMode == AudioManager.RINGER_MODE_SILENT

    val primaryColor = when (ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> Color(0xFF8B5CF6) // Electric Violet
        AudioManager.RINGER_MODE_VIBRATE -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFF94A3B8)                             // Muted Slate
    }

    val secondaryColor = when (ringerMode) {
        AudioManager.RINGER_MODE_NORMAL -> Color(0xFF06B6D4) // Cyan
        AudioManager.RINGER_MODE_VIBRATE -> Color(0xFFD97706) // Warm Amber
        else -> Color(0xFF64748B)
    }

    val haptics = com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .pureLiquidGlass(
                shape = RoundedCornerShape(26.dp),
                refraction = 14f,
                isDark = isDark
            )
            .androidx.compose.foundation.clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                haptics.lightClick()
            }
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.1f)) {
                Text(
                    text = audioInfo.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = audioInfo.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x14000000),
                    border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x20000000))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = when (ringerMode) {
                                AudioManager.RINGER_MODE_NORMAL -> Icons.Outlined.NotificationsActive
                                AudioManager.RINGER_MODE_VIBRATE -> Icons.Outlined.Vibration
                                else -> Icons.Outlined.NotificationsOff
                            },
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = audioInfo.chipText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }
            }

            // Animated Equalizer Visualizer
            Box(
                modifier = Modifier
                    .width(115.dp)
                    .height(68.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidAudioSpectrumVisualizer(
                    isSilent = isSilent,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }
    }
}

private data class RingerInfo(
    val mode: Int,
    val title: String,
    val subtitle: String,
    val chipText: String
)

private fun getRingerInfo(context: Context): RingerInfo {
    return try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val mode = am?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        when (mode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerInfo(
                mode,
                "Sound & acoustics",
                "Calls and notifications will ring",
                "Ring Mode Active"
            )
            AudioManager.RINGER_MODE_VIBRATE -> RingerInfo(
                mode,
                "Vibrate mode",
                "Calls and alerts will vibrate only",
                "Vibrate Only"
            )
            else -> RingerInfo(
                mode,
                "Silent mode",
                "Notifications and calls are muted",
                "Silent Active"
            )
        }
    } catch (e: Throwable) {
        RingerInfo(AudioManager.RINGER_MODE_NORMAL, "Sound & vibration", "Standard audio profile", "Sound Active")
    }
}
