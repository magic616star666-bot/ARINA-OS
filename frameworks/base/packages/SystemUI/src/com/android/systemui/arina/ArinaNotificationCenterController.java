package com.android.systemui.arina;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * ARINA presentation/undo controller for the real AOSP notification center.
 *
 * Security boundary:
 * - notification history continues to launch through AOSP's existing FooterView listener;
 * - clearability is still decided by AOSP before this class sees rows;
 * - accessibility dismiss still enters AOSP's existing dismiss path;
 * - ARINA delays the final user-dismiss callback briefly so an explicit Undo can cancel it;
 * - app/system cancellation is never intercepted here.
 */
public final class ArinaNotificationCenterController {

    private static final long UNDO_WINDOW_MS = 5000L;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final WeakHashMap<ExpandableNotificationRow, PendingDismiss> sPendingRows =
            new WeakHashMap<>();

    private static FrameLayout sHost;
    private static LinearLayout sUndoBar;
    private static TextView sUndoMessage;
    private static TextView sUndoButton;
    private static Runnable sPendingClearAllCommit;
    private static List<ExpandableNotificationRow> sPendingClearAllRows;
    private static boolean sCommitting;

    private ArinaNotificationCenterController() {}

    public static boolean isUndoEnabled() {
        return true;
    }

    public static void attachHost(ViewGroup host) {
        if (!(host instanceof FrameLayout)) return;

        FrameLayout frame = (FrameLayout) host;
        if (sHost == frame && sUndoBar != null) return;

        detachUndoBar();
        sHost = frame;
        ensureUndoBar();
    }

    public static void styleFooter(View footer) {
        if (footer == null) return;

        View manage = findById(footer, "manage_text");
        View clear = findById(footer, "dismiss_text");

        styleFooterButton(manage, false);
        styleFooterButton(clear, true);
    }

    /**
     * Called at the very start of the row's existing AOSP user-dismiss method.
     *
     * @return true when ARINA staged the dismissal and the caller should return without invoking
     *         the original AOSP dismissal yet.
     */
    public static boolean stageRowDismiss(
            ExpandableNotificationRow row,
            boolean fromAccessibility,
            Runnable commitOriginalDismiss) {
        if (row == null || commitOriginalDismiss == null || sCommitting) {
            return false;
        }

        // AOSP already checks clearability in its own row/controller path. This secondary check
        // avoids presenting Undo for a row that cannot be cleared.
        if (!row.isClearable()) {
            return false;
        }

        PendingDismiss old = sPendingRows.remove(row);
        if (old != null) {
            sMainHandler.removeCallbacks(old.timeout);
        }

        PendingDismiss pending = new PendingDismiss(row, fromAccessibility, commitOriginalDismiss);
        sPendingRows.put(row, pending);

        pending.timeout = () -> commitRowDismiss(row);
        sMainHandler.postDelayed(pending.timeout, UNDO_WINDOW_MS);

        row.setAlpha(0f);
        row.setVisibility(View.INVISIBLE);

        showUndo(
                text(row, "arina_notification_dismissed", "Notification dismissed"),
                () -> undoRowDismiss(row));

        if (fromAccessibility) {
            announce(row, text(row, "arina_notification_dismissed_undo_available",
                    "Notification dismissed. Undo available."));
        }

        return true;
    }

    public static boolean isRowDismissPending(ExpandableNotificationRow row) {
        return row != null && sPendingRows.containsKey(row);
    }

    /**
     * Stages backend clear-all after AOSP's own clear-all eligibility filtering and animation.
     *
     * @return true when the original backend clear callback should not run yet.
     */
    public static boolean stageClearAll(
            View host,
            List<ExpandableNotificationRow> rows,
            Runnable commitBackendClear) {
        if (commitBackendClear == null || rows == null || rows.isEmpty() || sCommitting) {
            return false;
        }

        cancelPendingClearAll(false);

        sPendingClearAllRows = new ArrayList<>(rows);
        sPendingClearAllCommit = commitBackendClear;

        for (ExpandableNotificationRow row : sPendingClearAllRows) {
            if (row == null) continue;
            row.setAlpha(0f);
            row.setVisibility(View.INVISIBLE);
        }

        Runnable timeout = ArinaNotificationCenterController::commitClearAll;
        sClearAllTimeout = timeout;
        sMainHandler.postDelayed(timeout, UNDO_WINDOW_MS);

        showUndo(
                text(host, "arina_notifications_cleared", "Notifications cleared"),
                ArinaNotificationCenterController::undoClearAll);

        announce(host, text(host, "arina_notifications_cleared_undo_available",
                "Notifications cleared. Undo available."));
        return true;
    }

