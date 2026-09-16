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

package com.android.settings.system

import android.content.Context
import androidx.preference.PreferenceScreen
import com.android.settings.core.BasePreferenceController
import com.android.settings.spa.preference.ComposePreference

/**
 * Controller that binds Xephira's Liquid Glass System & Quantum Core Hero Card.
 */
class XephiraSystemHeroPreferenceController(context: Context, key: String) :
    BasePreferenceController(context, key) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        val preference = screen.findPreference<ComposePreference>(preferenceKey)
        preference?.setContent {
            XephiraSystemHeroCard()
        }
    }
}
