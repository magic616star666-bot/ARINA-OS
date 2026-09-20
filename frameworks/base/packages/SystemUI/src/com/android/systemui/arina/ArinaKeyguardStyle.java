package com.android.systemui.arina;

import android.graphics.Color;

/**
 * Shared ARINA Keyguard visual constants.
 *
 * This class intentionally contains no authentication or unlock logic.
 * SystemUI/Keyguard remains the authority for credentials, biometrics,
 * emergency calling, doze and notification security.
 */
public final class ArinaKeyguardStyle {

    public static final int COLOR_BACKGROUND = Color.rgb(5, 11, 22);
    public static final int COLOR_ICE = Color.rgb(234, 247, 255);
    public static final int COLOR_STEEL = Color.rgb(169, 193, 216);
    public static final int COLOR_BLUE = Color.rgb(74, 125, 255);
    public static final int COLOR_CYAN = Color.rgb(53, 198, 255);

    public static final int PRESS_DURATION_MS = 90;
    public static final int REVEAL_DURATION_MS = 220;
    public static final int SHEET_DURATION_MS = 300;
    public static final int SCREEN_TRANSITION_MS = 380;

    public static final float PRESSED_SCALE = 0.975f;
    public static final float ACTION_PRESSED_SCALE = 0.94f;

    private ArinaKeyguardStyle() {}
}