    private static Runnable sClearAllTimeout;

    private static void undoRowDismiss(ExpandableNotificationRow row) {
        PendingDismiss pending = sPendingRows.remove(row);
        if (pending == null) return;

        sMainHandler.removeCallbacks(pending.timeout);
        restoreRow(row);
        hideUndo();

        announce(row, text(row, "arina_notification_restored", "Notification restored"));
    }

    private static void commitRowDismiss(ExpandableNotificationRow row) {
        PendingDismiss pending = sPendingRows.remove(row);
        if (pending == null) return;

        hideUndo();

        sCommitting = true;
        try {
            pending.commit.run();
            row.removeFromTransientContainer();
            row.removeChildrenWithKeepInParent();
        } finally {
            sCommitting = false;
        }
    }

    private static void undoClearAll() {
        if (sPendingClearAllCommit == null) return;

        if (sClearAllTimeout != null) {
            sMainHandler.removeCallbacks(sClearAllTimeout);
        }

        if (sPendingClearAllRows != null) {
            for (ExpandableNotificationRow row : sPendingClearAllRows) {
                restoreRow(row);
            }
        }

        View announcementHost = sHost;
        sPendingClearAllRows = null;
        sPendingClearAllCommit = null;
        sClearAllTimeout = null;
        hideUndo();

        announce(announcementHost,
                text(announcementHost, "arina_notifications_restored", "Notifications restored"));
    }

    private static void commitClearAll() {
        Runnable commit = sPendingClearAllCommit;
        if (commit == null) return;

        sPendingClearAllRows = null;
        sPendingClearAllCommit = null;
        sClearAllTimeout = null;
        hideUndo();

        sCommitting = true;
        try {
            commit.run();
        } finally {
            sCommitting = false;
        }
    }

    private static void cancelPendingClearAll(boolean restore) {
        if (sClearAllTimeout != null) {
            sMainHandler.removeCallbacks(sClearAllTimeout);
        }

        if (restore && sPendingClearAllRows != null) {
            for (ExpandableNotificationRow row : sPendingClearAllRows) {
                restoreRow(row);
            }
        }

        sPendingClearAllRows = null;
        sPendingClearAllCommit = null;
        sClearAllTimeout = null;
    }

    private static void restoreRow(ExpandableNotificationRow row) {
        if (row == null) return;

        row.animate().cancel();
        row.setVisibility(View.VISIBLE);
        row.setTranslationX(0f);
        row.setTranslationY(0f);
        row.setAlpha(0f);
        row.animate()
                .alpha(1f)
                .setDuration(ArinaKeyguardStyle.REVEAL_DURATION_MS)
                .start();
        row.requestLayout();
        row.invalidate();
    }

