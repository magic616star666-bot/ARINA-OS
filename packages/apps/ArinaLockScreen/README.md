# ARINA Lock Screen Foundation

This package is the visual and interaction foundation for the final ARINA Keyguard.

Implemented now:
- original ARINA lock wallpaper renderer
- real clock/date
- ARINA brand capsule
- calm notification surface
- bottom quick-action controls
- secure-camera launch path
- swipe-up motion and haptic behavior
- edge-to-edge system presentation

Important architecture note:
The shipping lock screen belongs inside AOSP SystemUI/Keyguard. This package exists so the final ARINA lock-screen design can compile and be tested independently on the reference target before its views/controllers are wired into SystemUI.

The preview action is:

`com.arina.intent.action.PREVIEW_LOCK_SCREEN`

The final SystemUI stage will:
- own authentication/unlock authority
- bind real notifications
- bind torch state
- bind privacy indicators
- connect biometric state
- connect doze/AOD
- preserve emergency-call behavior
