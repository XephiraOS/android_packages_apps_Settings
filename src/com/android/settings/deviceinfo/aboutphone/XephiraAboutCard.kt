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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.SystemProperties
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.LegalSettings
import com.android.settings.R
import com.android.settings.SubSettings
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionSettings
import com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics
import com.android.settings.widget.liquidglass.pureLiquidGlass
import com.android.settingslib.DeviceInfoUtils
import com.android.settingslib.development.DevelopmentSettingsEnabler

/**
 * Pure Liquid Glass About Phone Full Compose Screen.
 *
 * Features the official Xephira vector brand hero banner, 4 dynamic real hardware
 * tiles (Device, Processor, Platform, Security), and comprehensive system identity groups.
 */
@Composable
fun XephiraAboutHeader() {
    XephiraAboutScreen()
}

@Composable
fun XephiraAboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val haptics = rememberXephiraHaptics()

    // ─── REAL SYSTEM & HARDWARE PARAMETERS ────────────────────
    val xephiraVersion = remember {
        val v = SystemProperties.get("ro.xephira.version")
        if (v.isNotEmpty()) v else SystemProperties.get("ro.lineage.version", "1.0")
    }
    val buildType = remember { SystemProperties.get("ro.xephira.buildtype", "OFFICIAL") }
    val androidVersion = remember { Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY }
    val sdkInt = remember { Build.VERSION.SDK_INT }
    val deviceName = remember { getRealDeviceName(context) }
    val (procTitle, procDetail) = remember { getRealProcessorSummary() }
    val (platformTitle, platformDetail) = remember { getRealPlatformSummary(context) }
    val (securityTitle, securityDetail) = remember { getRealSecuritySummary() }
    val (deviceTitle, deviceDetail) = remember { getRealDeviceSummary() }
    val buildNumber = remember { getSanitizedBuildNumber() }
    val uptime = remember { formatUptime(SystemClock.elapsedRealtime()) }
    val maintainer = remember { getDeviceMaintainer() }
    val maintainerUrl = remember { SystemProperties.get("ro.xephira.maintainer.url", "") }
    val ipAddress = remember { getRealIpAddress(context) }
    val simSummary = remember { getSimCarrierSummary(context) }

    // Easter egg & developer unlock states
    var clickCount by remember { mutableIntStateOf(0) }
    var devHits by remember { mutableIntStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }
    var showHUD by remember { mutableStateOf(false) }
    var showSimDialog by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "logo_scale"
    )

    // Halo pulse animation around logo
    val infiniteTransition = rememberInfiniteTransition(label = "about_halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── 1. HERO PURE LIQUID GLASS CARD ───────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pureLiquidGlass(
                    shape = RoundedCornerShape(28.dp),
                    cornerRadius = 28.dp,
                    refraction = 16f,
                    borderWidth = 1.dp,
                    isDark = isDark
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    clickCount++
                    isPressed = !isPressed
                    haptics.confirm()
                    if (clickCount >= 5) {
                        clickCount = 0
                        showHUD = true
                        haptics.gesturePulse()
                    }
                }
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Vector Xephira 'X' Logo with animated radial halo
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(76.dp * haloPulse)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isDark) listOf(Color(0x35007AFF), Color.Transparent)
                                    else listOf(Color(0x25007AFF), Color.Transparent)
                                )
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.xephira_x_logo),
                        contentDescription = "Xephira Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                    )
                }

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
                    text = "Android $androidVersion • API $sdkInt",
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

        // ─── 2. 4 REAL HARDWARE & PLATFORM SPEC TILES (2x2 GRID) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tile 1: Device
            RealHardwareTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Smartphone,
                gradientColors = listOf(Color(0xFF007AFF), Color(0xFF00C6FF)),
                title = "Device",
                value = deviceTitle,
                detail = deviceDetail,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Device Model", "$deviceTitle ($deviceDetail)")
                }
            )

            // Tile 2: Processor
            RealHardwareTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Memory,
                gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFFB340)),
                title = "Processor",
                value = procTitle,
                detail = procDetail,
                isDark = isDark,
                onClick = {
                    haptics.heavyClick()
                    showHUD = true
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tile 3: Platform
            RealHardwareTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Storage,
                gradientColors = listOf(Color(0xFF5856D6), Color(0xFF706FD3)),
                title = "Platform",
                value = platformTitle,
                detail = platformDetail,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchSubSettings(context, FirmwareVersionSettings::class.java.name)
                }
            )

            // Tile 4: Security
            RealHardwareTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Security,
                gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
                title = "Security",
                value = securityTitle,
                detail = securityDetail,
                badge = "Active",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchWebUrl(context, "https://source.android.com/docs/security/bulletin/")
                }
            )
        }

        // ─── 3. CATEGORY: DEVICE ESSENTIALS & IDENTIFIERS ─────────
        AboutCategoryGroup(title = "DEVICE ESSENTIALS", isDark = isDark) {
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_about_device_filled,
                title = "Device name",
                subtitle = deviceName,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Device Name", deviceName)
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_devices_other_filled,
                title = "Model & hardware",
                subtitle = "${Build.MODEL} (${Build.HARDWARE})",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchSubSettings(context, "com.android.settings.deviceinfo.hardwareinfo.HardwareInfoFragment")
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_wireless_filled,
                title = "SIM status",
                subtitle = simSummary,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    showSimDialog = true
                }
            )
        }

        // ─── 4. CATEGORY: OS & FIRMWARE STACK ─────────────────────
        AboutCategoryGroup(title = "OS & FIRMWARE", isDark = isDark) {
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_security_filled,
                title = "Android version",
                subtitle = "Android $androidVersion (API $sdkInt)",
                badge = "16",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchSubSettings(context, FirmwareVersionSettings::class.java.name)
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_system_dashboard_filled,
                title = "XephiraOS version",
                subtitle = "v$xephiraVersion ($buildType)",
                badge = "Pure Glass",
                isDark = isDark,
                onClick = {
                    haptics.heavyClick()
                    showHUD = true
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_date_time,
                title = "Build number",
                subtitle = buildNumber,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    devHits++
                    handleDevHit(context, devHits)
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_accent,
                title = "Uptime",
                subtitle = uptime,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Uptime", uptime)
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_about_device_filled,
                title = "Device maintainer",
                subtitle = maintainer,
                badge = if (maintainer != "XephiraOS Team") "Verified" else null,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    if (maintainerUrl.isNotEmpty()) {
                        launchWebUrl(context, maintainerUrl)
                    } else {
                        copyToClipboard(context, "Device Maintainer", maintainer)
                    }
                }
            )
        }

        // ─── 5. CATEGORY: NETWORK IDENTIFIERS ─────────────────────
        AboutCategoryGroup(title = "NETWORK & ADDRESSES", isDark = isDark) {
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_wireless_filled,
                title = "IP address",
                subtitle = ipAddress,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "IP Address", ipAddress)
                }
            )
            AboutDivider(isDark = isDark)
            AboutDetailRow(
                iconRes = R.drawable.ic_settings_bluetooth,
                title = "Bluetooth address",
                subtitle = getBluetoothAddress(context),
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Bluetooth Address", getBluetoothAddress(context))
                }
            )
        }

        // ─── 6. CATEGORY: LEGAL & REGULATORY ──────────────────────
        AboutCategoryGroup(title = "LEGAL & REGULATORY", isDark = isDark) {
            AboutDetailRow(
                iconRes = R.drawable.ic_lock_closed,
                title = "Legal information",
                subtitle = "Third-party licenses, system terms, privacy",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchSubSettings(context, LegalSettings::class.java.name)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pure Liquid Glass Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "XephiraOS • Pure Liquid Glass Architecture",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0x60FFFFFF) else Color(0x60000000),
                letterSpacing = 1.sp
            )
        }
    }

    // Diagnostics Telemetry Dialog
    if (showHUD) {
        XephiraPerformanceHUD(
            onDismiss = { showHUD = false },
            isDark = isDark
        )
    }

    // SIM Status Modal Dialog
    if (showSimDialog) {
        XephiraSimStatusDialog(
            onDismiss = { showSimDialog = false },
            isDark = isDark
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE SUBCOMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RealHardwareTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    gradientColors: List<Color>,
    title: String,
    value: String,
    detail: String,
    badge: String? = null,
    isDark: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "tile_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(22.dp),
                cornerRadius = 22.dp,
                refraction = 12f,
                borderWidth = 1.dp,
                isDark = isDark
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onClick()
            }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient Squircle Container
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(gradientColors))
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                if (badge != null) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x2234C759) else Color(0x1834C759),
                        border = BorderStroke(1.dp, Color(0x4034C759))
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0x85FFFFFF) else Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = detail,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color(0xAAFFFFFF) else Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AboutCategoryGroup(
    title: String,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0x85FFFFFF) else Color(0xFF64748B),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 10.dp, bottom = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pureLiquidGlass(
                    shape = RoundedCornerShape(22.dp),
                    cornerRadius = 22.dp,
                    refraction = 12f,
                    borderWidth = 1.dp,
                    isDark = isDark
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun AboutDetailRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    badge: String? = null,
    isDark: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "row_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDark) Color(0x1AFFFFFF) else Color(0x0D000000),
            border = BorderStroke(1.dp, if (isDark) Color(0x30FFFFFF) else Color(0x18000000)),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(if (isDark) Color.White else Color(0xFF0F172A)),
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(15.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF64748B),
                maxLines = 1
            )
        }

        if (badge != null) {
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0x1EFFFFFF) else Color(0x0C000000),
                border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x16000000)),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xDDFFFFFF) else Color(0xFF334155),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = if (isDark) Color(0x45FFFFFF) else Color(0x45000000),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AboutDivider(
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.75.dp)
            .background(if (isDark) Color(0x14FFFFFF) else Color(0x0F000000))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// REAL HARDWARE & DATA TELEMETRY HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun getRealDeviceSummary(): Pair<String, String> {
    val model = Build.MODEL
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val customDisplay = SystemProperties.get("ro.xephira.display")
    val subtitle = if (customDisplay.isNotEmpty()) {
        "$manufacturer • ${customDisplay.replace("_", " ")}"
    } else {
        "$manufacturer • ${Build.DEVICE}"
    }
    return Pair(model, subtitle)
}

