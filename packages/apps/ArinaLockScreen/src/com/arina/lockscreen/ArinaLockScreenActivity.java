package com.arina.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ArinaLockScreenActivity extends Activity {

    private static final int ICE = Color.rgb(234, 247, 255);
    private static final int STEEL = Color.rgb(169, 193, 216);
    private static final int BLUE = Color.rgb(74, 125, 255);
    private static final int CYAN = Color.rgb(53, 198, 255);

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private TextView timeView;
    private TextView dateView;
    private float touchStartY;

    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            clockHandler.postDelayed(this, 1000L);
        }
    };

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView label(String value, float sizeSp, int color, int gravity) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setGravity(gravity);
        view.setFontFeatureSettings("kern");
        view.setIncludeFontPadding(false);
        return view;
    }

    private GradientDrawable rounded(int fill, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), Color.argb(40, 216, 240, 255));
        return drawable;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setDecorFitsSystemWindows(false);

        FrameLayout root = new FrameLayout(this);
        root.addView(new ArinaLockWallpaperView(this),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(20), dp(18), dp(20), dp(18));
        root.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView brand = label("ARINA", 12, ICE, Gravity.CENTER);
        brand.setLetterSpacing(0.22f);
        brand.setBackground(rounded(Color.argb(88, 20, 35, 58), 999));
        brand.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams brandParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34));
        brandParams.setMargins(0, dp(8), 0, dp(18));
        content.addView(brand, brandParams);

        timeView = label("", 82, ICE, Gravity.CENTER);
        timeView.setLetterSpacing(-0.02f);
        timeView.setShadowLayer(14f, 0f, 2f, Color.argb(105, 0, 0, 0));
        content.addView(timeView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(104)));

        dateView = label("", 16, STEEL, Gravity.CENTER);
        content.addView(dateView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(34)));

        TextView intelligence = label("ARINA · ready when you are", 13, ICE, Gravity.CENTER);
        intelligence.setBackground(rounded(Color.argb(82, 20, 35, 58), 18));
        LinearLayout.LayoutParams intelligenceParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36));
        intelligenceParams.setMargins(0, dp(14), 0, dp(22));
        intelligence.setPadding(dp(16), 0, dp(16), 0);
        content.addView(intelligence, intelligenceParams);

        LinearLayout notificationCard = buildNotificationCard();
        LinearLayout.LayoutParams notificationParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(86));
        notificationParams.setMargins(0, dp(4), 0, 0);
        content.addView(notificationCard, notificationParams);

        View spacer = new View(this);
        content.addView(spacer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout actions = buildBottomActions();
        content.addView(actions, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(66)));

        TextView swipe = label("Swipe up", 12, Color.argb(190, 234, 247, 255), Gravity.CENTER);
        LinearLayout.LayoutParams swipeParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(28));
        swipeParams.setMargins(0, dp(6), 0, 0);
        content.addView(swipe, swipeParams);

        TextView homeIndicator = new TextView(this);
        homeIndicator.setBackground(rounded(Color.argb(230, 234, 247, 255), 999));
        LinearLayout.LayoutParams indicatorParams =
                new LinearLayout.LayoutParams(dp(126), dp(5));
        indicatorParams.setMargins(0, dp(2), 0, dp(4));
        content.addView(homeIndicator, indicatorParams);

        root.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touchStartY = event.getY();
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                float delta = event.getY() - touchStartY;
                if (delta < -dp(110)) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    content.animate()
                            .translationY(-dp(80))
                            .alpha(0f)
                            .setDuration(220)
                            .withEndAction(this::finish)
                            .start();
                }
                return true;
            }
            return true;
        });

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            content.setPadding(dp(20), top + dp(6), dp(20), Math.max(dp(10), bottom + dp(2)));
            return insets;
        });

        setContentView(root);
        updateClock();
    }

    private LinearLayout buildNotificationCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setBackground(rounded(Color.argb(118, 16, 35, 63), 24));
        card.setElevation(dp(8));

        TextView title = label("No New Notifications", 15, ICE, Gravity.START | Gravity.CENTER_VERTICAL);
        TextView subtitle = label("ARINA keeps the Lock Screen quiet until something needs you.", 12, STEEL, Gravity.START | Gravity.CENTER_VERTICAL);
        subtitle.setMaxLines(2);

        card.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(28)));
        card.addView(subtitle, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(34)));

        return card;
    }

    private LinearLayout buildBottomActions() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView light = actionButton("✦", "Torch");
        light.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            animatePress(v);
            Toast.makeText(this, "Torch control will be wired through SystemUI Keyguard.", Toast.LENGTH_SHORT).show();
        });

        TextView camera = actionButton("◉", "Camera");
        camera.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            animatePress(v);
            try {
                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception error) {
                try {
                    startActivity(new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA));
                } catch (Exception ignored) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });

        row.addView(light, new LinearLayout.LayoutParams(dp(60), dp(60)));

        View center = new View(this);
        row.addView(center, new LinearLayout.LayoutParams(0, dp(1), 1f));

        row.addView(camera, new LinearLayout.LayoutParams(dp(60), dp(60)));
        return row;
    }

    private TextView actionButton(String glyph, String description) {
        TextView view = label(glyph, 25, ICE, Gravity.CENTER);
        view.setContentDescription(description);
        view.setBackground(rounded(Color.argb(130, 20, 35, 58), 999));
        view.setElevation(dp(8));
        return view;
    }

    private void animatePress(View view) {
        view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void updateClock() {
        Date now = new Date();
        String timePattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "h:mm";
        timeView.setText(new SimpleDateFormat(timePattern, Locale.getDefault()).format(now));
        dateView.setText(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now));
    }

    @Override
    protected void onStart() {
        super.onStart();
        clockHandler.removeCallbacks(clockTick);
        clockHandler.post(clockTick);
    }

    @Override
    protected void onStop() {
        clockHandler.removeCallbacks(clockTick);
        super.onStop();
    }
}
