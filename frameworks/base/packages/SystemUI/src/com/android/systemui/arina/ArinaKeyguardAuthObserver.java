package com.android.systemui.arina;

import android.hardware.biometrics.BiometricSourceType;
import android.util.Log;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;

/**
 * ARINA presentation observer for AOSP Keyguard authentication.
 *
 * Security boundary:
 * - PIN/password/pattern verification remains inside AOSP Keyguard controllers.
 * - Biometric authentication remains inside KeyguardUpdateMonitor and the platform biometric stack.
 * - This class never validates credentials and never calls keyguardDone().
 *
 * It only mirrors trusted AOSP authentication state for ARINA visuals/haptics.
 */
public final class ArinaKeyguardAuthObserver {

    private static final String TAG = "ARINA-KeyguardAuth";

    public enum State {
        IDLE,
        PRIMARY_CREDENTIAL,
        BIOMETRIC_LISTENING,
        BIOMETRIC_AUTHENTICATED,
        BIOMETRIC_FAILED,
        CREDENTIAL_AUTHENTICATED,
        CREDENTIAL_FAILED,
        LOCKED_OUT
    }

    private final KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mAttached;
    private volatile State mState = State.IDLE;
    private volatile String mSecurityMode = "Unknown";

    private final KeyguardUpdateMonitorCallback mCallback =
            new KeyguardUpdateMonitorCallback() {
                @Override
                public void onBiometricRunningStateChanged(
                        boolean running, BiometricSourceType biometricSourceType) {
                    if (running) {
                        setState(State.BIOMETRIC_LISTENING,
                                "biometric listening: " + biometricSourceType);
                    } else if (mState == State.BIOMETRIC_LISTENING) {
                        setState(State.IDLE, "biometric listening stopped");
                    }
                }

                @Override
                public void onBiometricAuthenticated(
                        int userId,
                        BiometricSourceType biometricSourceType,
                        boolean isStrongBiometric) {
                    setState(
                            State.BIOMETRIC_AUTHENTICATED,
                            "biometric authenticated: "
                                    + biometricSourceType
                                    + ", strong=" + isStrongBiometric);
                }

                @Override
                public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
                    setState(State.BIOMETRIC_FAILED,
                            "biometric failed: " + biometricSourceType);
                }

                @Override
                public void onStrongAuthStateChanged(int userId) {
                    Log.d(TAG, "strong-auth policy changed for user=" + userId);
                }
            };

    public ArinaKeyguardAuthObserver(KeyguardUpdateMonitor updateMonitor) {
        mUpdateMonitor = updateMonitor;
    }

    public void attach() {
        if (mAttached) return;
        mAttached = true;
        mUpdateMonitor.registerCallback(mCallback);
        Log.i(TAG, "attached to AOSP KeyguardUpdateMonitor");
    }

    public void detach() {
        if (!mAttached) return;
        mAttached = false;
        mUpdateMonitor.removeCallback(mCallback);
        Log.i(TAG, "detached");
    }

    public void onPrimarySecurityScreen(String securityMode) {
        mSecurityMode = securityMode == null ? "Unknown" : securityMode;
        setState(State.PRIMARY_CREDENTIAL, "primary security=" + mSecurityMode);
    }

    public void onCredentialAttempt(boolean success, int timeoutMs) {
        if (success) {
            setState(State.CREDENTIAL_AUTHENTICATED,
                    "credential authenticated: " + mSecurityMode);
        } else if (timeoutMs > 0) {
            setState(State.LOCKED_OUT,
                    "credential lockout: " + timeoutMs + "ms");
        } else {
            setState(State.CREDENTIAL_FAILED,
                    "credential failed: " + mSecurityMode);
        }
    }

    public State getState() {
        return mState;
    }

    public String getSecurityMode() {
        return mSecurityMode;
    }

    private void setState(State state, String reason) {
        mState = state;
        Log.d(TAG, state + " · " + reason);
    }
}
