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

import android.os.Build
import android.os.SystemProperties
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Pure Liquid Glass About Phone Header Composable.
 * Features official Xephira vector branding and a 2x2 spec matrix.
 */
@Composable
fun XephiraAboutHeader() {
    val isDark = isSystemInDarkTheme()
    val xephiraVersion = SystemProperties.get("ro.xephira.version", "1.0")
    val buildType = SystemProperties.get("ro.xephira.buildtype", "OFFICIAL")
    val androidVersion = SystemProperties.get("ro.xephira.android.version", "16")
    val codename = SystemProperties.get("ro.xephira.device", Build.DEVICE)
    val securityPatch = Build.VERSION.SECURITY_PATCH

    var clickCount by remember { mutableIntStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "logo_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ─── HERO PURE LIQUID GLASS CARD ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pureLiquidGlass(
                    shape = RoundedCornerShape(28.dp),
                    refraction = 14f,
                    isDark = isDark
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clickCount++
                    isPressed = !isPressed
                }
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Vector Xephira 'X' Logo
                Image(
                    painter = painterResource(id = R.drawable.xephira_x_logo),
                    contentDescription = "Xephira Logo",
                    modifier = Modifier
                        .size(68.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Official Xephira Logotype Wordmark
                Image(
                    painter = painterResource(id = R.drawable.xephira_logotype),
                    contentDescription = "Xephira",
                    colorFilter = if (!isDark) ColorFilter.tint(Color(0xFF0F172A)) else null,
                    modifier = Modifier.height(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "XephiraOS $xephiraVersion",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Android $androidVersion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Minimalist Frosted Pill Badge
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x12000000),
                    border = BorderStroke(1.dp, if (isDark) Color(0x40FFFFFF) else Color(0x24000000))
                ) {
                    Text(
                        text = buildType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ─── 2x2 PURE LIQUID GLASS SPEC GRID ───────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PureGlassTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Smartphone,
                title = "Device",
                subtitle = Build.MODEL,
                isDark = isDark
            )
            PureGlassTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Memory,
                title = "Processor",
                subtitle = codename,
                isDark = isDark
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PureGlassTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Storage,
                title = "Platform",
                subtitle = "Linux 6.x",
                isDark = isDark
            )
            PureGlassTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Security,
                title = "Security",
                subtitle = securityPatch,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun PureGlassTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean = isSystemInDarkTheme()
) {
    Box(
        modifier = modifier
            .pureLiquidGlass(
                shape = RoundedCornerShape(20.dp),
                refraction = 9f,
                isDark = isDark
            )
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDark) Color.White else Color(0xFF0F172A),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}
