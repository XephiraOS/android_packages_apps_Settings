/*
 * Copyright (C) 2026 XephiraOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.notification;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.LayoutPreference;

import org.jetbrains.annotations.NotNull;

/**
 * Controller for XephiraOS Spatial Sound Visualizer Hero Card.
 * Manages spectrum animation lifecycle, acoustic mode presets, and haptic feedback.
 */
public class XephiraSoundVisualizerController extends BasePreferenceController
        implements DefaultLifecycleObserver {

    private static final String PREF_KEY = "xephira_sound_visualizer_hero";

    private static final String[] PRESET_TITLES = {
        "Adaptive Spatial Audio • Dolby Atmos & Dirac",
        "Studio Lossless • 24-bit Pure Bitstream",
        "Acoustic Resonance • O-Haptics 2.0 Turbo"
    };

    private static final String[] PRESET_BADGES = {
        "Hi-Res 192kHz",
        "Lossless Master",
        "Resonance MAX"
    };

    private XephiraAudioVisualizerView mVisualizerView;
    private TextView mStatusTextView;
    private TextView mBadgeView;
    private int mCurrentPresetIndex = 0;

    public XephiraSoundVisualizerController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public XephiraSoundVisualizerController(Context context) {
        this(context, PREF_KEY);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(getPreferenceKey());
        if (pref instanceof LayoutPreference) {
            final LayoutPreference layoutPref = (LayoutPreference) pref;
            final View root = layoutPref.findViewById(R.id.xephira_sound_hero_container);
            if (root != null) {
                mVisualizerView = root.findViewById(R.id.xephira_sound_visualizer);
                mStatusTextView = root.findViewById(R.id.xephira_sound_engine_status);
                mBadgeView = root.findViewById(R.id.xephira_sound_profile_badge);

                root.setOnClickListener(v -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    cycleAcousticPreset();
                });
            }
        }
    }

    private void cycleAcousticPreset() {
        mCurrentPresetIndex = (mCurrentPresetIndex + 1) % PRESET_TITLES.length;
        if (mStatusTextView != null) {
            mStatusTextView.setText(PRESET_TITLES[mCurrentPresetIndex]);
        }
        if (mBadgeView != null) {
            mBadgeView.setText(PRESET_BADGES[mCurrentPresetIndex]);
        }
    }

    @Override
    public void onResume(@NotNull LifecycleOwner owner) {
        if (mVisualizerView != null) {
            mVisualizerView.start();
        }
    }

    @Override
    public void onPause(@NotNull LifecycleOwner owner) {
        if (mVisualizerView != null) {
            mVisualizerView.stop();
        }
    }
}
