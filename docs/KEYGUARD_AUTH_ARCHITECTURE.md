# ARINA Keyguard Authentication Architecture

Target base: **AOSP android16-release**

## Security model

ARINA does not implement its own PIN checker or biometric authenticator.

The shipping lock screen stays inside AOSP SystemUI/Keyguard:

- PIN/password/pattern verification: AOSP Keyguard security controllers
- credential policy / lockout: LockPatternUtils + DevicePolicyManager
- fingerprint / face: Android biometric HAL/framework + KeyguardUpdateMonitor
- primary bouncer: PrimaryBouncerInteractor
- final keyguard dismissal: KeyguardSecurityContainerController / KeyguardViewMediator
- emergency-call behavior: stock SystemUI/Keyguard path

This preserves Android's real authentication boundary.

## ARINA wiring

`ArinaKeyguardAuthObserver` is injected into
`KeyguardSecurityContainerController` by
`scripts/patch-systemui-keyguard.py`.

It observes:

- primary security mode shown
- real credential success/failure/lockout after AOSP validation
- biometric listening state
- real biometric success/failure
- strong-auth policy changes

It does **not**:

- compare or store PINs
- bypass strong-auth requirements
- dismiss Keyguard on its own
- change biometric strength policy
- disable lockout
- replace emergency-call flows

## Swipe / bouncer behavior

The ARINA lock-screen presentation must use the normal SystemUI lockscreen gesture path.
When the user requests device entry, AOSP shows the primary bouncer and selects the
real configured security mode. The ARINA visual layer decorates this experience rather
than creating a second authentication surface.

## Styling

ARINA uses product resource overlays for SystemUI Keyguard geometry and color.
This preserves the underlying AOSP controllers while applying ARINA's final
iOS-inspired visual language.

## Preview package

`packages/apps/ArinaLockScreen` is retained only as an isolated visual preview/reference.
It is not shipped as the lock authority and is not listed in `PRODUCT_PACKAGES`.

## Build verification

The reference AOSP build should compile:

`m SystemUI ArinaHome ArinaLockScreen`

The standalone preview package is built only to catch visual-layer compile regressions.
The shipping secure lock screen is SystemUI.
