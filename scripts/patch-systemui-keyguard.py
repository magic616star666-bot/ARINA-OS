#!/usr/bin/env python3
"""
Wire ARINA presentation state into Android 16 SystemUI Keyguard without replacing
AOSP credential or biometric authentication.

Target:
  frameworks/base @ refs/heads/android16-release

This patch is deliberately narrow:
- observes real PIN/password/pattern results after AOSP validates them;
- observes real biometric callbacks from KeyguardUpdateMonitor;
- preserves AOSP KeyguardViewMediator / PrimaryBouncerInteractor dismissal paths;
- does not add a credential checker, bypass, or custom biometric authenticator.
"""

from pathlib import Path
import os
import sys

aosp = Path(os.environ.get("AOSP_DIR", Path.home() / "aosp"))
path = aosp / "frameworks/base/packages/SystemUI/src/com/android/keyguard/KeyguardSecurityContainerController.java"

if not path.exists():
    raise SystemExit(f"Missing Android 16 SystemUI source: {path}")

src = path.read_text()

marker = "ARINA_KEYGUARD_AUTH_WIRING"
if marker in src:
    print("ARINA Keyguard auth wiring already applied")
    sys.exit(0)

field_anchor = "private final KeyguardUpdateMonitor mUpdateMonitor;"
field_insert = """private final KeyguardUpdateMonitor mUpdateMonitor;
    // ARINA_KEYGUARD_AUTH_WIRING: mirrors AOSP-authenticated state for ARINA UI only.
    private final com.android.systemui.arina.ArinaKeyguardAuthObserver mArinaAuthObserver;"""
if field_anchor not in src:
    raise SystemExit("Android 16 field anchor not found")
src = src.replace(field_anchor, field_insert, 1)

ctor_anchor = "mUpdateMonitor = keyguardUpdateMonitor;"
ctor_insert = """mUpdateMonitor = keyguardUpdateMonitor;
        mArinaAuthObserver =
                new com.android.systemui.arina.ArinaKeyguardAuthObserver(keyguardUpdateMonitor);"""
if ctor_anchor not in src:
    raise SystemExit("Android 16 constructor anchor not found")
src = src.replace(ctor_anchor, ctor_insert, 1)

attach_anchor = "mUpdateMonitor.registerCallback(mKeyguardUpdateMonitorCallback);"
attach_insert = """mUpdateMonitor.registerCallback(mKeyguardUpdateMonitorCallback);
        mArinaAuthObserver.attach();"""
if attach_anchor not in src:
    raise SystemExit("Android 16 onViewAttached anchor not found")
src = src.replace(attach_anchor, attach_insert, 1)

detach_anchor = "mUpdateMonitor.removeCallback(mKeyguardUpdateMonitorCallback);"
detach_insert = """mUpdateMonitor.removeCallback(mKeyguardUpdateMonitorCallback);
        mArinaAuthObserver.detach();"""
if detach_anchor not in src:
    raise SystemExit("Android 16 onViewDetached anchor not found")
src = src.replace(detach_anchor, detach_insert, 1)

credential_anchor = """public void reportUnlockAttempt(int userId, boolean success, int timeoutMs) {
"""
credential_insert = """public void reportUnlockAttempt(int userId, boolean success, int timeoutMs) {
            mArinaAuthObserver.onCredentialAttempt(success, timeoutMs);
"""
if credential_anchor not in src:
    raise SystemExit("Android 16 credential result anchor not found")
src = src.replace(credential_anchor, credential_insert, 1)

screen_anchor = """mPrimaryBouncerInteractor.get().setLastShownPrimarySecurityScreen(securityMode);
        showSecurityScreen(securityMode);"""
screen_insert = """mPrimaryBouncerInteractor.get().setLastShownPrimarySecurityScreen(securityMode);
        mArinaAuthObserver.onPrimarySecurityScreen(securityMode.name());
        showSecurityScreen(securityMode);"""
if screen_anchor not in src:
    raise SystemExit("Android 16 primary security anchor not found")
src = src.replace(screen_anchor, screen_insert, 1)

path.write_text(src)
print(f"Patched real AOSP Keyguard auth observation: {path}")
