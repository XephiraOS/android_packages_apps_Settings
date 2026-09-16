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

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemProperties
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.settings.R
import com.android.settings.SubSettings
import com.android.settings.widget.liquidglass.pureLiquidGlass

/**
 * Pure Liquid Glass Full Settings Dashboard.
 *
 * Implements an ultra-modern, pure crystal liquid glass interface
 * for the entire Android Settings experience inspired by Kyant0/AndroidLiquidGlass.
 * Integrates SDF normal-gradient refraction, specular white rim sheens,
 * floating glass search bar, hero status glance, and 7 grouped category containers.
 */
@Composable
fun XephiraSettingsDashboard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val xephiraVersion = remember { SystemProperties.get("ro.xephira.version", "1.0") }
    val androidVersion = remember { SystemProperties.get("ro.xephira.android.version", "16") }
    val buildType = remember { SystemProperties.get("ro.xephira.buildtype", "OFFICIAL") }

    // Live device metrics for status pills
    val batteryPct = remember { getBatteryPercentage(context) }
    val storageInfo = remember { getStorageSummary() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── 1. FLOATING PURE LIQUID GLASS SEARCH BAR ─────────────
        LiquidGlassSearchBar(
            onClick = {
                launchIntent(
                    context,
                    Settings.ACTION_APP_SEARCH_SETTINGS,
                    "com.google.android.settings.intelligence.modules.search.SearchActivity"
                )
            }
        )

        // ─── 2. HERO STATUS & QUICK GLANCE ────────────────────────
        LiquidGlassHeroBanner(
            xephiraVersion = xephiraVersion,
            androidVersion = androidVersion,
            buildType = buildType,
            batteryPct = batteryPct,
            storageInfo = storageInfo,
            onHeroClick = {
                launchIntent(context, Settings.ACTION_DEVICE_INFO_SETTINGS, "com.android.settings.deviceinfo.aboutphone.MyDeviceInfoFragment")
            },
            onBatteryClick = {
                launchIntent(context, Intent.ACTION_POWER_USAGE_SUMMARY, "com.android.settings.fuelgauge.PowerUsageSummary")
            },
            onStorageClick = {
                launchIntent(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "com.android.settings.deviceinfo.StorageDashboardFragment")
            },
            onWifiClick = {
                launchIntent(context, Settings.ACTION_WIRELESS_SETTINGS, "com.android.settings.network.NetworkDashboardFragment")
            }
        )

        // ─── 3. CATEGORY: CONNECTIVITY & NETWORK ───────────────────
        LiquidGlassCategoryGroup(title = "CONNECTIVITY") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_wireless_filled,
                title = "Network & internet",
                subtitle = "Mobile, Wi-Fi, hotspot, SIMs",
                onClick = {
                    launchIntent(context, Settings.ACTION_WIRELESS_SETTINGS, "com.android.settings.network.NetworkDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_devices_other_filled,
                title = "Connected devices",
                subtitle = "Bluetooth, pairing, Quick Share",
                onClick = {
                    launchIntent(context, Settings.ACTION_BLUETOOTH_SETTINGS, "com.android.settings.connecteddevice.ConnectedDeviceDashboardFragment")
                }
            )
        }

        // ─── 4. CATEGORY: APPS & NOTIFICATIONS ─────────────────────
        LiquidGlassCategoryGroup(title = "APPLICATIONS") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_apps_filled,
                title = "Apps",
                subtitle = "Recent apps, default apps, permissions",
                onClick = {
                    launchIntent(context, Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS, "com.android.settings.applications.AppDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_notifications_filled,
                title = "Notifications",
                subtitle = "Notification history, conversations",
                onClick = {
                    launchIntent(context, Settings.ACTION_NOTIFICATION_SETTINGS, "com.android.settings.notification.ConfigureNotificationSettings")
                }
            )
        }

        // ─── 5. CATEGORY: DEVICE ESSENTIALS ────────────────────────
        LiquidGlassCategoryGroup(title = "DEVICE ESSENTIALS") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_battery_filled,
                title = "Battery",
                subtitle = "$batteryPct% • Adaptive charging",
                onClick = {
                    launchIntent(context, Intent.ACTION_POWER_USAGE_SUMMARY, "com.android.settings.fuelgauge.PowerUsageSummary")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_storage_filled,
                title = "Storage",
                subtitle = storageInfo,
                onClick = {
                    launchIntent(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS, "com.android.settings.deviceinfo.StorageDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_volume_up_filled,
                title = "Sound & vibration",
                subtitle = "Volume, haptics, Do Not Disturb",
                onClick = {
                    launchIntent(context, Settings.ACTION_SOUND_SETTINGS, "com.android.settings.notification.SoundSettings")
                }
            )
        }

        // ─── 6. CATEGORY: PERSONALIZATION & DISPLAY ────────────────
        LiquidGlassCategoryGroup(title = "PERSONALIZATION") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_display_filled,
                title = "Display",
                subtitle = "Dark theme, refresh rate, screen timeout",
                onClick = {
                    launchIntent(context, Settings.ACTION_DISPLAY_SETTINGS, "com.android.settings.DisplaySettings")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_wallpaper_filled,
                title = "Wallpaper & style",
                subtitle = "Colors, themed icons, lock screen clock",
                onClick = {
                    launchIntent(context, "android.settings.WALLPAPER_SETTINGS", null)
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_accessibility_filled,
                title = "Accessibility",
                subtitle = "Display size, text, screen reader",
                onClick = {
                    launchIntent(context, Settings.ACTION_ACCESSIBILITY_SETTINGS, "com.android.settings.accessibility.AccessibilitySettings")
                }
            )
        }

        // ─── 7. CATEGORY: PRIVACY & SECURITY ───────────────────────
        LiquidGlassCategoryGroup(title = "SECURITY & PRIVACY") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_security_filled,
                title = "Security & biometrics",
                subtitle = "Screen lock, fingerprint, face unlock",
                onClick = {
                    launchIntent(context, Settings.ACTION_SECURITY_SETTINGS, "com.android.settings.security.SecuritySettings")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_privacy_filled,
                title = "Privacy",
                subtitle = "Permissions, camera & mic access",
                onClick = {
                    launchIntent(context, Settings.ACTION_PRIVACY_SETTINGS, "com.android.settings.privacy.PrivacyDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_location_filled,
                title = "Location",
                subtitle = "Location access, app permissions",
                onClick = {
                    launchIntent(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS, "com.android.settings.location.LocationSettings")
                }
            )
        }

        // ─── 8. CATEGORY: ACCOUNTS & SAFETY ────────────────────────
        LiquidGlassCategoryGroup(title = "ACCOUNTS & SAFETY") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_emergency_filled,
                title = "Safety & emergency",
                subtitle = "Emergency SOS, medical info, alerts",
                onClick = {
                    launchIntent(context, "android.settings.SAFETY_CENTER", "com.android.settings.emergency.EmergencyDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_passwords_filled,
                title = "Passwords & accounts",
                subtitle = "Autofill service, saved passwords, Google",
                onClick = {
                    launchIntent(context, Settings.ACTION_SYNC_SETTINGS, "com.android.settings.accounts.AccountDashboardFragment")
                }
            )
        }

        // ─── 9. CATEGORY: SYSTEM & ABOUT PHONE ─────────────────────
        LiquidGlassCategoryGroup(title = "SYSTEM") {
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_system_dashboard_filled,
                title = "System",
                subtitle = "Languages, gestures, date & time, backup",
                onClick = {
                    launchIntent(context, Settings.ACTION_SYSTEM_UPDATE_SETTINGS, "com.android.settings.system.SystemDashboardFragment")
                }
            )
            LiquidGlassDivider()
            LiquidGlassTile(
                iconRes = R.drawable.ic_settings_about_device_filled,
                title = "About phone",
                subtitle = "XephiraOS $xephiraVersion • ${Build.MODEL}",
                onClick = {
                    launchIntent(context, Settings.ACTION_DEVICE_INFO_SETTINGS, "com.android.settings.deviceinfo.aboutphone.MyDeviceInfoFragment")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtle Pure Liquid Glass Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "XephiraOS • Pure Liquid Glass UI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0x60FFFFFF),
                letterSpacing = 1.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUBCOMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LiquidGlassSearchBar(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "search_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .pureLiquidGlass(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                refraction = 12f,
                borderWidth = 1.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = !isPressed
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Search settings...",
                fontSize = 15.sp,
                color = Color(0xAAFFFFFF),
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = CircleShape,
                color = Color(0x20FFFFFF),
                border = BorderStroke(1.dp, Color(0x35FFFFFF)),
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.xephira_x_logo),
                        contentDescription = "Profile",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassHeroBanner(
    xephiraVersion: String,
    androidVersion: String,
    buildType: String,
    batteryPct: Int,
    storageInfo: String,
    onHeroClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onStorageClick: () -> Unit,
    onWifiClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f),
        label = "hero_scale"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main Hero Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .pureLiquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    cornerRadius = 26.dp,
                    refraction = 16f,
                    borderWidth = 1.dp
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = !isPressed
                    onHeroClick()
                }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.xephira_x_logo),
                    contentDescription = "Xephira Logo",
                    modifier = Modifier.size(52.dp)
                )

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
                            color = Color.White
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0x22FFFFFF),
                            border = BorderStroke(1.dp, Color(0x40FFFFFF))
                        ) {
                            Text(
                                text = buildType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Android $androidVersion • ${Build.MODEL}",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0x60FFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3 Quick Status Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_settings_wireless_filled,
                label = "Wi-Fi",
                status = "Active",
                onClick = onWifiClick
            )
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_settings_battery_filled,
                label = "Battery",
                status = "$batteryPct%",
                onClick = onBatteryClick
            )
            LiquidGlassStatusPill(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_storage_filled,
                label = "Storage",
                status = storageInfo.split(" ").firstOrNull() ?: "Ready",
                onClick = onStorageClick
            )
        }
    }
}