private fun getRealProcessorSummary(): Pair<String, String> {
    val customSoc = SystemProperties.get("ro.xephira.soc")
    val socModel = if (customSoc.isNotEmpty()) {
        customSoc.replace("_", " ")
    } else if (Build.SOC_MODEL != Build.UNKNOWN && Build.SOC_MODEL.isNotEmpty()) {
        Build.SOC_MODEL
    } else {
        val roSoc = SystemProperties.get("ro.soc.model")
        if (roSoc.isNotEmpty()) roSoc else {
            val board = SystemProperties.get("ro.board.platform")
            if (board.isNotEmpty()) board else Build.HARDWARE
        }
    }

    val socManufacturer = if (Build.SOC_MANUFACTURER != Build.UNKNOWN && Build.SOC_MANUFACTURER.isNotEmpty()) {
        Build.SOC_MANUFACTURER
    } else {
        SystemProperties.get("ro.soc.manufacturer", "")
    }

    val title = if (socManufacturer.isNotEmpty() && !socModel.contains(socManufacturer, ignoreCase = true)) {
        "$socManufacturer $socModel"
    } else {
        socModel
    }

    val cores = Runtime.getRuntime().availableProcessors()
    val arch = System.getProperty("os.arch") ?: "ARM64"
    val subtitle = "$cores Cores • $arch"

    return Pair(title, subtitle)
}

