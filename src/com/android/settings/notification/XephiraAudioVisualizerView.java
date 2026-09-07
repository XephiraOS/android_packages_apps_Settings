/*
 * Copyright (C) 2026 XephiraOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.notification;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Hardware-accelerated fluid audio spectrum visualizer view.
 * Simulates real-time studio acoustics and spatial audio resonance
 * using organic harmonic wave synthesis.
 * Optimized with automatic lifecycle management to consume zero idle battery.
 */
public class XephiraAudioVisualizerView extends View {

    private static final int BAR_COUNT = 15;
    private static final float CORNER_RADIUS = 6f;

    private final Paint mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mBarRect = new RectF();

    private ValueAnimator mAnimator;
    private float mPhase = 0f;
    private boolean mIsRunning = false;

    // Harmonically tuned natural spectrum curve multipliers
    private static final float[] SPECTRUM_WEIGHTS = {
        0.42f, 0.65f, 0.88f, 0.95f, 0.78f,
        0.86f, 1.00f, 0.92f, 0.82f, 0.96f,
        0.75f, 0.90f, 0.84f, 0.62f, 0.38f
    };

    // Color gradient tokens (Crimson to Cyan with specular apex)
    private static final int COLOR_BOTTOM = 0xFFE60026; // Xephira Crimson
    private static final int COLOR_TOP = 0xFF00F2FE;    // Aether Neon Cyan

    public XephiraAudioVisualizerView(Context context) {
        this(context, null);
    }

    public XephiraAudioVisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XephiraAudioVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBarPaint.setStyle(Paint.Style.FILL);
        setupAnimator();
    }

    private void setupAnimator() {
        mAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        mAnimator.setDuration(2400);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mPhase = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    public void start() {
        if (mAnimator != null && !mAnimator.isRunning()) {
            mAnimator.start();
            mIsRunning = true;
        }
    }

    public void stop() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
            mIsRunning = false;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE) {
            start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            start();
        } else {
            stop();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            LinearGradient gradient = new LinearGradient(
                    0, h,
                    0, 0,
                    new int[]{COLOR_BOTTOM, 0xFFE6335C, COLOR_TOP},
                    new float[]{0.0f, 0.6f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            mBarPaint.setShader(gradient);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        final float totalBarSpace = width * 0.88f;
        final float barWidth = (totalBarSpace / BAR_COUNT) * 0.58f;
        final float gap = (totalBarSpace - (barWidth * BAR_COUNT)) / (BAR_COUNT - 1);
        final float startX = (width - totalBarSpace) / 2.0f;
        final float centerY = height / 2.0f;
        final float maxBarHalfHeight = (height * 0.42f);

        for (int i = 0; i < BAR_COUNT; i++) {
            float weight = (i < SPECTRUM_WEIGHTS.length) ? SPECTRUM_WEIGHTS[i] : 0.7f;
            // Harmonic wave synthesis for organic fluid movement
            double wave1 = Math.sin(mPhase + (i * 0.45));
            double wave2 = Math.cos((mPhase * 1.35) + (i * 0.28));
            double composite = Math.abs((wave1 * 0.65) + (wave2 * 0.35));

            float minHalfHeight = 4f;
            float barHalfHeight = (float) (minHalfHeight + (maxBarHalfHeight * weight * composite));

            float left = startX + i * (barWidth + gap);
            float right = left + barWidth;
            float top = centerY - barHalfHeight;
            float bottom = centerY + barHalfHeight;

            mBarRect.set(left, top, right, bottom);
            canvas.drawRoundRect(mBarRect, CORNER_RADIUS, CORNER_RADIUS, mBarPaint);
        }
    }
}
