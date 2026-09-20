#!/usr/bin/env python3
"""
Wire ARINA notification presentation into the real Android 16 SystemUI notification pipeline.

Security / behavior boundary:
- AOSP still decides grouping, sensitivity, public/private layouts, visibility and actions.
- ARINA receives state only after AOSP has made those decisions.
- Existing swipe, long-press, inline action, accessibility and expansion logic remains intact.
"""

from pathlib import Path
import os
import sys

aosp = Path(os.environ.get("AOSP_DIR", Path.home() / "aosp"))
entry_path = aosp / "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/notification/collection/NotificationEntry.java"
row_path = aosp / "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/notification/row/ExpandableNotificationRow.java"

for path in (entry_path, row_path):
    if not path.exists():
        raise SystemExit(f"Missing Android 16 SystemUI source: {path}")

entry = entry_path.read_text()
row = row_path.read_text()

privacy_marker = "ARINA_NOTIFICATION_PRIVACY_WIRING"
interaction_marker = "ARINA_NOTIFICATION_INTERACTION_WIRING"
group_marker = "ARINA_NOTIFICATION_GROUP_WIRING"

if privacy_marker not in entry:
    setter = "public void setSensitive(boolean sensitive, boolean deviceSensitive) {"
    call = "getRow().setSensitive(sensitive, deviceSensitive);"

    if setter not in entry or call not in entry:
        raise SystemExit(
            "Android 16 NotificationEntry sensitivity anchors not found; "
            "refuse to guess against a different notification pipeline revision."
        )

    replacement = f"""getRow().setSensitive(sensitive, deviceSensitive);
        // {privacy_marker}: AOSP has already computed the real redaction state.
        com.android.systemui.arina.ArinaKeyguardNotificationStyler.onPrivacyStateChanged(
                getRow(), sensitive, deviceSensitive);"""
    entry = entry.replace(call, replacement, 1)
    entry_path.write_text(entry)
    print(f"Patched AOSP notification privacy visual hook: {entry_path}")
else:
    print("ARINA notification privacy wiring already applied")

if interaction_marker not in row:
    signature = "public void setUserExpanded(boolean userExpanded, boolean allowChildExpansion) {"
    falsing = "mFalsingManager.setNotificationExpanded();"

    if signature not in row or falsing not in row:
        raise SystemExit(
            "Android 16 ExpandableNotificationRow expansion anchors not found; "
            "refuse to guess against a different row implementation."
        )

    start = row.index(signature)
    falsing_index = row.index(falsing, start)
    insertion_at = falsing_index + len(falsing)
    row = (
        row[:insertion_at]
        + f"""
        // {interaction_marker}: motion/haptic only; AOSP still owns expansion.
        com.android.systemui.arina.ArinaKeyguardNotificationStyler
                .onUserExpansionRequested(this, userExpanded);"""
        + row[insertion_at:]
    )

if group_marker not in row:
    candidates = [
        "public void setChildrenExpanded(boolean expanded) {",
        "public void setChildrenExpanded(boolean expanded, boolean animate) {",
    ]

    group_signature = next((candidate for candidate in candidates if candidate in row), None)
    if group_signature is None:
        raise SystemExit(
            "Android 16 grouped-notification expansion anchor not found; "
            "refuse to guess against a different row implementation."
        )

    group_start = row.index(group_signature) + len(group_signature)
    row = (
        row[:group_start]
        + f"""
        // {group_marker}: mirror AOSP group state into ARINA depth/motion.
        com.android.systemui.arina.ArinaKeyguardNotificationStyler
                .onGroupExpansionChanged(this, expanded);"""
        + row[group_start:]
    )

row_path.write_text(row)
print(f"Patched AOSP notification interaction/group visual hooks: {row_path}")
