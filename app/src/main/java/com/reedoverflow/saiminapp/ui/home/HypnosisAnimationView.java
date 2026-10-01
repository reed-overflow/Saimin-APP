package com.reedoverflow.saiminapp.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import com.reedoverflow.saiminapp.data.ModeConfig;

/** Size is relative to the shorter side, including in landscape and fullscreen. */
public class HypnosisAnimationView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path spiral = new Path();
    private float spiralRadius = -1;
    private String type = "ripple";
    private int sizePercent = 90;
    private int durationMs = 5000;
    private int color = Color.rgb(240, 98, 146);
    private boolean running;
    private long startedAt;

    public HypnosisAnimationView(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public HypnosisAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void configure(ModeConfig mode) {
        sizePercent = mode.animationSizePercent;
        durationMs = mode.animationDurationMs;
        color = Color.parseColor(mode.animationColor);
        type = mode.animationType;
        setBackgroundColor(Color.parseColor(mode.backgroundColor));
        invalidate();
    }

    public void start() {
        if (running) return;
        startedAt = SystemClock.elapsedRealtime();
        running = true;
        invalidate();
    }

    public void stop() {
        running = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!running) return;
        float density = getResources().getDisplayMetrics().density;
        float radius = Math.max(0, Math.min(getWidth(), getHeight()) / 2f - 4 * density) * sizePercent / 100f;
        long elapsed = SystemClock.elapsedRealtime() - startedAt;
        float phase = (elapsed % durationMs) / (float) durationMs;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3 * density);
        paint.setColor(color);
        paint.setAlpha(255);
        if ("spiral".equals(type)) {
            if (spiralRadius != radius) {
                spiralRadius = radius;
                spiral.rewind();
                for (int i = 0; i <= 480; i++) {
                    float distance = radius * i / 480f;
                    double angle = i / 480.0 * Math.PI * 6;
                    float x = (float) Math.cos(angle) * distance;
                    float y = (float) Math.sin(angle) * distance;
                    if (i == 0) spiral.moveTo(x, y);
                    else spiral.lineTo(x, y);
                }
            }
            canvas.save();
            canvas.translate(getWidth() / 2f, getHeight() / 2f);
            canvas.rotate(phase * 360);
            paint.setStrokeWidth(Math.max(density, radius / 16f));
            for (int arm = 0; arm < 2; arm++) {
                canvas.drawPath(spiral, paint);
                canvas.rotate(180);
            }
            canvas.restore();
        } else if ("pendulum".equals(type)) {
            float cx = getWidth() / 2f;
            float top = getHeight() / 2f - radius * 0.8f;
            double angle = Math.sin(phase * Math.PI * 2) * Math.PI / 4;
            float x = cx + (float) Math.sin(angle) * radius * 1.3f;
            float y = top + (float) Math.cos(angle) * radius * 1.3f;
            canvas.drawLine(cx, top, x, y, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, radius * 0.16f, paint);
        } else {
            // Original RippleBackground: ten staggered rings, scale 1 to 15,
            // with AccelerateDecelerateInterpolator on both scale and opacity.
            for (int i = 0; i < 10; i++) {
                long age = elapsed - i * durationMs / 10L;
                if (age < 0) continue;
                float position = (age % durationMs) / (float) durationMs;
                float eased = (1 - (float) Math.cos(Math.PI * position)) / 2;
                float scale = 1 + 14 * eased;
                float ringRadius = radius * scale / 15;
                paint.setStrokeWidth(Math.max(density, ringRadius / 12));
                paint.setAlpha((int) ((1 - eased) * 255));
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, ringRadius, paint);
            }
        }
        if (running && isShown()) postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }
}
