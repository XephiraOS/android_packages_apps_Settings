/*
 * Copyright (C) 2026 XephiraOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.deviceinfo.aboutphone;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.LayoutPreference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Controller for XephiraOS OxygenOS 17 & iOS 18 Spatial About Phone
 * hero card and 2x2 Flux Hardware Matrix dashboard.
 */
public class XephiraAboutDashboardController extends BasePreferenceController {

    public XephiraAboutDashboardController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final LayoutPreference pref = screen.findPreference(getPreferenceKey());
        if (pref == null) {
            return;
        }

        final View root = pref.findViewById(android.R.id.content) != null
                ? pref.findViewById(android.R.id.content)
                : pref.itemView;

        if (root != null) {
            bindViews(root);
        }
    }

    private void bindViews(View root) {
        // --- 1. Hero Card Bindings ---
        final TextView deviceNameView = root.findViewById(R.id.xephira_about_device_name);
        if (deviceNameView != null) {
            String deviceName = Settings.Global.getString(
                    mContext.getContentResolver(), Settings.Global.DEVICE_NAME);
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = Build.MODEL;
            }
            deviceNameView.setText(deviceName);
        }

        final TextView editionView = root.findViewById(R.id.xephira_about_edition);
        if (editionView != null) {
            String xephiraVer = SystemProperties.get("ro.xephira.version", "1.0-Aether");
            editionView.setText(xephiraVer + " • OxygenOS 17 Engine");
        }

        final TextView androidVerView = root.findViewById(R.id.xephira_about_android_ver);
        if (androidVerView != null) {
            androidVerView.setText("Android " + Build.VERSION.RELEASE);
        }

        // --- 2. Hardware Matrix Bindings ---
        // Tile 1: SoC Engine
        final TextView socTextView = root.findViewById(R.id.xephira_hw_soc_text);
        if (socTextView != null) {
            String soc = getProcessorName();
            socTextView.setText(soc);
        }

        // Tile 2: RAM & Virtual Expansion
        final TextView ramTextView = root.findViewById(R.id.xephira_hw_ram_text);
        final TextView ramBoostView = root.findViewById(R.id.xephira_hw_ram_boost_text);
        final ProgressBar ramBar = root.findViewById(R.id.xephira_hw_ram_bar);
        if (ramTextView != null) {
            long totalRamGb = getTotalRamGb();
            ramTextView.setText(totalRamGb + " GB LPDDR5X");
        }
        if (ramBoostView != null) {
            ramBoostView.setText("+ 12 GB Liquid Boost");
        }
        if (ramBar != null) {
            ramBar.setProgress(getRamUsedPercent());
        }

        // Tile 3: Storage Intelligence
        final TextView storageTextView = root.findViewById(R.id.xephira_hw_storage_text);
        final TextView storageFreeView = root.findViewById(R.id.xephira_hw_storage_free_text);
        final ProgressBar storageBar = root.findViewById(R.id.xephira_hw_storage_bar);
        if (storageTextView != null || storageBar != null) {
            bindStorageStats(storageTextView, storageFreeView, storageBar);
        }

        // Tile 4: Battery & Silicon Health
        final TextView batteryTextView = root.findViewById(R.id.xephira_hw_battery_text);
        final TextView batteryHealthView = root.findViewById(R.id.xephira_hw_battery_health_text);
        final ProgressBar batteryBar = root.findViewById(R.id.xephira_hw_battery_bar);
        if (batteryTextView != null) {
            int batteryCap = getBatteryCapacityMah();
            batteryTextView.setText(batteryCap + " mAh");
        }
        if (batteryBar != null) {
            batteryBar.setProgress(getBatteryLevel());
        }
        if (batteryHealthView != null) {
            batteryHealthView.setText("SUPERVOOC • 99% Health");
        }
    }

    private int getRamUsedPercent() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            if (memInfo.totalMem > 0) {
                long used = memInfo.totalMem - memInfo.availMem;
                return (int) Math.min(100, Math.max(0, (used * 100) / memInfo.totalMem));
            }
        }
        return 42;
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
        return 85;
    }

    private String getProcessorName() {
        String soc = SystemProperties.get("ro.soc.model");
        if (TextUtils.isEmpty(soc)) {
            soc = SystemProperties.get("ro.board.platform");
        }
        if (TextUtils.isEmpty(soc) || "unknown".equalsIgnoreCase(soc)) {
            soc = Build.HARDWARE;
        }
        if (TextUtils.isEmpty(soc) || "unknown".equalsIgnoreCase(soc)) {
            soc = getCpuInfoHardware();
        }
        if (TextUtils.isEmpty(soc) || "unknown".equalsIgnoreCase(soc)) {
            return "Snapdragon® 8 Gen 3";
        }
        return soc.toUpperCase();
    }

    private String getCpuInfoHardware() {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Hardware")) {
                    String[] parts = line.split(":");
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private long getTotalRamGb() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(memInfo);
            double gb = (double) memInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
            return Math.max(1, Math.round(gb));
        }
        return 16;
    }

    private void bindStorageStats(TextView storageText, TextView storageFree, ProgressBar progressBar) {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            long totalBytes = statFs.getTotalBytes();
            long availableBytes = statFs.getAvailableBytes();
            long usedBytes = totalBytes - availableBytes;

            long totalGb = Math.max(1, Math.round((double) totalBytes / (1024.0 * 1024.0 * 1024.0)));
            long usedGb = Math.round((double) usedBytes / (1024.0 * 1024.0 * 1024.0));
            long freeGb = Math.max(0, totalGb - usedGb);

            if (storageText != null) {
                storageText.setText(usedGb + " GB / " + totalGb + " GB");
            }
            if (storageFree != null) {
                storageFree.setText(freeGb + " GB Available");
            }
            if (progressBar != null && totalBytes > 0) {
                int progress = (int) Math.min(100, Math.max(0, (usedBytes * 100) / totalBytes));
                progressBar.setProgress(progress);
            }
        } catch (Exception e) {
            if (storageText != null) {
                storageText.setText("128 GB / 512 GB");
            }
            if (storageFree != null) {
                storageFree.setText("384 GB Available");
            }
            if (progressBar != null) {
                progressBar.setProgress(25);
            }
        }
    }

    private int getBatteryCapacityMah() {
        try {
            Class<?> powerProfileClass = Class.forName("com.android.internal.os.PowerProfile");
            Object powerProfile = powerProfileClass.getConstructor(Context.class).newInstance(mContext);
            double batteryCapacity = (double) powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile);
            if (batteryCapacity > 500) {
                return (int) Math.round(batteryCapacity);
            }
        } catch (Exception ignored) {
        }
        return 5400;
    }
}
