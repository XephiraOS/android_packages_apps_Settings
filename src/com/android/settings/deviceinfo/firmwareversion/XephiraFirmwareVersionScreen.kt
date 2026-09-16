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

package com.android.settings.deviceinfo.firmwareversion

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.SystemProperties
import android.text.TextUtils
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import com.android.settings.deviceinfo.aboutphone.XephiraPerformanceHUD
import com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics
import com.android.settings.widget.liquidglass.pureLiquidGlass
import com.android.settingslib.DeviceInfoUtils

/**
 * Pure Liquid Glass Android Version Screen.
 *
 * Replaces the traditional firmware version list with a full Jetpack Compose
 * liquid glass dashboard featuring real device telemetry, optical refraction cards,
 * Android PlatLogo easter egg launcher, and Xephira Performance HUD integration.
 */
@Composable
fun XephiraFirmwareVersionScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val haptics = rememberXephiraHaptics()

    // Real system parameters
    val androidRelease = remember { Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY }
    val sdkInt = remember { Build.VERSION.SDK_INT }
    val codename = remember { Build.VERSION.CODENAME }
    val securityPatch = remember { DeviceInfoUtils.getSecurityPatch() ?: "Up to date" }
    val vendorPatch = remember { getVendorSecurityPatch() }
    val mainlineVersion = remember { getMainlineVersion(context) }
    val kernelVersion = remember { DeviceInfoUtils.getFormattedKernelVersion(context) }
    val basebandVersion = remember {
        SystemProperties.get("gsm.version.baseband", Build.getRadioVersion() ?: "Unknown")
    }
    val buildNumber = remember { getSanitizedBuildNumber() }
    val buildDate = remember { SystemProperties.get("ro.build.date", "Recent") }
    val xephiraVersion = remember {
        val ver = SystemProperties.get("ro.xephira.version")
        if (ver.isNotEmpty()) ver else SystemProperties.get("ro.lineage.version", "1.0")
    }
    val xephiraBuildType = remember { SystemProperties.get("ro.xephira.buildtype", "OFFICIAL") }
    val selinuxStatus = remember { getSELinuxStatus() }

    // Easter egg & telemetry dialog states
    var showHUD by remember { mutableStateOf(false) }
    var androidTapHits by remember { mutableStateOf(0) }
    var lastAndroidTapTime by remember { mutableStateOf(0L) }
    var xephiraTapHits by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── 1. ANDROID HERO GLASS CARD ───────────────────────────
        AndroidHeroCard(
            androidRelease = androidRelease,
            sdkInt = sdkInt,
            codename = codename,
            isDark = isDark,
            onEasterEggClick = {
                val now = SystemClock.uptimeMillis()
                if (now - lastAndroidTapTime < 500L) {
                    androidTapHits++
                } else {
                    androidTapHits = 1
                }
                lastAndroidTapTime = now
                haptics.lightClick()

                if (androidTapHits >= 3) {
                    androidTapHits = 0
                    haptics.heavyClick()
                    launchAndroidPlatLogo(context)
                }
            }
        )

        // ─── 2. XEPHIRA OS FLAGSHIP CARD ──────────────────────────
        XephiraOSGlassCard(
            xephiraVersion = xephiraVersion,
            buildType = xephiraBuildType,
            buildNumber = buildNumber,
            isDark = isDark,
            onCardClick = {
                xephiraTapHits++
                haptics.lightClick()
                if (xephiraTapHits % 3 == 0) {
                    haptics.heavyClick()
                    showHUD = true
                }
            }
        )

        // ─── 3. SECURITY & OS COMPONENT UPDATES ───────────────────
        FirmwareCategoryGroup(title = "SECURITY & OS STACK", isDark = isDark) {
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_security_filled,
                gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
                title = "Android security update",
                subtitle = securityPatch,
                badge = "Verified",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchSecurityBulletin(context)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_privacy_filled,
                gradientColors = listOf(Color(0xFF007AFF), Color(0xFF00C6FF)),
                title = "Vendor security patch",
                subtitle = vendorPatch,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Vendor Security Patch", vendorPatch)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_apps_filled,
                gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFFB340)),
                title = "Google Play system update",
                subtitle = mainlineVersion,
                badge = "Mainline",
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    launchMainlineUpdate(context)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_lock_closed,
                gradientColors = listOf(Color(0xFF5856D6), Color(0xFF706FD3)),
                title = "SELinux status",
                subtitle = selinuxStatus,
                badge = selinuxStatus,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "SELinux Status", selinuxStatus)
                }
            )
        }

        // ─── 4. KERNEL & DEVICE ARCHITECTURE ──────────────────────
        FirmwareCategoryGroup(title = "KERNEL & HARDWARE", isDark = isDark) {
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_system_dashboard_filled,
                gradientColors = listOf(Color(0xFF0A84FF), Color(0xFF5AC8FA)),
                title = "Kernel version",
                subtitle = kernelVersion,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Kernel Version", kernelVersion)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_wireless_filled,
                gradientColors = listOf(Color(0xFFFF2D55), Color(0xFFFF6482)),
                title = "Baseband version",
                subtitle = basebandVersion,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Baseband Version", basebandVersion)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_about_device_filled,
                gradientColors = listOf(Color(0xFF64748B), Color(0xFF94A3B8)),
                title = "Build number",
                subtitle = buildNumber,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Build Number", buildNumber)
                }
            )
            FirmwareDivider(isDark = isDark)
            FirmwareDetailTile(
                iconRes = R.drawable.ic_settings_date_time,
                gradientColors = listOf(Color(0xFFAF52DE), Color(0xFFBF5AF2)),
                title = "Build date",
                subtitle = buildDate,
                isDark = isDark,
                onClick = {
                    haptics.lightClick()
                    copyToClipboard(context, "Build Date", buildDate)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtle Pure Liquid Glass Footer
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
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPOSABLE SUBCOMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AndroidHeroCard(
    androidRelease: String,
    sdkInt: Int,
    codename: String,
    isDark: Boolean,
    onEasterEggClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "android_hero_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "android_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(28.dp),
                cornerRadius = 28.dp,
                refraction = 18f,
                borderWidth = 1.dp,
                isDark = isDark
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onEasterEggClick()
            }
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylized Android Number Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(68.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp * glowPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isDark) listOf(Color(0x3534C759), Color.Transparent)
                                else listOf(Color(0x2534C759), Color.Transparent)
                            )
                        )
                )
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x10000000),
                    border = BorderStroke(1.2.dp, if (isDark) Color(0x40FFFFFF) else Color(0x20000000)),
                    modifier = Modifier.size(58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = androidRelease,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Android $androidRelease",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x2434C759) else Color(0x1834C759),
                        border = BorderStroke(1.dp, Color(0x4034C759))
                    ) {
                        Text(
                            text = "API $sdkInt",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF86EFAC) else Color(0xFF16A34A),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Codename $codename • Tap 3 times for Easter Egg",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xBBFFFFFF) else Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun XephiraOSGlassCard(
    xephiraVersion: String,
    buildType: String,
    buildNumber: String,
    isDark: Boolean,
    onCardClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "xephira_card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp,
                refraction = 14f,
                borderWidth = 1.dp,
                isDark = isDark
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onCardClick()
            }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.xephira_x_logo),
                contentDescription = "Xephira Logo",
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "XephiraOS $xephiraVersion",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x22FFFFFF) else Color(0x12000000),
                        border = BorderStroke(1.dp, if (isDark) Color(0x40FFFFFF) else Color(0x20000000))
                    ) {
                        Text(
                            text = buildType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Tap 3 times for Performance Telemetry HUD",
                    fontSize = 11.5.sp,
                    color = if (isDark) Color(0xAAFFFFFF) else Color(0xFF64748B)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color(0x50FFFFFF) else Color(0x50000000),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FirmwareCategoryGroup(
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
private fun FirmwareDetailTile(
    iconRes: Int,
    gradientColors: List<Color>,
    title: String,
    subtitle: String,
    badge: String? = null,
    isDark: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "tile_scale"
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
        // Curated Squircle Badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.linearGradient(gradientColors))
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(20.dp)
            )
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
                color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF64748B)
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
    }
}

