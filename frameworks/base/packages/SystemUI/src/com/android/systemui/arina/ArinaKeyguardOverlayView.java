package com.android.systemui.arina;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
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
 * AOSP Keyguard remains responsible for gestures, bouncer presentation,
 * authentication, emergency calling, doze/AOD and quick-affordance actions.
 */
public final class ArinaKeyguardOverlayView extends View {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private String mBiometricText = "Unlock with biometrics or swipe up";
    private int mBiometricColor = ArinaKeyguardStyle.COLOR_STEEL;
    private boolean mAttached;

    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            if (mAttached) {
                mHandler.postDelayed(this, 1000L);
            }
        }
    };

    public ArinaKeyguardOverlayView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setWillNotDraw(false);
        mStroke.setStyle(Paint.Style.STROKE);
    }

    public void setBiometricState(ArinaKeyguardAuthObserver.State state) {
        post(() -> {
            switch (state) {
                case BIOMETRIC_LISTENING:
                    mBiometricText = "Looking for you";
                    mBiometricColor = ArinaKeyguardStyle.COLOR_CYAN;
                    break;
                case BIOMETRIC_AUTHENTICATED:
                case CREDENTIAL_AUTHENTICATED:
                    mBiometricText = "Unlocked";
                    mBiometricColor = Color.rgb(68, 214, 165);
                    break;
                case BIOMETRIC_FAILED:
                    mBiometricText = "Try again or swipe up";
                    mBiometricColor = Color.rgb(240, 201, 106);
                    break;
                case PRIMARY_CREDENTIAL:
                    mBiometricText = "Enter your passcode";
                    mBiometricColor = ArinaKeyguardStyle.COLOR_ICE;
                    break;
                case CREDENTIAL_FAILED:
                    mBiometricText = "Passcode not accepted";
                    mBiometricColor = Color.rgb(255, 111, 134);
                    break;
                case LOCKED_OUT:
                    mBiometricText = "Try again shortly";
                    mBiometricColor = Color.rgb(240, 201, 106);
                    break;
                case IDLE:
                default:
                    mBiometricText = "Unlock with biometrics or swipe up";
                    mBiometricColor = ArinaKeyguardStyle.COLOR_STEEL;
                    break;
            }
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached = true;
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttached = false;
        mHandler.removeCallbacks(mTick);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float width = getWidth();
        final float height = getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }

        drawBrand(canvas, width);
        drawClock(canvas, width, height);
        drawBiometricIndicator(canvas, width, height);
        drawSwipeHint(canvas, width, height);
    }

    private void drawBrand(Canvas canvas, float width) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = width / 2f;
        final float top = 54f * density;
        final float capsuleWidth = 92f * density;
        final float capsuleHeight = 30f * density;

        RectF capsule = new RectF(
                centerX - capsuleWidth / 2f,
                top,
                centerX + capsuleWidth / 2f,
                top + capsuleHeight);

        mPaint.setColor(Color.argb(90, 11, 26, 48));
        canvas.drawRoundRect(capsule, capsuleHeight / 2f, capsuleHeight / 2f, mPaint);

        mStroke.setStrokeWidth(Math.max(1f, density));
        mStroke.setColor(Color.argb(55, 216, 240, 255));
        canvas.drawRoundRect(capsule, capsuleHeight / 2f, capsuleHeight / 2f, mStroke);

        mPaint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(11f * density);
        mPaint.setTypeface(Typeface.create("sans", Typeface.BOLD));
        canvas.drawText("ARINA", centerX, top + 20f * density, mPaint);
    }

    private void drawClock(Canvas canvas, float width, float height) {
        final float density = getResources().getDisplayMetrics().density;
        final Date now = new Date();
        final String timePattern = DateFormat.is24HourFormat(getContext()) ? "HH:mm" : "h:mm";
        final String time = new SimpleDateFormat(timePattern, Locale.getDefault()).format(now);
        final String date = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now);

        final float clockY = Math.min(height * 0.27f, 250f * density);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        mPaint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        mPaint.setTextSize(78f * density);
        mPaint.setShadowLayer(18f * density, 0f, 4f * density, Color.argb(90, 0, 0, 0));
        canvas.drawText(time, width / 2f, clockY, mPaint);
        mPaint.clearShadowLayer();

        mPaint.setColor(ArinaKeyguardStyle.COLOR_STEEL);
        mPaint.setTextSize(16f * density);
        canvas.drawText(date, width / 2f, clockY + 32f * density, mPaint);
    }

    private void drawBiometricIndicator(Canvas canvas, float width, float height) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = width / 2f;
        final float centerY = height - 178f * density;
        final float radius = 22f * density;

        mPaint.setColor(Color.argb(75, 20, 35, 58));
        canvas.drawCircle(centerX, centerY, radius + 8f * density, mPaint);

        mStroke.setStrokeWidth(2f * density);
        mStroke.setColor(mBiometricColor);
        canvas.drawCircle(centerX, centerY, radius, mStroke);

        mPaint.setColor(mBiometricColor);
        canvas.drawCircle(centerX, centerY, 4f * density, mPaint);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        mPaint.setTextSize(12f * density);
        mPaint.setColor(ArinaKeyguardStyle.COLOR_ICE);
        canvas.drawText(mBiometricText, centerX, centerY + 48f * density, mPaint);
    }

    private void drawSwipeHint(Canvas canvas, float width, float height) {
        final float density = getResources().getDisplayMetrics().density;
        final float centerX = width / 2f;

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        mPaint.setTextSize(11f * density);
        mPaint.setColor(Color.argb(185, 234, 247, 255));
        canvas.drawText("Swipe up to unlock", centerX, height - 57f * density, mPaint);

        mPaint.setColor(Color.argb(235, 234, 247, 255));
        RectF pill = new RectF(
                centerX - 62f * density,
                height - 31f * density,
                centerX + 62f * density,
                height - 26f * density);
        canvas.drawRoundRect(pill, 999f, 999f, mPaint);
    }
}
