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

package com.android.settings.homepage.liquidglass

import android.app.Activity
import android.app.settings.SettingsEnums
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.os.storage.StorageManager
import android.provider.Settings
import android.util.Log
import com.android.settings.SettingsActivity
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.android.settings.SubSettings
import com.android.settings.activityembedding.ActivityEmbeddingRulesController
import com.android.settings.core.SubSettingLauncher
import com.android.settings.deviceinfo.aboutphone.XephiraPerformanceHUD
import com.android.settings.overlay.FeatureFactory
import com.android.settings.search.SearchFeatureProvider
import com.android.settings.widget.liquidglass.anim.LiquidPrismVisualizer
import com.android.settings.widget.liquidglass.haptics.rememberXephiraHaptics
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Pure Liquid Glass Full Settings Dashboard.
 *
 * Implements an ultra-modern, pure crystal liquid glass interface
 * for the entire Android Settings experience inspired by Kyant0/AndroidLiquidGlass.
 * Integrates SDF normal-gradient refraction, specular white rim sheens,
 * floating glass search bar with quick chips, live telemetry hero banner,
 * Liquid Glass Lab showcase card, curated gradient squircle categories,
 * and built-in Performance HUD diagnostic overlay.
 */
@Composable
fun XephiraSettingsDashboard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val haptics = rememberXephiraHaptics()

    val xephiraVersion = remember { SystemProperties.get("ro.xephira.version", "1.0") }
    val androidVersion = remember { SystemProperties.get("ro.xephira.android.version", "16") }
    val buildType = remember { SystemProperties.get("ro.xephira.buildtype", "OFFICIAL") }

    // Live device metrics for status pills with reactive dynamic updates
    var wifiState by remember { mutableStateOf(getWifiLiveState(context)) }
    var batteryState by remember { mutableStateOf(getBatteryLiveState(context)) }
    var storageState by remember { mutableStateOf(getStorageLiveDetails(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_CHANGED,
                    Intent.ACTION_POWER_CONNECTED,
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        batteryState = getBatteryLiveState(context)
                    }
                    WifiManager.NETWORK_STATE_CHANGED_ACTION,
                    WifiManager.WIFI_STATE_CHANGED_ACTION,
                    ConnectivityManager.CONNECTIVITY_ACTION -> {
                        wifiState = getWifiLiveState(context)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (ignored: Throwable) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (ignored: Throwable) {}
        }
    }

    // Telemetry easter egg state
    var showPerformanceHUD by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── 1. FLOATING PURE LIQUID GLASS SEARCH BAR ─────────────
        LiquidGlassSearchBar(
            isDark = isDark,
            onSearchClick = {
                haptics.lightClick()
                launchSettingsSearch(context)
            },
            onAvatarClick = {
                haptics.heavyClick()
                showPerformanceHUD = true
            }
        )

        // ─── 2. QUICK ACTION SUGGESTION CHIPS ─────────────────────
        LiquidGlassQuickChipsRow(
            isDark = isDark,
            batteryState = batteryState,
            storageState = storageState,
            onChipClick = { target ->
                haptics.lightClick()
                when (target) {
                    QuickChipTarget.LIQUID_LAB -> launchIntent(
                        context,
                        "android.settings.DISPLAY_SETTINGS",
                        "com.android.settings.display.liquidlab.LiquidLabSettings"
                    )
                    QuickChipTarget.BATTERY -> launchIntent(
                        context,
                        Intent.ACTION_POWER_USAGE_SUMMARY,
                        "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary"
                    )
                    QuickChipTarget.DISPLAY -> launchIntent(
                        context,
                        Settings.ACTION_DISPLAY_SETTINGS,
                        "com.android.settings.DisplaySettings"
                    )
                    QuickChipTarget.STORAGE -> launchIntent(
                        context,
                        Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
                        "com.android.settings.deviceinfo.StorageDashboardFragment"
                    )
                    QuickChipTarget.SECURITY -> launchIntent(
                        context,
                        Settings.ACTION_SECURITY_SETTINGS,
                        "com.android.settings.security.SecuritySettings"
                    )
                }
            }
        )

        // ─── 3. HERO STATUS & QUICK GLANCE ────────────────────────
        LiquidGlassHeroBanner(
            xephiraVersion = xephiraVersion,
            androidVersion = androidVersion,
            buildType = buildType,
            batteryState = batteryState,
            wifiState = wifiState,
            storageState = storageState,
            isDark = isDark,
            onHeroClick = {
                haptics.heavyClick()
                launchSystemUpdate(context)
            },
            onLogoEasterEgg = {
                showPerformanceHUD = true
            },
            onBatteryClick = {
                launchIntent(context, Intent.ACTION_POWER_USAGE_SUMMARY, "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
            },
            onStorageClick = {
                launchIntent(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "com.android.settings.deviceinfo.StorageDashboardFragment")
            },
            onWifiClick = {
                launchIntent(context, Settings.ACTION_WIRELESS_SETTINGS, "com.android.settings.network.NetworkDashboardFragment")
            }
        )

        // ─── 4. FEATURED: LIQUID GLASS LAB ────────────────────────
        LiquidGlassLabFeaturedCard(
            isDark = isDark,
            onClick = {
                haptics.heavyClick()
                launchIntent(
                    context,
                    "android.settings.DISPLAY_SETTINGS",
                    "com.android.settings.display.liquidlab.LiquidLabSettings"
                )
            }
        )

        // ─── 5. CATEGORY: CONNECTIVITY & NETWORK ──────────────────
        LiquidGlassCategoryGroup(title = "CONNECTIVITY", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_wireless_filled,
                gradientColors = listOf(Color(0xFF007AFF), Color(0xFF00C6FF)),
                title = "Network & internet",
                subtitle = if (wifiState.isConnected) "Connected • ${wifiState.ssid}" else if (wifiState.isEnabled) "Wi-Fi, mobile, hotspot, SIMs" else "Wi-Fi Off • Mobile, hotspot",
                badge = if (wifiState.isConnected) "Connected" else if (wifiState.isEnabled) "Available" else "Off",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_WIRELESS_SETTINGS, "com.android.settings.network.NetworkDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_devices_other_filled,
                gradientColors = listOf(Color(0xFF5856D6), Color(0xFF706FD3)),
                title = "Connected devices",
                subtitle = "Bluetooth, pairing, Quick Share",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_BLUETOOTH_SETTINGS, "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                }
            )
        }

        // ─── 6. CATEGORY: APPS & NOTIFICATIONS ────────────────────
        LiquidGlassCategoryGroup(title = "APPLICATIONS", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_apps_filled,
                gradientColors = listOf(Color(0xFFFF2D55), Color(0xFFFF6482)),
                title = "Apps",
                subtitle = "Recent apps, default apps, permissions",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, "com.android.settings.applications.AppDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_notifications_filled,
                gradientColors = listOf(Color(0xFFFF375F), Color(0xFFFF7597)),
                title = "Notifications",
                subtitle = "Notification history, conversations",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_NOTIFICATION_SETTINGS, "com.android.settings.notification.ConfigureNotificationSettings")
                }
            )
        }

        // ─── 7. CATEGORY: DEVICE ESSENTIALS ───────────────────────
        LiquidGlassCategoryGroup(title = "DEVICE ESSENTIALS", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_battery_filled,
                gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFFB340)),
                title = "Battery",
                subtitle = "${batteryState.statusText} • Adaptive charging",
                badge = batteryState.statusText,
                isDark = isDark,
                onClick = {
                    launchIntent(context, Intent.ACTION_POWER_USAGE_SUMMARY, "com.android.settings.fuelgauge.batteryusage.PowerUsageSummary")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_storage_filled,
                gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
                title = "Storage",
                subtitle = storageState.summary,
                badge = storageState.statusText,
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "com.android.settings.deviceinfo.StorageDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_volume_up_filled,
                gradientColors = listOf(Color(0xFFAF52DE), Color(0xFFBF5AF2)),
                title = "Sound & vibration",
                subtitle = "Volume, haptics, Do Not Disturb",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_SOUND_SETTINGS, "com.android.settings.notification.SoundSettings")
                }
            )
        }

        // ─── 8. CATEGORY: PERSONALIZATION & DISPLAY ───────────────
        LiquidGlassCategoryGroup(title = "PERSONALIZATION", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_display_filled,
                gradientColors = listOf(Color(0xFF0A84FF), Color(0xFF5AC8FA)),
                title = "Display",
                subtitle = "Dark theme, refresh rate, screen timeout",
                badge = "120Hz Fluid",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_DISPLAY_SETTINGS, "com.android.settings.DisplaySettings")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_wallpaper_filled,
                gradientColors = listOf(Color(0xFFFF2D55), Color(0xFFFF9500)),
                title = "Wallpaper & style",
                subtitle = "Colors, themed icons, lock screen clock",
                isDark = isDark,
                onClick = {
                    launchIntent(context, "android.settings.WALLPAPER_SETTINGS", null)
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_accessibility_filled,
                gradientColors = listOf(Color(0xFF30D158), Color(0xFF34C759)),
                title = "Accessibility",
                subtitle = "Display size, text, screen reader",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_ACCESSIBILITY_SETTINGS, "com.android.settings.accessibility.AccessibilitySettings")
                }
            )
        }

        // ─── 9. CATEGORY: PRIVACY & SECURITY ──────────────────────
        LiquidGlassCategoryGroup(title = "SECURITY & PRIVACY", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_security_filled,
                gradientColors = listOf(Color(0xFF34C759), Color(0xFF30D158)),
                title = "Security & biometrics",
                subtitle = "Screen lock, fingerprint, face unlock",
                badge = "Protected",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_SECURITY_SETTINGS, "com.android.settings.security.SecuritySettings")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_privacy_filled,
                gradientColors = listOf(Color(0xFF32ADE6), Color(0xFF00C7BE)),
                title = "Privacy",
                subtitle = "Permissions, camera & mic access",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_PRIVACY_SETTINGS, "com.android.settings.privacy.PrivacyDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_location_filled,
                gradientColors = listOf(Color(0xFFFF9500), Color(0xFFFFCC00)),
                title = "Location",
                subtitle = "Location access, app permissions",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS, "com.android.settings.location.LocationSettings")
                }
            )
        }

        // ─── 10. CATEGORY: ACCOUNTS & SAFETY ──────────────────────
        LiquidGlassCategoryGroup(title = "ACCOUNTS & SAFETY", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_emergency_filled,
                gradientColors = listOf(Color(0xFFFF453A), Color(0xFFFF6961)),
                title = "Safety & emergency",
                subtitle = "Emergency SOS, medical info, alerts",
                badge = "SOS",
                isDark = isDark,
                onClick = {
                    launchIntent(context, "android.settings.SAFETY_CENTER", "com.android.settings.emergency.EmergencyDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_passwords_filled,
                gradientColors = listOf(Color(0xFF64748B), Color(0xFF94A3B8)),
                title = "Passwords & accounts",
                subtitle = "Autofill service, saved passwords, Google",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_SYNC_SETTINGS, "com.android.settings.accounts.AccountDashboardFragment")
                }
            )
        }

        // ─── 11. CATEGORY: SYSTEM & ABOUT PHONE ───────────────────
        LiquidGlassCategoryGroup(title = "SYSTEM", isDark = isDark) {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_system_dashboard_filled,
                gradientColors = listOf(Color(0xFF5E5CE6), Color(0xFF7D7AFF)),
                title = "System",
                subtitle = "Languages, gestures, date & time, backup",
                isDark = isDark,
                onClick = {
                    launchIntent(context, Settings.ACTION_SYSTEM_UPDATE_SETTINGS, "com.android.settings.system.SystemDashboardFragment")
                }
            )
            LiquidGlassDivider(isDark = isDark)
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_about_device_filled,
                gradientColors = listOf(Color(0xFF007AFF), Color(0xFF5856D6)),
                title = "About phone",
                subtitle = "XephiraOS $xephiraVersion • ${Build.MODEL}",
                badge = "v$xephiraVersion",
                isDark = isDark,
                onClick = {
                    launchAboutPhone(context)
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
                text = "XephiraOS • Pure Liquid Glass UI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0x60FFFFFF) else Color(0x60000000),
                letterSpacing = 1.sp
            )
        }
    }

    // Diagnostics Telemetry Dialog
    if (showPerformanceHUD) {
        XephiraPerformanceHUD(
            onDismiss = { showPerformanceHUD = false },
            isDark = isDark
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUBCOMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LiquidGlassSearchBar(
    isDark: Boolean = isSystemInDarkTheme(),
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "search_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                refraction = 14f,
                borderWidth = 1.dp,
                isDark = isDark
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onSearchClick()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = if (isDark) Color.White else Color(0xFF0F172A),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Search settings, features...",
                fontSize = 15.sp,
                color = if (isDark) Color(0xAAFFFFFF) else Color(0xFF64748B),
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            // Right Avatar Icon / HUD Trigger
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0x24FFFFFF) else Color(0x0E000000),
                border = BorderStroke(1.dp, if (isDark) Color(0x40FFFFFF) else Color(0x1C000000)),
                modifier = Modifier
                    .size(34.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onAvatarClick()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.xephira_x_logo),
                        contentDescription = "Profile / HUD",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private enum class QuickChipTarget {
    LIQUID_LAB, BATTERY, DISPLAY, STORAGE, SECURITY
}

@Composable
private fun LiquidGlassQuickChipsRow(
    isDark: Boolean,
    batteryState: BatteryLiveState,
    storageState: StorageLiveDetails,
    onChipClick: (QuickChipTarget) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip 1: Liquid Lab
        LiquidChipItem(
            text = "🧪 Liquid Lab",
            isAccent = true,
            isDark = isDark,
            onClick = { onChipClick(QuickChipTarget.LIQUID_LAB) }
        )

        // Chip 2: Battery
        LiquidChipItem(
            text = "⚡ Battery ${batteryState.statusText}",
            isAccent = false,
            isDark = isDark,
            onClick = { onChipClick(QuickChipTarget.BATTERY) }
        )

        // Chip 3: Display
        LiquidChipItem(
            text = "✨ Display 120Hz",
            isAccent = false,
            isDark = isDark,
            onClick = { onChipClick(QuickChipTarget.DISPLAY) }
        )

        // Chip 4: Storage
        LiquidChipItem(
            text = "💾 ${storageState.freeGb}GB Free",
            isAccent = false,
            isDark = isDark,
            onClick = { onChipClick(QuickChipTarget.STORAGE) }
        )

        // Chip 5: Security
        LiquidChipItem(
            text = "🛡️ Security Active",
            isAccent = false,
            isDark = isDark,
            onClick = { onChipClick(QuickChipTarget.SECURITY) }
        )
    }
}

@Composable
private fun LiquidChipItem(
    text: String,
    isAccent: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "chip_scale"
    )

    val backgroundBrush = if (isAccent) {
        Brush.linearGradient(
            colors = if (isDark) listOf(Color(0x358A2387), Color(0x35E94057))
            else listOf(Color(0x208A2387), Color(0x20E94057))
        )
    } else {
        Brush.linearGradient(
            colors = if (isDark) listOf(Color(0x16FFFFFF), Color(0x09FFFFFF))
            else listOf(Color(0x0C000000), Color(0x06000000))
        )
    }

    val borderColor = if (isAccent) {
        if (isDark) Color(0x60E94057) else Color(0x50E94057)
    } else {
        if (isDark) Color(0x28FFFFFF) else Color(0x14000000)
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isAccent) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isAccent) {
                if (isDark) Color(0xFFFF80AA) else Color(0xFFD81B60)
            } else {
                if (isDark) Color(0xEEFFFFFF) else Color(0xFF334155)
            }
        )
    }
}

