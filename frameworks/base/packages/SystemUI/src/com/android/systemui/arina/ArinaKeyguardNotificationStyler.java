package com.android.systemui.arina;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * ARINA presentation hooks for the real AOSP notification rows on Keyguard.
 *
 * Important:
 * - AOSP owns grouping, ranking, visibility and notification actions.
 * - AOSP SensitiveContentCoordinator / NotificationLockscreenUserManager own redaction.
 * - ARINA only mirrors those trusted states into visual treatment and motion.
 * - No touch listener is replaced, so swipe, long-press, inline actions and accessibility stay AOSP.
 */
public final class ArinaKeyguardNotificationStyler {

    private static final WeakHashMap<View, Boolean> sSensitiveRows = new WeakHashMap<>();
    private static final WeakHashMap<View, Boolean> sExpandedRows = new WeakHashMap<>();

    private ArinaKeyguardNotificationStyler() {}

    public static void styleStack(ViewGroup stack) {
        if (stack == null) return;

        final float density = stack.getResources().getDisplayMetrics().density;
        GradientDrawable glass = new GradientDrawable();
        glass.setColor(Color.argb(46, 16, 35, 63));
        glass.setCornerRadius(28f * density);
        glass.setStroke(Math.max(1, Math.round(density)), Color.argb(34, 216, 240, 255));
        stack.setBackground(glass);
        stack.setOutlineProvider(new RoundOutlineProvider(28f * density));

        for (int i = 0; i < stack.getChildCount(); i++) {
            View child = stack.getChildAt(i);
            if (isNotificationRow(child)) {
                styleRow(child);
            }
        }
    }

    public static void styleRow(View row) {
        if (row == null || !isNotificationRow(row)) return;

        final float density = row.getResources().getDisplayMetrics().density;
        final boolean summary = invokeBoolean(row, "isSummaryWithChildren", false);
        final boolean groupChild = invokeBoolean(row, "isChildInGroup", false);
        final boolean childrenExpanded = invokeBoolean(row, "areChildrenExpanded", false);
        final boolean userExpanded = invokeBoolean(row, "isUserExpanded", false);
        final boolean expanded = childrenExpanded || userExpanded;
        final boolean sensitive = resolveSensitive(row);

        final float radiusDp;
        final float elevationDp;

        if (groupChild) {
            radiusDp = 18f;
            elevationDp = 2f;
        } else if (summary && expanded) {
            radiusDp = 22f;
            elevationDp = 7f;
        } else if (summary) {
            radiusDp = 26f;
            elevationDp = 10f;
        } else if (expanded) {
            radiusDp = 24f;
            elevationDp = 8f;
        } else {
            radiusDp = 24f;
            elevationDp = 6f;
        }

        row.setOutlineProvider(new RoundOutlineProvider(radiusDp * density));
        // Do not clip: AOSP swipe menus, guts and expand animations must remain unobstructed.
        row.setClipToOutline(false);
        row.setElevation(elevationDp * density);

        // Keep grouped children visually subordinate without changing their gesture translation.
        if (groupChild) {
            row.setScaleX(0.985f);
            row.setAlpha(sensitive ? 0.88f : 0.96f);
        } else {
            row.setScaleX(1f);
            row.setAlpha(sensitive ? 0.92f : 1f);
        }

        stylePublicRedactionSurface(row, sensitive);
        sExpandedRows.put(row, expanded);
    }

    /**
     * Called only after AOSP has computed the real privacy state.
     * ARINA does not decide whether notification content is private.
     */
    public static void onPrivacyStateChanged(
            View row, boolean sensitive, boolean deviceSensitive) {
        if (row == null) return;
        sSensitiveRows.put(row, sensitive);
        stylePublicRedactionSurface(row, sensitive);
        styleRow(row);
    }