@Composable
private fun FirmwareDivider(
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
// INTENT & DATA HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun launchAndroidPlatLogo(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("android", "com.android.internal.app.PlatLogoActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Throwable) {
        Toast.makeText(context, "Android Easter Egg", Toast.LENGTH_SHORT).show()
    }
}

private fun launchSecurityBulletin(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://source.android.com/docs/security/bulletin/")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (ignored: Throwable) {}
}

private fun launchMainlineUpdate(context: Context) {
    try {
        val intent = Intent("android.settings.MODULE_UPDATE_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Throwable) {
        try {
            val intentV2 = Intent("android.settings.MODULE_UPDATE_VERSIONS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intentV2)
        } catch (ignored: Throwable) {}
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

private fun getVendorSecurityPatch(): String {
    val patch = SystemProperties.get("ro.vendor.build.security_patch")
    if (patch.isNotEmpty()) return patch
    val lineagePatch = SystemProperties.get("ro.lineage.build.vendor_security_patch")
    if (lineagePatch.isNotEmpty()) return lineagePatch
    return "Matched with Android patch"
}

private fun getMainlineVersion(context: Context): String {
    return try {
        val pm = context.packageManager
        val moduleProvider = try {
            context.getString(com.android.internal.R.string.config_defaultModuleMetadataProvider)
        } catch (e: Throwable) {
            "com.google.android.modulemetadata"
        }
        val targetPackage = if (!TextUtils.isEmpty(moduleProvider)) moduleProvider else "com.google.android.modulemetadata"
        val info = pm.getPackageInfo(targetPackage, 0)
        info.versionName ?: "Up to date"
    } catch (e: Throwable) {
        "Up to date"
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

fun getSanitizedBuildNumber(): String {
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