@Composable
private fun LiquidGlassStatusPill(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    status: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .pureLiquidGlass(
                shape = RoundedCornerShape(18.dp),
                cornerRadius = 18.dp,
                refraction = 8f,
                borderWidth = 1.dp
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0x90FFFFFF)
            )
        }
    }
}

@Composable
private fun LiquidGlassCategoryGroup(
    title: String,
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
            color = Color(0x85FFFFFF),
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
                    borderWidth = 1.dp
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
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0x1AFFFFFF),
            border = BorderStroke(1.dp, Color(0x30FFFFFF)),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9E9E9E),
                maxLines = 1
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0x45FFFFFF),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun LiquidGlassDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.75.dp)
            .background(Color(0x14FFFFFF))
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun launchIntent(context: Context, action: String, fragmentClass: String?) {
    try {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        if (fragmentClass != null) {
            try {
                val subSettingIntent = Intent(context, SubSettings::class.java).apply {
                    putExtra(":settings:show_fragment", fragmentClass)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(subSettingIntent)
            } catch (ignored: Exception) {}
        }
    }
}

private fun getBatteryPercentage(context: Context): Int {
    return try {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
    } catch (e: Throwable) {
        85
    }
}

private fun getStorageSummary(): String {
    return try {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong
        val usedGb = ((totalBytes - availableBytes) / (1024.0 * 1024.0 * 1024.0)).toInt()
        val totalGb = (totalBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
        "${totalGb - usedGb} GB free of $totalGb GB"
    } catch (e: Throwable) {
        "Storage available"
    }
}
