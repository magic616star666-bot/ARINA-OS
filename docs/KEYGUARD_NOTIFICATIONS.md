# ARINA Keyguard Notifications

Target: AOSP `android16-release`

## Design goal

ARINA keeps Android's real notification pipeline and applies the final ARINA/iOS-inspired
visual language on top of it.

## Grouping

AOSP remains the source of truth for notification groups.

ARINA reads the real `ExpandableNotificationRow` state and styles:
- group summaries as the strongest card
- expanded summaries with softer depth
- grouped children with tighter radius and reduced visual weight
- transitions when AOSP expands/collapses child groups

ARINA does not create fake groups or reorder notifications outside the AOSP pipeline.

## Privacy redaction

Privacy remains owned by AOSP:
- `NotificationLockscreenUserManager` decides whether private content needs redaction
- `SensitiveContentCoordinator` marks entries sensitive on the lock screen
- the public notification layout remains the redacted content surface

ARINA hooks `NotificationEntry.setSensitive(...)` only after AOSP has calculated the real
privacy state. Sensitive rows receive a restrained dark-glass privacy treatment while the
actual hidden/public content decision remains Android's.

ARINA never reads or reconstructs the private notification text when AOSP has redacted it.

## Expanded interactions

Existing AOSP interactions stay intact:
- tap/expander toggles supported notification expansion
- grouped summary expansion stays in GroupExpansionManager
- swipe actions stay in SwipeHelper / notification row
- long press stays in NotificationGuts
- inline notification actions stay in the RemoteViews/content pipeline
- accessibility expand/collapse/dismiss actions remain AOSP

ARINA adds only:
- short haptic feedback when the real user-expansion path runs
- subtle press/settle motion
- depth/roundness changes when real expansion state changes
- grouped child visual hierarchy

No touch listener is replaced.

## Build integration

`scripts/patch-systemui-notifications.py` wires ARINA into:
- `NotificationEntry.java` for trusted privacy state
- `ExpandableNotificationRow.java` for existing expansion/group state

The patcher fails closed if the expected Android 16 anchors are not present.
