package com.android.systemui.arina;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.WeakHashMap;

/**
 * ARINA visual treatment for the existing AOSP notification interaction surfaces.
 *
 * This class never installs click/touch/accessibility listeners and never sends a
 * PendingIntent itself. AOSP remains the owner of:
 * - notification action execution
 * - RemoteInput / quick-reply security and unlock gating
 * - smart replies
 * - swipe menus / snooze / notification settings
 * - accessibility actions and content descriptions
 */
public final class ArinaNotificationInteractionStyler {

    private static final int ICE = ArinaKeyguardStyle.COLOR_ICE;
    private static final int STEEL = ArinaKeyguardStyle.COLOR_STEEL;
    private static final int CYAN = ArinaKeyguardStyle.COLOR_CYAN;

    private static final WeakHashMap<View, Boolean> sObservedRows = new WeakHashMap<>();

    private ArinaNotificationInteractionStyler() {}

    public static void style(View row) {
        if (row == null) return;

        observeRow(row);
        styleInlineActions(row);
        styleQuickReplies(row);
        styleSwipeMenu(row);
    }

    private static void observeRow(View row) {
        if (sObservedRows.containsKey(row)) return;
        sObservedRows.put(row, Boolean.TRUE);

        // RemoteInput and smart-reply views can be inflated after the row itself.
        // Re-style after layout without replacing any AOSP interaction listener.
        row.addOnLayoutChangeListener(
                (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    styleInlineActions(view);
                    styleQuickReplies(view);
                    styleSwipeMenu(view);
                });
    }

    private static void styleInlineActions(View row) {
        View actions = findByIdNames(
                row,
                new String[] {
                    "actions",
                    "actions_container",
                    "notification_action_list",
                    "smart_reply_view"
                });

        if (actions instanceof ViewGroup) {
            styleActionContainer((ViewGroup) actions);
        }

        // Smart replies may live outside the normal actions container.
        visit(row, view -> {
            String className = view.getClass().getName();
            if (className.contains("SmartReplyView")) {
                if (view instanceof ViewGroup) {
                    styleActionContainer((ViewGroup) view);
                }
            }
        });
    }

    private static void styleActionContainer(ViewGroup container) {
        final float density = container.getResources().getDisplayMetrics().density;

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);

            if (child instanceof TextView && child.isClickable()) {
                TextView action = (TextView) child;
                action.setTextColor(ICE);
                action.setMinHeight(Math.round(40f * density));
                action.setPadding(
                        Math.round(14f * density),
                        Math.round(7f * density),
                        Math.round(14f * density),
                        Math.round(7f * density));
                action.setBackground(
                        pressableGlass(
                                action.getContext(),
                                Color.argb(118, 30, 60, 98),
                                Color.argb(188, 53, 198, 255),
                                18f));
                action.setElevation(3f * density);
                action.setOutlineProvider(new RoundOutlineProvider(18f * density));
            }