private fun getDeviceMaintainer(): String {
    val prop = SystemProperties.get("ro.xephira.maintainer")
        .ifEmpty { SystemProperties.get("ro.lineage.maintainer") }
        .ifEmpty { SystemProperties.get("ro.build.user") }
    if (prop.isEmpty() || prop.equals("UNKNOWN", ignoreCase = true)) {
        return "XephiraOS Team"
    }
    return prop.replace("_", " ")
}

private fun getRealPlatformSummary(context: Context): Pair<String, String> {
    val androidRelease = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY
    val sdkInt = Build.VERSION.SDK_INT
    val kernel = DeviceInfoUtils.getFormattedKernelVersion(context)
    val kernelShort = kernel.split("-").firstOrNull() ?: kernel
    return Pair(kernelShort, "Android $androidRelease (API $sdkInt)")
}

private fun getRealSecuritySummary(): Pair<String, String> {
    val patch = DeviceInfoUtils.getSecurityPatch() ?: "Up to date"
    val selinux = getSELinuxStatus()
    return Pair(patch, "SELinux: $selinux")
}

private fun getRealDeviceName(context: Context): String {
    return try {
        Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            ?: Settings.Global.getString(context.contentResolver, "device_name")
            ?: Build.MODEL
    } catch (e: Throwable) {
        Build.MODEL
    }
}

