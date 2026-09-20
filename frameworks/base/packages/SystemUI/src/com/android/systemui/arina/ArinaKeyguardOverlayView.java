package com.android.systemui.arina;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Non-interactive ARINA visual layer drawn inside the real SystemUI Keyguard.
 *
 * It never consumes touch input. AOSP Keyguard keeps ownership of gestures,
 * bouncer presentation, authentication, emergency calling and quick-affordance actions.
 */
public final class ArinaKeyguardOverlayView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String biometricText = "Unlock with biometrics or swipe up";
    private int biometricColor = ArinaKeyguardStyle.COLOR_STEEL;
    private boolean attached;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            if (attached) {
                handler.postDelayed(this, 1000L);
            }
        }
    };

    public ArinaKeyguardOverlayView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setWillNotDraw(false);
        stroke.setStyle(Paint.Style.STROKE);
    }

    public void setBiometricState(ArinaKeyguardAuthObserver.State state) {
        post(() -> {
            switch (state) {
                case BIOMETRIC_LISTENING:
                    biometricText = "Looking for you";
                    biometricColor = ArinaKeyguardStyle.COLOR_CYAN;
                    break;
                case BIOMETRIC_AUTHENTICATED:
                    biometricText = "Unlocked";
                    biometricColor = Color.rgb(68, 214, 165);
                    break;
                case BIOMETRIC_FAILED:
                    biometricText = "Try again or swipe up";
                    biometricColor = Color.rgb(240, 201, 106);
                    break;
                case PRIMARY_CREDENTIAL:
                    biometricText = "Enter your passcode";
                    biometricColor = ArinaKeyguardStyle.COLOR_ICE;
                    break;
                case CREDENTIAL_FAILED:
                    biometricText = "Passcode not accepted";
                    biometricColor = Color.rgb(255, 111, 134);
                    break;
                case LOCKED_OUT:
                    biometricText = "Try again shortly";
                    biometricColor = Color.rgb(240, 201, 106);
                    break;
                case CREDENTIAL_AUTHENTICATED:
                    biometricText = "Unlocked";
                    biometricColor = Color.rgb(68, 214, 165);
                    break;
                case IDLE:
                default:
                    biometricText = "Unlock with biometrics or swipe up";
                    biometricColor = ArinaKeyguardStyle.COLOR_STEEL;
                    break;
            }
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        handler.removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w = getWidth();
        final float h = getHeight();
        if (w <= 0f || h <= 0f) return;

        drawBrand(canvas, w);
        drawClock(canvas, w, h);
        drawBiometricIndicator(canvas, w, h);
        drawSwipeHint(canvas, w, h);
    }

    private void drawBrand(Canvas canvas, float w) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = w / 2f;
        final float top = 54f * density;
        final float width = 92f * density;
        final float height = 30f * density;

        paint.setColor(Color.argb(90, 11, 26, 48));
        RectF capsule = new RectF(
                centerX - width / 2f,
                top,
                centerX + width / 2f,
                top + height);
        canvas.drawRoundRect(capsule, height / 2f, height / 2f, paint);

        stroke.setStrokeWidth(Math.max(1f, density));
        stroke.setColor(Color.argb(55, 216, 240, 255));
        canvas.drawRoundRect(capsule, height / 2f, height / 2f, stroke);

        paint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(11f * density);
        paint.setLetterSpacingCompat(0.18f);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        canvas.drawText("ARINA", centerX, top + 20f * density, paint);
    }

    private void drawClock(Canvas canvas, float w, float h) {
        final float density = getResources().getDisplayMetrics().density;
        final Date now = new Date();
        final String timePattern = DateFormat.is24HourFormat(getContext()) ? "HH:mm" : "h:mm";
        final String time = new SimpleDateFormat(timePattern, Locale.getDefault()).format(now);
        final String date = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now);

        float clockY = Math.min(h * 0.27f, 250f * density);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        paint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        paint.setTextSize(78f * density);
        paint.setShadowLayer(18f * density, 0f, 4f * density, Color.argb(90, 0, 0, 0));
        paint.setLetterSpacingCompat(-0.03f);
        canvas.drawText(time, w / 2f, clockY, paint);
        paint.clearShadowLayer();

        paint.setColor(ArinaKeyguardStyle.COLOR_STEEL);
        paint.setTextSize(16f * density);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        paint.setLetterSpacingCompat(0f);
        canvas.drawText(date, w / 2f, clockY + 32f * density, paint);
    }

    private void drawBiometricIndicator(Canvas canvas, float w, float h) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = w / 2f;
        final float centerY = h - 178f * density;
        final float radius = 22f * density;

        paint.setColor(Color.argb(75, 20, 35, 58));
        canvas.drawCircle(centerX, centerY, radius + 8f * density, paint);

        stroke.setStrokeWidth(2f * density);
        stroke.setColor(biometricColor);
        canvas.drawCircle(centerX, centerY, radius, stroke);

        paint.setColor(biometricColor);
        canvas.drawCircle(centerX, centerY, 4f * density, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        paint.setTextSize(12f * density);
        paint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        canvas.drawText(biometricText, centerX, centerY + 48f * density, paint);
    }

    private void drawSwipeHint(Canvas canvas, float w, float h) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = w / 2f;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        paint.setTextSize(11f * density);
        paint.setColor(Color.argb(185, 234, 247, 255));
        canvas.drawText("Swipe up to unlock", centerX, h - 57f * density, paint);

        paint.setColor(Color.argb(235, 234, 247, 255));
        RectF pill = new RectF(
                centerX - 62f * density,
                h - 31f * density,
                centerX + 62f * density,
                h - 26f * density);
        canvas.drawRoundRect(pill, 999f, 999f, paint);
    }

    /**
     * Paint has no public letter-spacing API. Kept as a semantic hook for the ARINA design
     * system without relying on hidden APIs; TextView-based surfaces use real letter spacing.
     */
    private static final class LetterSpacingPaint extends Paint {}

    private void setPaintLetterSpacingNoop(float value) {}

}
