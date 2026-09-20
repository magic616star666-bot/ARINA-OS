#!/usr/bin/env python3
"""
Merge ARINA clock, notification, biometric and unlock-hint visuals into
the real Android 16 AOSP SystemUI Keyguard root.

Target:
  frameworks/base @ refs/heads/android16-release

AOSP keeps ownership of:
- notification data and privacy
- quick-affordance actions (flashlight/camera)
- swipe gesture recognition
- primary bouncer
- PIN/password/pattern verification
- biometric authentication
- emergency calling
"""

from pathlib import Path
import os
import sys

aosp = Path(os.environ.get("AOSP_DIR", Path.home() / "aosp"))
path = aosp / "frameworks/base/packages/SystemUI/src/com/android/systemui/keyguard/KeyguardViewConfigurator.kt"

if not path.exists():
    raise SystemExit(f"Missing Android 16 SystemUI source: {path}")

src = path.read_text()
marker = "ARINA_KEYGUARD_VISUAL_WIRING"

if marker in src:
    print("ARINA Keyguard visual wiring already applied")
    sys.exit(0)

anchor = """        keyguardBlueprintCommandListener.start()
    }
"""

replacement = """        keyguardBlueprintCommandListener.start()

        // ARINA_KEYGUARD_VISUAL_WIRING
        // Presentation only: AOSP Keyguard still owns gestures, bouncer and authentication.
        com.android.systemui.arina.ArinaKeyguardVisualController.attach(
            keyguardRootView,
            notificationShadeWindowView,
        )
    }
"""

if anchor not in src:
    raise SystemExit(
        "Android 16 KeyguardViewConfigurator start() anchor not found; "
        "refuse to guess against a different SystemUI revision."
    )

src = src.replace(anchor, replacement, 1)
path.write_text(src)
print(f"Patched real AOSP Keyguard visuals: {path}")