    private static void ensureUndoBar() {
        if (sHost == null || sUndoBar != null) return;

        final float density = sHost.getResources().getDisplayMetrics().density;

        LinearLayout bar = new LinearLayout(sHost.getContext());
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(
                Math.round(18f * density),
                0,
                Math.round(8f * density),
                0);
        bar.setVisibility(View.GONE);
        bar.setElevation(18f * density);
        bar.setBackground(glass(
                Color.argb(236, 8, 20, 38),
                Color.argb(88, 53, 198, 255),
                24f,
                density));

        TextView message = new TextView(sHost.getContext());
        message.setTextColor(ArinaKeyguardStyle.COLOR_ICE);
        message.setTextSize(14f);
        message.setGravity(Gravity.CENTER_VERTICAL);
        message.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        TextView undo = new TextView(sHost.getContext());
        undo.setText(text(sHost, "arina_notification_undo", "Undo"));
        undo.setTextColor(ArinaKeyguardStyle.COLOR_CYAN);
        undo.setTextSize(14f);
        undo.setGravity(Gravity.CENTER);
        undo.setContentDescription(text(sHost, "arina_notification_undo", "Undo"));
        undo.setFocusable(true);
        undo.setClickable(true);
        undo.setMinWidth(Math.round(68f * density));
        undo.setMinHeight(Math.round(48f * density));
        undo.setBackground(glass(
                Color.argb(60, 53, 198, 255),
                Color.argb(90, 53, 198, 255),
                20f,
                density));

        bar.addView(message, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        bar.addView(undo, new LinearLayout.LayoutParams(
                Math.round(76f * density),
                ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Math.round(58f * density),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.setMargins(
                Math.round(18f * density),
                0,
                Math.round(18f * density),
                Math.round(34f * density));

        sHost.addView(bar, params);
        sUndoBar = bar;
        sUndoMessage = message;
        sUndoButton = undo;
    }

    private static void showUndo(String message, Runnable action) {
        ensureUndoBar();
        if (sUndoBar == null) return;

        sUndoMessage.setText(message);
        sUndoButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            action.run();
        });

        sUndoBar.animate().cancel();
        sUndoBar.setVisibility(View.VISIBLE);
        sUndoBar.setAlpha(0f);
        sUndoBar.setTranslationY(18f * sUndoBar.getResources().getDisplayMetrics().density);
        sUndoBar.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ArinaKeyguardStyle.REVEAL_DURATION_MS)
                .start();
    }

    private static void hideUndo() {
        if (sUndoBar == null || sUndoBar.getVisibility() != View.VISIBLE) return;

        sUndoBar.animate().cancel();
        sUndoBar.animate()
                .alpha(0f)
                .translationY(12f * sUndoBar.getResources().getDisplayMetrics().density)
                .setDuration(160L)
                .withEndAction(() -> {
                    if (sUndoBar != null) {
                        sUndoBar.setVisibility(View.GONE);
                        sUndoBar.setTranslationY(0f);
                    }
                })
                .start();
    }

    private static void detachUndoBar() {
        if (sUndoBar != null && sUndoBar.getParent() instanceof ViewGroup) {
            ((ViewGroup) sUndoBar.getParent()).removeView(sUndoBar);
        }
        sUndoBar = null;
        sUndoMessage = null;
        sUndoButton = null;
        sHost = null;
    }

    private static void styleFooterButton(View view, boolean clearAll) {
        if (!(view instanceof TextView)) return;

        TextView button = (TextView) view;
        final float density = button.getResources().getDisplayMetrics().density;
        button.setTextColor(clearAll ? Color.rgb(255, 130, 148) : ArinaKeyguardStyle.COLOR_ICE);
        button.setMinHeight(Math.round(44f * density));
        button.setPadding(
                Math.round(16f * density),
                0,
                Math.round(16f * density),
                0);
        button.setBackground(glass(
                clearAll ? Color.argb(76, 255, 111, 134) : Color.argb(94, 30, 60, 98),
                clearAll ? Color.argb(105, 255, 111, 134) : Color.argb(70, 53, 198, 255),
                20f,
                density));
        button.setElevation(4f * density);
        button.setOutlineProvider(new RoundOutlineProvider(20f * density));
        button.setClipToOutline(true);
        // Existing click listeners, content descriptions and accessibility actions stay untouched.
    }

    private static View findById(View root, String name) {
        int id = root.getResources().getIdentifier(
                name, "id", root.getContext().getPackageName());
        return id == 0 ? null : root.findViewById(id);
    }

    private static String text(View view, String resourceName, String fallback) {
        if (view == null) return fallback;

        int id = view.getResources().getIdentifier(
                resourceName, "string", view.getContext().getPackageName());
        return id == 0 ? fallback : view.getContext().getString(id);
    }

    private static void announce(View view, String text) {
        if (view != null && text != null) {
            view.announceForAccessibility(text);
        }
    }

    private static GradientDrawable glass(
            int fill, int stroke, float radiusDp, float density) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radiusDp * density);
        drawable.setStroke(Math.max(1, Math.round(density)), stroke);
        return drawable;
    }

    private static final class PendingDismiss {
        final ExpandableNotificationRow row;
        final boolean fromAccessibility;
        final Runnable commit;
        Runnable timeout;

        PendingDismiss(
                ExpandableNotificationRow row,
                boolean fromAccessibility,
                Runnable commit) {
            this.row = row;
            this.fromAccessibility = fromAccessibility;
            this.commit = commit;
        }
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