@Composable
private fun LiquidGlassHeroBanner(
    xephiraVersion: String,
    androidVersion: String,
    buildType: String,
    batteryState: BatteryLiveState,
    wifiState: WifiLiveState,
    storageState: StorageLiveDetails,
    isDark: Boolean = isSystemInDarkTheme(),
    onHeroClick: () -> Unit,
    onLogoEasterEgg: () -> Unit,
    onBatteryClick: () -> Unit,
    onStorageClick: () -> Unit,
    onWifiClick: () -> Unit
) {
    val haptics = rememberXephiraHaptics()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "hero_scale"
    )

    var tapCount by remember { mutableStateOf(0) }

    // Ambient halo glow around logo
    val infiniteTransition = rememberInfiniteTransition(label = "hero_ambient")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main Hero Box - Exclusively opens System Updater
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .pureLiquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadius = 26.dp,
                    refraction = 16f,
                    borderWidth = 1.dp,
                    isDark = isDark
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    haptics.heavyClick()
                    isPressed = !isPressed
                    onHeroClick()
                }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo with pulsing aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp * haloPulse)
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
                            .size(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                tapCount++
                                haptics.lightClick()
                                if (tapCount % 3 == 0) {
                                    haptics.heavyClick()
                                    onLogoEasterEgg()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "XephiraOS $xephiraVersion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0x22FFFFFF) else Color(0x12000000),
                            border = BorderStroke(1.dp, if (isDark) Color(0x40FFFFFF) else Color(0x24000000))
                        ) {
                            Text(
                                text = buildType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Android $androidVersion • ${Build.MODEL}",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF475569)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = if (isDark) Color(0x60FFFFFF) else Color(0x60000000),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3 Quick Status Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wi-Fi Pill
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_settings_wireless_filled,
                label = "Wi-Fi",
                status = wifiState.statusText,
                progress = wifiState.progress,
                accentColor = wifiState.accentColor,
                isDark = isDark,
                onClick = onWifiClick
            )

            // Battery Pill
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_settings_battery_filled,
                label = if (batteryState.isCharging) "Charging" else "Battery",
                status = batteryState.statusText,
                progress = batteryState.progress,
                accentColor = batteryState.accentColor,
                isDark = isDark,
                onClick = onBatteryClick
            )

            // Storage Pill
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_storage_filled,
                label = "Storage",
                status = storageState.statusText,
                progress = storageState.progress,
                accentColor = storageState.accentColor,
                isDark = isDark,
                onClick = onStorageClick
            )
        }
    }
}

