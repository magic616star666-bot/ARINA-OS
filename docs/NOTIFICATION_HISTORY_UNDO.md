# ARINA Notification History, Clear All and Undo

Target: AOSP `android16-release`

## Notification history

ARINA does not create a parallel notification archive.

The real AOSP FooterView remains responsible for the History/Manage action. When Android
notification history is enabled, the existing SystemUI listener continues to launch the
platform notification-history screen. ARINA only applies the final glass/pill visual treatment
to the real `manage_text` and `dismiss_text` controls.

This means:
- history enablement still follows Android secure settings;
- history data access remains protected by Android's notification permissions;
- ARINA does not read private historical notification content itself;
- existing content descriptions and click listeners remain AOSP-owned.

## Clear all

AOSP still decides which notifications are clearable and builds the real clear-all row list.

ARINA changes the final interaction:
1. AOSP performs its existing clear-all eligibility filtering and animations.
2. The backend clear callback is staged for a five-second Undo window.
3. The notification shade stays open while Undo is available.
4. If the timer expires, the original AOSP clear-all callback runs unchanged.
5. If Undo is pressed, the rows are restored and the backend clear callback is never executed.

Non-clearable notifications remain untouched because ARINA receives only AOSP's already-filtered
clear-all row list.

## Swipe/accessibility dismiss Undo

The real `ExpandableNotificationRow` dismiss entry point is staged before AOSP sends the final
dismiss callback.

- swipe dismiss enters the same AOSP row dismiss method as before;
- accessibility dismiss enters the same method and gets the same Undo window;
- app/system cancellation is not delayed;
- after five seconds, ARINA re-enters the original AOSP dismiss method and lets Android finish;
- Undo restores the existing row without reposting or reconstructing notification content.

A commit guard prevents nested/group dismiss calls from opening additional Undo windows while the
original AOSP dismissal is being finalized.

## Undo surface

The ARINA Undo bar is attached to the real SystemUI notification-shade window.

It:
- uses generic text on the lock/shade surface to avoid exposing private notification content;
- has a dedicated focusable/clickable Undo control;
- announces dismissal/restoration through Android accessibility;
- uses original ARINA navy/ice/cyan styling;
- does not replace notification-row accessibility delegates.

## Build integration

`scripts/patch-systemui-notification-center.py` patches:
- `ExpandableNotificationRow.java`
- `NotificationStackScrollLayout.java`
- `NotificationStackScrollLayoutController.java`

The patcher is fail-closed and refuses to modify an unexpected SystemUI revision.

`tests/test_patch_systemui_notification_center.py` checks patch idempotence.

`tests/test_notification_center_security.py` ensures the ARINA controller does not directly
launch history, cancel notifications, send PendingIntents or bypass Keyguard.
