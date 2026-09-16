/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.Build;
import android.text.BidiFormatter;

import com.android.settings.core.BasePreferenceController;

// LINT.IfChange
public class SimpleBuildNumberPreferenceController extends BasePreferenceController {

    public SimpleBuildNumberPreferenceController(Context context,
            String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE_UNSEARCHABLE;
    }

    @Override
    public CharSequence getSummary() {
        return BidiFormatter.getInstance().unicodeWrap(getSanitizedBuildNumber());
    }

    private String getSanitizedBuildNumber() {
        String custom = android.os.SystemProperties.get("ro.xephira.display.version");
        if (!android.text.TextUtils.isEmpty(custom)) {
            return custom;
        }
        String raw = Build.DISPLAY;
        if (android.text.TextUtils.isEmpty(raw)) {
            String ver = android.os.SystemProperties.get("ro.xephira.version", "1.0");
            String type = android.os.SystemProperties.get("ro.xephira.buildtype", "OFFICIAL");
            return "XephiraOS-" + ver + "-" + Build.DEVICE + "-" + type;
        }
        return raw.replaceAll("(?i)lineage_", "xephira_")
                .replaceAll("(?i)lineage-", "xephira-")
                .replaceAll("(?i)lineageos", "XephiraOS")
                .replaceAll("(?i)lineage", "xephira");
    }
}
// LINT.ThenChange(SimpleBuildNumberPreference.kt)
