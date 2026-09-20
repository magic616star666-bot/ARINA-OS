package com.android.systemui.arina;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import java.lang.ref.WeakReference;

/**
 * Applies ARINA presentation to the real AOSP SystemUI Keyguard tree.
 *
 * The controller is visual-only. Existing AOSP controllers keep all touch,
 * authentication, notification, camera, flashlight and bouncer behavior.
 */
public final class ArinaKeyguardVisualController {

    private static WeakReference<ArinaKeyguardOverlayView> sOverlay =
            new WeakReference<>(null);

    private ArinaKeyguardVisualController() {}

    public static void attach(ViewGroup keyguardRoot, ViewGroup shadeWindow) {
        if (keyguardRoot == null || shadeWindow == null) return;

        ArinaKeyguardOverlayView overlay = findOverlay(keyguardRoot);
        if (overlay == null) {
            overlay = new ArinaKeyguardOverlayView(keyguardRoot.getContext());
            keyguardRoot.addView(
                    overlay,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }

        sOverlay = new WeakReference<>(overlay);
        ArinaKeyguardAuthObserver.setUiListener(overlay::setBiometricState);

        final ArinaKeyguardOverlayView finalOverlay = overlay;
        final Runnable apply = () -> {
            hideNativeClock(keyguardRoot);
            styleQuickAffordances(keyguardRoot);
            styleQuickAffordances(shadeWindow);
            styleNotifications(shadeWindow);
            finalOverlay.bringToFront();
        };

        apply.run();
        keyguardRoot.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> apply.run());
        shadeWindow.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> apply.run());
    }

    private static ArinaKeyguardOverlayView findOverlay(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ArinaKeyguardOverlayView) {
                return (ArinaKeyguardOverlayView) child;
            }
        }
        return null;
    }

    private static void hideNativeClock(View root) {
        setInvisibleButLaidOut(root, "keyguard_clock_container");
        setInvisibleButLaidOut(root, "lockscreen_clock_view");
        setInvisibleButLaidOut(root, "lockscreen_clock_view_large");
    }

    private static void setInvisibleButLaidOut(View root, String idName) {
        int id = id(root, idName);
        if (id == 0) return;
        View view = root.findViewById(id);
        if (view != null && !(view instanceof ArinaKeyguardOverlayView)) {
            view.setAlpha(0f);
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
    }

    private static void styleQuickAffordances(View root) {
        styleAffordance(root, "start_button");
        styleAffordance(root, "end_button");
        styleAffordance(root, "bottom_start");
        styleAffordance(root, "bottom_end");
    }

    private static void styleAffordance(View root, String name) {
        int id = id(root, name);
        if (id == 0) return;

        View button = root.findViewById(id);
        if (button == null) return;

        float density = button.getResources().getDisplayMetrics().density;
        button.setElevation(10f * density);
        button.setOutlineProvider(new RoundOutlineProvider(30f * density));
        button.setClipToOutline(true);
    }

    private static void styleNotifications(View root) {
        int stackId = id(root, "notification_stack_scroller");
        if (stackId == 0) return;

        View stackView = root.findViewById(stackId);
        if (!(stackView instanceof ViewGroup)) return;

        ViewGroup stack = (ViewGroup) stackView;
        ArinaKeyguardNotificationStyler.styleStack(stack);
    }

    private static int id(View view, String name) {
        return view.getResources().getIdentifier(
                name, "id", view.getContext().getPackageName());
    }

    private static final class RoundOutlineProvider extends ViewOutlineProvider {
        private final float radius;

        RoundOutlineProvider(float radius) {
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
        }
    }
}
