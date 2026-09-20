#!/usr/bin/env python3
"""
Wire ARINA notification history styling, staged clear-all and undoable user dismissals
into the real Android 16 AOSP SystemUI notification surfaces.

Security/accessibility boundary:
- AOSP still decides whether a notification is clearable.
- AOSP FooterView still owns History/Manage click behavior and accessibility labels.
- AOSP notification rows still own accessibility dismiss actions.
- ARINA only delays the final user-dismiss callback for a short Undo window.
- app/system cancellations are not intercepted.
"""

from pathlib import Path
import os
import sys

aosp = Path(os.environ.get("AOSP_DIR", Path.home() / "aosp"))

row_path = aosp / (
    "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
    "notification/row/ExpandableNotificationRow.java"
)
stack_path = aosp / (
    "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
    "notification/stack/NotificationStackScrollLayout.java"
)
controller_path = aosp / (
    "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
    "notification/stack/NotificationStackScrollLayoutController.java"
)

for path in (row_path, stack_path, controller_path):
    if not path.exists():
        raise SystemExit(f"Missing Android 16 SystemUI source: {path}")

row = row_path.read_text()
stack = stack_path.read_text()
controller = controller_path.read_text()

ROW_MARKER = "ARINA_NOTIFICATION_UNDO_ROW_WIRING"
STACK_MARKER = "ARINA_NOTIFICATION_CENTER_STACK_WIRING"
CONTROLLER_MARKER = "ARINA_NOTIFICATION_UNDO_CONTROLLER_WIRING"

if ROW_MARKER not in row:
    candidates = [
        ("public void dismiss(boolean refocusOnDismiss) {", "refocusOnDismiss", "dismiss"),
        ("public void performDismiss(boolean fromAccessibility) {", "fromAccessibility", "performDismiss"),
    ]
    match = next((candidate for candidate in candidates if candidate[0] in row), None)

    if match is None:
        raise SystemExit(
            "Android 16 notification-row dismiss anchor not found; "
            "refuse to guess against a different SystemUI revision."
        )

    signature, argument, method = match
    insertion = f"""{signature}
        // {ROW_MARKER}: stage only a user dismiss; AOSP still performs the final dismissal.
        if (com.android.systemui.arina.ArinaNotificationCenterController.stageRowDismiss(
                this,
                {argument},
                () -> {method}({argument}))) {{
            return;
        }}
"""
    row = row.replace(signature, insertion, 1)
    row_path.write_text(row)
    print(f"Patched undoable AOSP notification row dismissal: {row_path}")
else:
    print("ARINA row undo wiring already applied")

if CONTROLLER_MARKER not in controller:
    anchors = [
        """        row.removeFromTransientContainer();
        if (row instanceof ExpandableNotificationRow) {""",
        """        row.removeFromTransientContainer();
        if (row instanceof ExpandableNotificationRow row) {""",
    ]
    anchor = next((candidate for candidate in anchors if candidate in controller), None)

    if anchor is None:
        # Newer revisions use pattern matching above, then still call removeFromTransientContainer.
        simple = "        row.removeFromTransientContainer();"
        if simple not in controller:
            raise SystemExit(
                "Android 16 onChildDismissed cleanup anchor not found; "
                "refuse to guess against a different controller revision."
            )
        replacement = f"""        // {CONTROLLER_MARKER}: keep the swiped row alive during Undo window.
        if (row instanceof ExpandableNotificationRow
                && com.android.systemui.arina.ArinaNotificationCenterController
                        .isRowDismissPending((ExpandableNotificationRow) row)) {{
            return;
        }}
        row.removeFromTransientContainer();"""
        controller = controller.replace(simple, replacement, 1)
    else:
        replacement = f"""        // {CONTROLLER_MARKER}: keep the swiped row alive during Undo window.
        if (row instanceof ExpandableNotificationRow
                && com.android.systemui.arina.ArinaNotificationCenterController
                        .isRowDismissPending((ExpandableNotificationRow) row)) {{
            return;
        }}
{anchor}"""
        controller = controller.replace(anchor, replacement, 1)

    controller_path.write_text(controller)
    print(f"Patched notification-row cleanup for Undo: {controller_path}")
else:
    print("ARINA notification controller undo wiring already applied")

if STACK_MARKER not in stack:
    # 1) Style the real AOSP FooterView. Existing History/Manage/Clear-All listeners remain.
    footer_signature = "public void setFooterView(@NonNull FooterView footerView) {"
    footer_assignment = "mFooterView = footerView;"

    if footer_signature not in stack:
        raise SystemExit("Android 16 FooterView binding anchor not found")

    footer_start = stack.index(footer_signature)
    assignment_at = stack.find(footer_assignment, footer_start)
    if assignment_at < 0:
        raise SystemExit("Android 16 FooterView assignment anchor not found")

    assignment_end = assignment_at + len(footer_assignment)
    stack = (
        stack[:assignment_end]
        + f"""
        // {STACK_MARKER}: presentation only; listeners/content descriptions stay AOSP-owned.
        com.android.systemui.arina.ArinaNotificationCenterController.styleFooter(mFooterView);"""
        + stack[assignment_end:]
    )

    # 2) Keep the notification shade open during the Undo window after clear-all.
    clear_all_candidates = [
        """public void clearAllNotifications(boolean hideSilentSection) {
        clearNotifications(ROWS_ALL, /* closeShade = */ true, hideSilentSection);
    }""",
        """public void clearAllNotifications(boolean hideSilentSection) {
        clearNotifications(ROWS_ALL, true /* closeShade */, hideSilentSection);
    }""",
    ]
    clear_anchor = next((candidate for candidate in clear_all_candidates if candidate in stack), None)
    if clear_anchor is None:
        raise SystemExit("Android 16 clearAllNotifications anchor not found")

    clear_replacement = clear_anchor.replace(
        "/* closeShade = */ true",
        "/* closeShade = */ !com.android.systemui.arina.ArinaNotificationCenterController.isUndoEnabled()",
    ).replace(
        "true /* closeShade */",
        "!com.android.systemui.arina.ArinaNotificationCenterController.isUndoEnabled() /* closeShade */",
    )
    stack = stack.replace(clear_anchor, clear_replacement, 1)

    # 3) Delay the backend clear-all callback until Undo expires.
    method_anchor = """private void onClearAllAnimationsEnd(
            List<ExpandableNotificationRow> viewsToRemove,
            @SelectedRows int selectedRows) {"""
    if method_anchor not in stack:
        raise SystemExit("Android 16 onClearAllAnimationsEnd anchor not found")

    method_start = stack.index(method_anchor)
    listener_block = """        if (mClearAllAnimationListener != null) {
            mClearAllAnimationListener.onAnimationEnd(viewsToRemove, selectedRows);
        }"""
    listener_at = stack.find(listener_block, method_start)
    if listener_at < 0:
        raise SystemExit("Android 16 clear-all backend listener block not found")

    listener_replacement = """        if (mClearAllAnimationListener != null) {
            final ClearAllAnimationListener arinaListener = mClearAllAnimationListener;
            if (com.android.systemui.arina.ArinaNotificationCenterController.stageClearAll(
                    this,
                    viewsToRemove,
                    () -> arinaListener.onAnimationEnd(viewsToRemove, selectedRows))) {
                return;
            }
            arinaListener.onAnimationEnd(viewsToRemove, selectedRows);
        }"""
    stack = stack[:listener_at] + listener_replacement + stack[listener_at + len(listener_block):]

    stack_path.write_text(stack)
    print(f"Patched ARINA history/footer + staged clear-all: {stack_path}")
else:
    print("ARINA notification center stack wiring already applied")
