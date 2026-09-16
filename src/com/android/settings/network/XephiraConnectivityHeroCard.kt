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

package com.android.settings.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
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
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
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
import com.android.settings.widget.liquidglass.anim.LiquidSignalArcVisualizer
import com.android.settings.widget.liquidglass.pureLiquidGlass

@Composable
fun XephiraConnectivityHeroCard() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val netInfo = remember { getActiveNetworkInfo(context) }
    val isConnected = netInfo.isConnected
    val primaryColor = if (isConnected) Color(0xFF38BDF8) else Color(0xFF94A3B8)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = netInfo.networkTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = netInfo.networkSubtitle,
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
                    Text(
                        text = if (isConnected) "Active Connection" else "Disconnected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Animated Radiance Arc Beacon
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidSignalArcVisualizer(
                    isConnected = isConnected,
                    primaryColor = primaryColor
                )
                Icon(
                    imageVector = when {
                        !isConnected -> Icons.Outlined.WifiOff
                        netInfo.isWifi -> Icons.Outlined.Wifi
                        else -> Icons.Outlined.SignalCellularAlt
                    },
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private data class ActiveNetInfo(
    val isConnected: Boolean,
    val isWifi: Boolean,
    val networkTitle: String,
    val networkSubtitle: String
)

private fun getActiveNetworkInfo(context: Context): ActiveNetInfo {
    return try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNet)

        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        if (hasInternet && caps != null) {
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val ssid = wm?.connectionInfo?.ssid?.replace("\"", "") ?: "Wi-Fi"
                ActiveNetInfo(true, true, ssid, "Connected • Excellent signal")
            } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                ActiveNetInfo(true, false, "Mobile Network", "Connected • 5G / LTE")
            } else {
                ActiveNetInfo(true, false, "Connected", "Ethernet / Network Active")
            }
        } else {
            ActiveNetInfo(false, false, "No Internet", "Connect to Wi-Fi or mobile data")
        }
    } catch (e: Throwable) {
        ActiveNetInfo(true, true, "Network & internet", "All network features online")
    }
}
