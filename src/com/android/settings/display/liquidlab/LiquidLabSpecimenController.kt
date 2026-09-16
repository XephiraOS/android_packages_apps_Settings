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

package com.android.settings.display.liquidlab

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceScreen
import com.android.settings.core.BasePreferenceController
import com.android.settings.spa.preference.ComposePreference
import com.android.settings.widget.liquidglass.pureLiquidGlass
import com.android.settings.widget.liquidglass.slider.LiquidGlassSlider

/**
 * Controller providing an interactive live preview specimen of Xephira Liquid Glass
 * for real-time optics tuning in Liquid Lab.
 */
class LiquidLabSpecimenController(context: Context, key: String) :
    BasePreferenceController(context, key) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val pref = screen.findPreference<ComposePreference>(preferenceKey)
        pref?.setContent {
            LiquidLabSpecimenCard()
        }
    }
}

@Composable
fun LiquidLabSpecimenCard() {
    val isDark = isSystemInDarkTheme()
    var previewSliderValue by remember { mutableFloatStateOf(0.68f) }

    val accentColor = if (isDark) Color(0xFF6366F1) else Color(0xFF38BDF8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .pureLiquidGlass(
                shape = RoundedCornerShape(26.dp),
                refraction = 16f,
                isDark = isDark
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Live Optical Specimen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }

                Text(
                    text = "AGSL v2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Simulates real-time refraction curvature and fluid inertia. Drag the slider below to test tactile feedback.",
                fontSize = 12.sp,
                color = if (isDark) Color(0xB3FFFFFF) else Color(0xFF475569),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive test slider inside the specimen
            LiquidGlassSlider(
                value = previewSliderValue,
                onValueChange = { previewSliderValue = it },
                valueRange = 0f..1f,
                icon = Icons.Outlined.Tune,
                title = "Refraction Test",
                primaryColor = accentColor,
                isDark = isDark
            )
        }
    }
}
