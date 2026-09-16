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

package com.android.settings.widget.liquidglass.haptics

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * High-precision tactile haptic feedback controller tailored for Xephira Liquid Glass surfaces.
 * Provides distinct physical sensations for taps, continuous sliders, gestures, and confirmations.
 */
class XephiraHaptics(private val view: View) {

    /**
     * Subtle light glass click for card interactions and category selections.
     */
    fun lightClick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Crisp confirmation tick for toggles, switches, and primary actions.
     */
    fun confirm() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONFIRM,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Continuous fine detent tick for sliders and rotary adjustments.
     */
    fun tick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Fluid gesture impulse for drag starts, visualizer ripples, and bounce animations.
     */
    fun gesturePulse() {
        view.performHapticFeedback(
            HapticFeedbackConstants.GESTURE_START,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }

    /**
     * Firm, prominent glass click for hero actions, easter eggs, and major state transitions.
     */
    fun heavyClick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}

/**
 * Composable helper to remember a [XephiraHaptics] instance bound to the active View.
 */
@Composable
fun rememberXephiraHaptics(): XephiraHaptics {
    val view = LocalView.current
    return remember(view) { XephiraHaptics(view) }
}
