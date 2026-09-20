# ARINA SystemUI Keyguard Visual Architecture

Target: AOSP `android16-release`

## Shipping path

The shipping ARINA Lock Screen is the real AOSP SystemUI/Keyguard surface.

`ArinaKeyguardVisualController` attaches to the existing Keyguard root after
`KeyguardBlueprintViewBinder` is bound. The ARINA layer is presentation-only.

## Clock

The legacy/default AOSP clock views remain structurally present but are made visually
transparent. ARINA draws its own branded time/date layer over the real Keyguard root.

The ARINA clock:
- follows 12/24-hour system preference
- uses system locale for date/time
- uses ARINA ice/steel typography colors
- keeps the AOSP unlock/bouncer pipeline untouched

## Notifications

The real SystemUI `notification_stack_scroller` remains the notification source and
interaction owner. ARINA applies:
- a restrained translucent glass backing
- rounded notification row clipping
- subtle elevation/depth
- no replacement notification database or privacy logic

## Biometric indicator

The indicator consumes only trusted state emitted by `ArinaKeyguardAuthObserver`.
Authentication still happens in Android's biometric framework and
`KeyguardUpdateMonitor`.

Visual states:
- listening
- authenticated
- failed
- primary credential requested
- credential failure
- lockout

## Torch and camera

The existing AOSP Keyguard Quick Affordance framework remains responsible for actions.
ARINA changes the default slots to:
- bottom start: flashlight
- bottom end: camera

Lock-screen shortcut customization remains enabled, so the user can manually replace
either default.

The existing affordance views receive ARINA glass, roundness, size and depth.

## Swipe to bouncer

ARINA draws the swipe-up text and home-indicator-style visual affordance only.
The actual swipe gesture, falsing checks, primary bouncer and credential flow remain
AOSP SystemUI behavior.

## Build-time merge

`scripts/patch-systemui-keyguard-visuals.py` patches
`KeyguardViewConfigurator.kt` only when the expected Android 16 anchor is present.
It refuses to guess against a different SystemUI revision.

`scripts/integrate-arina.sh` applies:
1. authentication observer patch
2. visual merge patch
3. product resource overlays