@Composable
private fun LiquidGlassLabFeaturedCard(
    isDark: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "lab_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp,
                refraction = 18f,
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
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Category Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE94057))
                    )
                    Text(
                        text = "XEPHIRA LABS • 120Hz AGSL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFF80AA) else Color(0xFFD81B60),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Liquid Glass Lab",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Fine-tune optical refraction, specular rim sheen & fluid haptics",
                    fontSize = 11.5.sp,
                    color = if (isDark) Color(0xBBFFFFFF) else Color(0xFF475569),
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Embedded live chromatic visualizer preview
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0x18FFFFFF) else Color(0x0C000000))
                    .padding(4.dp)
            ) {
                LiquidPrismVisualizer(
                    isDark = isDark,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassStatusPill(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    status: String,
    progress: Float,
    accentColor: Color,
    isDark: Boolean = isSystemInDarkTheme(),
    onClick: () -> Unit
) {
    val haptics = rememberXephiraHaptics()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "pill_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(18.dp),
                cornerRadius = 18.dp,
                refraction = 10f,
                borderWidth = 1.dp,
                isDark = isDark
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.lightClick()
                isPressed = !isPressed
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                colorFilter = ColorFilter.tint(if (isDark) Color.White else Color(0xFF0F172A)),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                maxLines = 1
            )

            // Mini progress indicator bar
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color(0x22FFFFFF) else Color(0x18000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isDark) Color(0x90FFFFFF) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun LiquidGlassCategoryGroup(
    title: String,
    isDark: Boolean = isSystemInDarkTheme(),
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
private fun LiquidGlassTile(
    iconRes: Int,
    gradientColors: List<Color>,
    title: String,
    subtitle: String,
    badge: String? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    onClick: () -> Unit
) {
    val haptics = rememberXephiraHaptics()
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
                haptics.lightClick()
                isPressed = !isPressed
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Curated Gradient Squircle Container
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
                fontWeight = FontWeight.Normal,
                color = if (isDark) Color(0xFF9E9E9E) else Color(0xFF64748B),
                maxLines = 1
            )
        }

        // Optional Live Status Badge
        if (badge != null) {
            Surface(
                shape = CircleShape,
                color = if (isDark) Color(0x1EFFFFFF) else Color(0x0C000000),
                border = BorderStroke(1.dp, if (isDark) Color(0x35FFFFFF) else Color(0x16000000)),
                modifier = Modifier.padding(end = 8.dp)
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
private fun LiquidGlassDivider(
    isDark: Boolean = isSystemInDarkTheme()
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
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun launchSettingsSearch(context: Context) {
    try {
        val activity = context as? Activity
            ?: (context as? ContextWrapper)?.baseContext as? Activity
        val featureFactory = FeatureFactory.featureFactory
        val searchProvider = featureFactory.searchFeatureProvider
        val intent = searchProvider.buildSearchIntent(context, SettingsEnums.SETTINGS_HOMEPAGE)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val resolveInfos = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfos.isNotEmpty()) {
            val searchComponentName = resolveInfos[0].componentInfo.componentName
            intent.component = searchComponentName
            ActivityEmbeddingRulesController.registerTwoPanePairRuleForSettingsHome(
                context,
                searchComponentName,
                intent.action,
                false /* finishPrimaryWithSecondary */,
                true /* finishSecondaryWithPrimary */,
                false /* clearTop */
            )
            featureFactory.slicesFeatureProvider.indexSliceDataAsync(context)
            featureFactory.metricsFeatureProvider.action(
                context,
                SettingsEnums.ACTION_SEARCH_RESULTS,
                SettingsEnums.SETTINGS_HOMEPAGE
            )
            if (activity != null) {
                activity.startActivityForResult(intent, SearchFeatureProvider.REQUEST_CODE)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            return
        }
    } catch (e: Throwable) {
        Log.e("XephiraSettings", "Failed to launch native search via provider, falling back", e)
    }

    // Direct Intent Fallback
    try {
        val fallback = Intent(Settings.ACTION_APP_SEARCH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(fallback)
    } catch (e: Throwable) {
        try {
            val intent = Intent("android.settings.APP_SEARCH_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (ignored: Throwable) {}
    }
}

private fun launchIntent(context: Context, action: String, fragmentClass: String?) {
    if (fragmentClass != null) {
        try {
            SubSettingLauncher(context)
                .setDestination(fragmentClass)
                .setSourceMetricsCategory(SettingsEnums.SETTINGS_HOMEPAGE)
                .launch()
            return
        } catch (e: Throwable) {
            Log.w("XephiraSettings", "SubSettingLauncher failed for $fragmentClass, trying Intent fallback", e)
        }
    }
    try {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (fragmentClass != null) {
                putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, fragmentClass)
            }
        }
        context.startActivity(intent)
    } catch (e: Throwable) {
        Log.e("XephiraSettings", "Failed to launch intent $action", e)
    }
}

private fun launchSystemUpdate(context: Context) {
    // 1. Try dedicated LineageOS / XephiraOS Updater components
    val explicitIntents = listOf(
        Intent().setComponent(android.content.ComponentName("org.lineageos.updater", "org.lineageos.updater.UpdatesActivity")),
        Intent("org.lineageos.updater.action.UPDATER"),
        Intent(Settings.ACTION_SYSTEM_UPDATE_SETTINGS)
    )

    for (intent in explicitIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo != null) {
                context.startActivity(intent)
                return
            }
        } catch (ignored: Throwable) {}
    }

    // 2. Try SystemUpdateRepository
    try {
        val repoIntent = com.android.settings.system.SystemUpdateRepository(context).getSystemUpdateIntent()
        if (repoIntent != null) {
            repoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(repoIntent)
            return
        }
    } catch (ignored: Throwable) {}

    // 3. Try SystemDashboardFragment
    try {
        SubSettingLauncher(context)
            .setDestination("com.android.settings.system.SystemDashboardFragment")
            .setSourceMetricsCategory(SettingsEnums.SETTINGS_HOMEPAGE)
            .launch()
        return
    } catch (ignored: Throwable) {}

    // 4. Standalone action fallback strictly for updater
    try {
        val fallback = Intent(Settings.ACTION_SYSTEM_UPDATE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallback)
    } catch (e: Throwable) {
        Log.e("XephiraSettings", "Failed to launch system update", e)
    }
}

private fun launchAboutPhone(context: Context) {
    try {
        SubSettingLauncher(context)
            .setDestination("com.android.settings.deviceinfo.aboutphone.MyDeviceInfoFragment")
            .setSourceMetricsCategory(SettingsEnums.SETTINGS_HOMEPAGE)
            .launch()
        return
    } catch (ignored: Throwable) {}

    try {
        val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Throwable) {
        Log.e("XephiraSettings", "Failed to launch about phone", e)
    }
}

data class WifiLiveState(
    val isEnabled: Boolean,
    val isConnected: Boolean,
    val ssid: String,
    val signalLevel: Int,
    val progress: Float,
    val statusText: String,
    val accentColor: Color
)

private fun getWifiLiveState(context: Context): WifiLiveState {
    return try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val isEnabled = wm?.isWifiEnabled == true
        if (!isEnabled) {
            return WifiLiveState(
                isEnabled = false,
                isConnected = false,
                ssid = "",
                signalLevel = 0,
                progress = 0.0f,
                statusText = "Off",
                accentColor = Color(0xFF8E8E93)
            )
        }

        val network = cm?.activeNetwork
        val caps = if (network != null) cm.getNetworkCapabilities(network) else null
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (isWifi) {
            val wifiInfo = (caps?.transportInfo as? WifiInfo) ?: wm?.connectionInfo
            val rawSsid = wifiInfo?.ssid
            val cleanSsid = rawSsid?.replace("\"", "")?.trim()
            val finalSsid = if (!cleanSsid.isNullOrEmpty() && cleanSsid != "<unknown ssid>") {
                cleanSsid
            } else {
                "Connected"
            }

            val rssi = wifiInfo?.rssi ?: -60
            val level = try {
                if (wm != null) {
                    wm.calculateSignalLevel(rssi)
                } else {
                    WifiManager.calculateSignalLevel(rssi, 5)
                }
            } catch (ignored: Throwable) {
                3
            }
            val progress = ((level + 1).toFloat() / 5f).coerceIn(0.25f, 1.0f)

            WifiLiveState(
                isEnabled = true,
                isConnected = true,
                ssid = finalSsid,
                signalLevel = level,
                progress = progress,
                statusText = finalSsid,
                accentColor = Color(0xFF34C759)
            )
        } else {
            WifiLiveState(
                isEnabled = true,
                isConnected = false,
                ssid = "",
                signalLevel = 0,
                progress = 0.15f,
                statusText = "Available",
                accentColor = Color(0xFFFF9500)
            )
        }
    } catch (e: Throwable) {
        WifiLiveState(
            isEnabled = true,
            isConnected = false,
            ssid = "",
            signalLevel = 0,
            progress = 0.5f,
            statusText = "Wi-Fi",
            accentColor = Color(0xFF34C759)
        )
    }
}

data class BatteryLiveState(
    val percentage: Int,
    val isCharging: Boolean,
    val statusText: String,
    val progress: Float,
    val accentColor: Color
)

private fun getBatteryLiveState(context: Context): BatteryLiveState {
    return try {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val pct = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(0, 100) ?: 85
        }

        val statusText = if (isCharging) "⚡ $pct%" else "$pct%"
        val progress = (pct / 100f).coerceIn(0.05f, 1f)
        val accentColor = when {
            isCharging -> Color(0xFF00C7BE)
            pct <= 20 -> Color(0xFFFF3B30)
            pct <= 40 -> Color(0xFFFF9500)
            else -> Color(0xFF34C759)
        }

        BatteryLiveState(
            percentage = pct,
            isCharging = isCharging,
            statusText = statusText,
            progress = progress,
            accentColor = accentColor
        )
    } catch (e: Throwable) {
        BatteryLiveState(
            percentage = 85,
            isCharging = false,
            statusText = "85%",
            progress = 0.85f,
            accentColor = Color(0xFF34C759)
        )
    }
}

data class StorageLiveDetails(
    val freeGb: Int,
    val totalGb: Int,
    val usedGb: Int,
    val summary: String,
    val statusText: String,
    val progress: Float,
    val accentColor: Color
)

private fun getStorageLiveDetails(context: Context): StorageLiveDetails {
    return try {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val partitionTotalBytes = stat.blockCountLong * stat.blockSizeLong

        var hardwareTotalBytes = 0L
        try {
            hardwareTotalBytes = sm?.primaryStorageSize ?: 0L
        } catch (ignored: Throwable) {}

        val totalGb = if (hardwareTotalBytes > 0L) {
            roundToStandardStorageGb(hardwareTotalBytes)
        } else {
            roundToStandardStorageGb(partitionTotalBytes)
        }

        val freeGb = (availableBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
        val usedGb = (totalGb - freeGb).coerceAtLeast(0)
        val progress = (usedGb.toFloat() / totalGb.coerceAtLeast(1).toFloat()).coerceIn(0.05f, 1f)

        val accentColor = when {
            freeGb < 5 -> Color(0xFFFF3B30)
            freeGb < 15 -> Color(0xFFFF9500)
            else -> Color(0xFF007AFF)
        }

        StorageLiveDetails(
            freeGb = freeGb,
            totalGb = totalGb,
            usedGb = usedGb,
            summary = "$freeGb GB free of $totalGb GB",
            statusText = "$freeGb GB Free",
            progress = progress,
            accentColor = accentColor
        )
    } catch (e: Throwable) {
        StorageLiveDetails(
            freeGb = 64,
            totalGb = 128,
            usedGb = 64,
            summary = "64 GB free of 128 GB",
            statusText = "64 GB Free",
            progress = 0.5f,
            accentColor = Color(0xFF007AFF)
        )
    }
}

private fun roundToStandardStorageGb(bytes: Long): Int {
    val gb = bytes / (1000.0 * 1000.0 * 1000.0)
    val tiers = intArrayOf(16, 32, 64, 128, 256, 512, 1024, 2048)
    for (tier in tiers) {
        if (gb <= tier * 1.05) {
            return tier
        }
    }
    return (bytes / (1024.0 * 1024.0 * 1024.0)).toInt()
}