            if (child instanceof ViewGroup) {
                styleActionContainer((ViewGroup) child);
            }
        }
    }

    private static void styleQuickReplies(View row) {
        visit(row, view -> {
            String className = view.getClass().getName();

            if (className.endsWith(".RemoteInputView")) {
                styleRemoteInputHost(view);
            } else if (view instanceof EditText
                    && hasAncestorClass(view, "RemoteInputView")) {
                styleRemoteEditText((EditText) view);
            } else if (view instanceof ImageButton
                    && hasAncestorClass(view, "RemoteInputView")) {
                styleRemoteSendButton((ImageButton) view);
            }
        });
    }

    private static void styleRemoteInputHost(View host) {
        final float density = host.getResources().getDisplayMetrics().density;
        host.setBackground(
                roundedGlass(
                        Color.argb(176, 11, 26, 48),
                        Color.argb(82, 53, 198, 255),
                        22f,
                        density));
        host.setElevation(6f * density);
        host.setOutlineProvider(new RoundOutlineProvider(22f * density));
        host.setClipToOutline(false);
    }

    private static void styleRemoteEditText(EditText editText) {
        final float density = editText.getResources().getDisplayMetrics().density;

        editText.setTextColor(ICE);
        editText.setHintTextColor(Color.argb(200, 169, 193, 216));
        editText.setBackground(
                pressableGlass(
                        editText.getContext(),
                        Color.argb(86, 234, 247, 255),
                        Color.argb(122, 53, 198, 255),
                        18f));
        editText.setPadding(
                Math.round(15f * density),
                editText.getPaddingTop(),
                Math.round(15f * density),
                editText.getPaddingBottom());
        editText.setMinHeight(Math.round(44f * density));

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            editText.setBackgroundTintList((ColorStateList) null);
        }
    }

    private static void styleRemoteSendButton(ImageButton button) {
        final float density = button.getResources().getDisplayMetrics().density;

        button.setBackground(
                pressableGlass(
                        button.getContext(),
                        Color.argb(210, 74, 125, 255),
                        Color.argb(230, 53, 198, 255),
                        22f));
        button.setColorFilter(ICE);
        button.setElevation(4f * density);
        button.setOutlineProvider(new RoundOutlineProvider(22f * density));
        button.setClipToOutline(true);
        button.setMinimumWidth(Math.round(44f * density));
        button.setMinimumHeight(Math.round(44f * density));
    }

    private static void styleSwipeMenu(View row) {
        Object provider = invoke(row, "getProvider");
        if (provider == null) return;

        Object menuViewObject = invoke(provider, "getMenuView");
        if (!(menuViewObject instanceof View)) return;

        View menuView = (View) menuViewObject;
        final float density = menuView.getResources().getDisplayMetrics().density;

        menuView.setElevation(2f * density);
        menuView.setBackground(
                roundedGlass(
                        Color.argb(124, 8, 20, 38),
                        Color.argb(50, 216, 240, 255),
                        24f,
                        density));
        menuView.setOutlineProvider(new RoundOutlineProvider(24f * density));
        menuView.setClipToOutline(false);

        if (menuView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) menuView;
            for (int i = 0; i < group.getChildCount(); i++) {
                View item = group.getChildAt(i);
                item.setBackground(
                        pressableGlass(
                                item.getContext(),
                                Color.argb(142, 23, 48, 82),
                                Color.argb(210, 53, 198, 255),
                                22f));
                item.setElevation(3f * density);
                item.setOutlineProvider(new RoundOutlineProvider(22f * density));
                item.setClipToOutline(true);
                // Do not change contentDescription/onClick: AOSP menu semantics stay intact.
            }
        }
    }

    private static View findByIdNames(View root, String[] names) {
        for (String name : names) {
            int id = id(root, name, root.getContext().getPackageName());
            if (id == 0) {
                id = id(root, name, "android");
            }
            if (id != 0) {
                View found = root.findViewById(id);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static int id(View view, String name, String pkg) {
        return view.getResources().getIdentifier(name, "id", pkg);
    }

    private static boolean hasAncestorClass(View view, String simpleName) {
        android.view.ViewParent parent = view.getParent();
        while (parent instanceof View) {
            View parentView = (View) parent;
            if (parentView.getClass().getName().endsWith("." + simpleName)) {
                return true;
            }
            parent = parentView.getParent();
        }
        return false;
    }

    private interface Visitor {
        void visit(View view);
    }

    private static void visit(View root, Visitor visitor) {
        visitor.visit(root);
        if (!(root instanceof ViewGroup)) return;

        ViewGroup group = (ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            visit(group.getChildAt(i), visitor);
        }
    }

    private static Object invoke(Object target, String methodName) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return null;
        }
    }

    private static StateListDrawable pressableGlass(
            Context context, int normalColor, int pressedColor, float radiusDp) {
        final float density = context.getResources().getDisplayMetrics().density;

        StateListDrawable states = new StateListDrawable();
        states.addState(
                new int[] {android.R.attr.state_pressed},
                roundedGlass(
                        pressedColor,
                        Color.argb(100, 234, 247, 255),
                        radiusDp,
                        density));
        states.addState(
                new int[0],
                roundedGlass(
                        normalColor,
                        Color.argb(52, 216, 240, 255),
                        radiusDp,
                        density));
        return states;
    }

    private static GradientDrawable roundedGlass(
            int fillColor, int strokeColor, float radiusDp, float density) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusDp * density);
        drawable.setStroke(Math.max(1, Math.round(density)), strokeColor);
        return drawable;
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