    /**
     * Called from ExpandableNotificationRow's existing user-expansion path.
     * Does not change the expansion decision; it only adds ARINA motion/haptic feedback.
     */
    public static void onUserExpansionRequested(View row, boolean expanding) {
        if (row == null) return;

        row.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        row.animate().cancel();
        row.animate()
                .scaleY(0.985f)
                .setDuration(ArinaKeyguardStyle.PRESS_DURATION_MS)
                .withEndAction(() -> {
                    row.animate()
                            .scaleY(1f)
                            .alpha(resolveSensitive(row) ? 0.92f : 1f)
                            .setDuration(ArinaKeyguardStyle.REVEAL_DURATION_MS)
                            .start();
                    styleRow(row);
                })
                .start();
    }

    /**
     * Mirrors the existing AOSP grouped-child expansion into ARINA depth animation.
     */
    public static void onGroupExpansionChanged(View row, boolean expanded) {
        if (row == null) return;
        Boolean previous = sExpandedRows.get(row);
        sExpandedRows.put(row, expanded);

        if (previous != null && previous == expanded) {
            styleRow(row);
            return;
        }

        row.animate().cancel();
        row.setAlpha(Math.min(row.getAlpha(), 0.94f));
        row.animate()
                .alpha(resolveSensitive(row) ? 0.92f : 1f)
                .setDuration(ArinaKeyguardStyle.REVEAL_DURATION_MS)
                .withEndAction(() -> styleRow(row))
                .start();
    }

    private static void stylePublicRedactionSurface(View row, boolean sensitive) {
        int publicId = id(row, "expandedPublic");
        if (publicId == 0) return;

        View publicLayout = row.findViewById(publicId);
        if (publicLayout == null) return;

        final float density = row.getResources().getDisplayMetrics().density;
        GradientDrawable privacyGlass = new GradientDrawable();
        privacyGlass.setCornerRadius(22f * density);

        if (sensitive) {
            privacyGlass.setColor(Color.argb(102, 8, 20, 38));
            privacyGlass.setStroke(
                    Math.max(1, Math.round(density)),
                    Color.argb(72, 53, 198, 255));
            publicLayout.setAlpha(0.96f);
        } else {
            privacyGlass.setColor(Color.TRANSPARENT);
            privacyGlass.setStroke(0, Color.TRANSPARENT);
            publicLayout.setAlpha(1f);
        }
        publicLayout.setBackground(privacyGlass);
    }

    private static boolean resolveSensitive(View row) {
        Boolean cached = sSensitiveRows.get(row);
        if (cached != null) return cached;

        try {
            Method getEntry = row.getClass().getMethod("getEntry");
            Object entry = getEntry.invoke(row);
            if (entry == null) return false;

            Method isSensitive = entry.getClass().getMethod("isSensitive");
            Object value = isSensitive.invoke(entry);

            if (value instanceof Boolean) {
                boolean sensitive = (Boolean) value;
                sSensitiveRows.put(row, sensitive);
                return sensitive;
            }

            // Newer Android branches expose sensitivity as StateFlow<Boolean>.
            if (value != null) {
                Method getValue = value.getClass().getMethod("getValue");
                Object flowValue = getValue.invoke(value);
                if (flowValue instanceof Boolean) {
                    boolean sensitive = (Boolean) flowValue;
                    sSensitiveRows.put(row, sensitive);
                    return sensitive;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Privacy is still enforced by AOSP even if the optional ARINA visual lookup fails.
        }
        return false;
    }

    private static boolean invokeBoolean(View view, String methodName, boolean fallback) {
        try {
            Method method = view.getClass().getMethod(methodName);
            Object value = method.invoke(view);
            return value instanceof Boolean ? (Boolean) value : fallback;
        } catch (ReflectiveOperationException ignored) {
            return fallback;
        }
    }

    private static boolean isNotificationRow(View view) {
        return view != null
                && view.getClass().getName().endsWith(".ExpandableNotificationRow");
    }

    private static int id(View view, String name) {
        return view.getResources().getIdentifier(
                name, "id", view.getContext().getPackageName());
    }

    private static final class RoundOutlineProvider extends ViewOutlineProvider {
        private final float mRadius;

        RoundOutlineProvider(float radius) {
            mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
        }
    }
}