private fun getRealIpAddress(context: Context): String {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val linkProps = cm?.getLinkProperties(cm.activeNetwork)
        val ips = linkProps?.linkAddresses?.mapNotNull { it.address.hostAddress }?.filter { !it.contains("%") }
        ips?.firstOrNull() ?: "Unavailable"
    } catch (e: Throwable) {
        "Unavailable"
    }
}

private fun getBluetoothAddress(context: Context): String {
    return try {
        Settings.Secure.getString(context.contentResolver, "bluetooth_address") ?: "Unavailable"
    } catch (e: Throwable) {
        "Unavailable"
    }
}

private fun getSimCarrierSummary(context: Context): String {
    return try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val operator = tm?.networkOperatorName
        if (!operator.isNullOrEmpty()) operator else "Ready"
    } catch (e: Throwable) {
        "Ready"
    }
}

private fun getSELinuxStatus(): String {
    return try {
        val clazz = Class.forName("android.os.SELinux")
        val method = clazz.getMethod("isSELinuxEnforced")
        val enforced = method.invoke(null) as? Boolean ?: true
        if (enforced) "Enforcing" else "Permissive"
    } catch (e: Throwable) {
        "Enforcing"
    }
}

private fun formatUptime(millis: Long): String {
    val seconds = millis / 1000
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60
    return if (days > 0) {
        "$days days, $hours hrs, $minutes mins"
    } else {
        "$hours hrs, $minutes mins"
    }
}

private fun handleDevHit(context: Context, hits: Int) {
    if (DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(context)) {
        Toast.makeText(context, "No need, you are already a developer.", Toast.LENGTH_SHORT).show()
        return
    }
    val remaining = 7 - hits
    if (remaining in 1..4) {
        Toast.makeText(context, "You are now $remaining steps away from being a developer.", Toast.LENGTH_SHORT).show()
    } else if (remaining <= 0) {
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(context, true)
        Toast.makeText(context, "You are now a developer!", Toast.LENGTH_LONG).show()
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, value)
        cm?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    } catch (ignored: Throwable) {}
}

private fun launchSubSettings(context: Context, fragmentClass: String) {
    try {
        val intent = Intent(context, SubSettings::class.java).apply {
            putExtra(":settings:show_fragment", fragmentClass)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (ignored: Throwable) {}
}

private fun launchWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (ignored: Throwable) {}
}

private fun getSanitizedBuildNumber(): String {
    val custom = SystemProperties.get("ro.xephira.display.version")
    if (custom.isNotEmpty()) return custom

    val raw = Build.DISPLAY
    if (raw.isNullOrEmpty()) {
        val ver = SystemProperties.get("ro.xephira.version", "1.0")
        val type = SystemProperties.get("ro.xephira.buildtype", "OFFICIAL")
        return "XephiraOS-$ver-${Build.DEVICE}-$type"
    }

    return raw
        .replace("lineage_", "xephira_", ignoreCase = true)
        .replace("lineage-", "xephira-", ignoreCase = true)
        .replace("LineageOS", "XephiraOS", ignoreCase = true)
        .replace("lineage", "xephira", ignoreCase = true)
}
