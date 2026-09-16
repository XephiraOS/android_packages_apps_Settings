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

import android.app.settings.SettingsEnums
import com.android.settings.R
import com.android.settings.dashboard.DashboardFragment

/**
 * Xephira Liquid Lab settings dashboard.
 * Provides real-time interactive tuning of Liquid Glass optical shaders, refraction depth,
 * chromatic tinting, and 120Hz visualizer refresh dynamics.
 */
class LiquidLabSettings : DashboardFragment() {

    override fun getPreferenceScreenResId(): Int = R.xml.liquid_lab_settings

    override fun getLogTag(): String = TAG

    override fun getMetricsCategory(): Int = SettingsEnums.DISPLAY

    companion object {
        private const val TAG = "LiquidLabSettings"
    }
}
