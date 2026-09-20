# ARINA OS — Final Product Specification

Status: **FINAL PRODUCT DIRECTION**

## Product statement
ARINA OS is an AOSP-based mobile operating system with an end-to-end iOS-inspired premium interaction model and original ARINA branding.

## Non-negotiable design rules
- iOS-inspired layout discipline, spacing, blur, glass, rounded geometry and gesture continuity.
- Original ARINA logo, iconography, wallpapers, sounds and haptics.
- No Apple logo, proprietary Apple artwork, bundled Apple fonts, private frameworks or copied source.
- One consistent visual language from boot to Lock Screen, Home, notifications, Control Center, Settings, Phone and Conference Hub.
- Motion must be smooth, direct-manipulation based and spring-settled.
- Controls must remain readable and touch friendly.

## Core system surfaces
1. Boot / setup
2. Lock Screen
3. Home / launcher
4. Notification Center
5. Control Center
6. Status bar
7. Recents / multitasking
8. Settings
9. Phone / Contacts
10. ARINA Conference Hub
11. Messages shell
12. Camera shell
13. Photos shell
14. Files
15. Clock
16. Calculator
17. Search / ARINA intelligence surface
18. Share sheets / permission sheets / system dialogs
19. Volume / brightness / charging / privacy HUDs
20. OTA / recovery / update UX

## Branding
- Product: ARINA OS
- Primary wordmark: ARINA
- Visual signature: calm glass + soft depth + controlled blue/cyan accents
- Default dark base: deep navy
- Default light base: soft neutral white/ice
- ARINA assets remain original.

## Manual-control philosophy
ARINA OS keeps user-facing Android policy choices explicit where AOSP permits it:
- app permissions
- background activity
- battery restrictions
- notification access
- default handlers
- install-source access
- privacy controls
- developer controls
- Phone/Conference controls

Security-critical platform guarantees such as sandboxing, verified boot policy, SELinux enforcement and emergency-call requirements are not silently disabled.

## First hardware target
Vivo V30 5G / V2318.

A hardware release is not considered complete until device-specific bootloader, kernel, vendor, RIL/IMS, HAL, SELinux and AVB integration is validated on real hardware.
