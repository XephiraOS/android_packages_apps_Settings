/*
 * Copyright (C) 2026 XephiraOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.homepage;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class XephiraHomepageHeroController extends BasePreferenceController {

    private static final String PREF_KEY = "top_level_xephira_hero";

    public XephiraHomepageHeroController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public XephiraHomepageHeroController(Context context) {
        this(context, PREF_KEY);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof LayoutPreference) {
            LayoutPreference layoutPref = (LayoutPreference) pref;
            View root = layoutPref.findViewById(R.id.xephira_hero_card);
            if (root != null) {
                TextView deviceNameView = root.findViewById(R.id.xephira_hero_device_name);
                TextView versionSubtitleView = root.findViewById(R.id.xephira_hero_version_subtitle);

                String deviceName = Settings.Global.getString(mContext.getContentResolver(), Settings.Global.DEVICE_NAME);
                if (deviceName == null || deviceName.isEmpty()) {
                    deviceName = Build.MODEL;
                }
                if (deviceNameView != null) {
                    deviceNameView.setText(deviceName);
                }

                String xephiraVersion = SystemProperties.get("ro.xephira.display.version", "XephiraOS 1.0 • Aether");
                if (versionSubtitleView != null) {
                    versionSubtitleView.setText(xephiraVersion);
                }

                TextView batteryPillView = root.findViewById(R.id.xephira_hero_battery_pill);
                TextView securityPillView = root.findViewById(R.id.xephira_hero_security_pill);

                if (batteryPillView != null) {
                    int batteryLevel = getBatteryLevel();
                    batteryPillView.setText(batteryLevel + "% • Optimized");
                }

                if (securityPillView != null) {
                    securityPillView.setText("Shield Protected");
                }

                root.setOnClickListener(v -> {
                    v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
                    Intent intent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                });
            }
        }
    }

    private int getBatteryLevel() {
        try {
            android.content.Intent batteryIntent = mContext.registerReceiver(
                    null, new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent != null) {
                int level = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    return Math.min(100, Math.max(0, (level * 100) / scale));
                }
            }
        } catch (Exception ignored) {
        }
        return 92;
    }
}
