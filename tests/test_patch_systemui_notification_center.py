import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


REPO = Path(__file__).resolve().parents[1]
PATCHER = REPO / "scripts" / "patch-systemui-notification-center.py"


class NotificationCenterPatcherTest(unittest.TestCase):
    def test_patches_history_clear_all_and_undo_idempotently(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)

            row = root / (
                "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
                "notification/row/ExpandableNotificationRow.java"
            )
            stack = root / (
                "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
                "notification/stack/NotificationStackScrollLayout.java"
            )
            controller = root / (
                "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
                "notification/stack/NotificationStackScrollLayoutController.java"
            )

            row.parent.mkdir(parents=True)
            stack.parent.mkdir(parents=True)
            controller.parent.mkdir(parents=True)

            row.write_text(
                """
public class ExpandableNotificationRow {
    public void dismiss(boolean refocusOnDismiss) {
        super.dismiss(refocusOnDismiss);
    }
}
""".strip()
            )

            stack.write_text(
                """
public class NotificationStackScrollLayout {
    private FooterView mFooterView;
    private ClearAllAnimationListener mClearAllAnimationListener;

    public void setFooterView(@NonNull FooterView footerView) {
        mFooterView = footerView;
        addView(mFooterView);
    }

    public void clearAllNotifications(boolean hideSilentSection) {
        clearNotifications(ROWS_ALL, /* closeShade = */ true, hideSilentSection);
    }

    private void onClearAllAnimationsEnd(
            List<ExpandableNotificationRow> viewsToRemove,
            @SelectedRows int selectedRows) {
        if (mClearAllAnimationListener != null) {
            mClearAllAnimationListener.onAnimationEnd(viewsToRemove, selectedRows);
        }
    }
}
""".strip()
            )

            controller.write_text(
                """
public class NotificationStackScrollLayoutController {
    public void onChildDismissed(View view) {
        ActivatableNotificationView row = (ActivatableNotificationView) view;
        row.removeFromTransientContainer();
        if (row instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) row).removeChildrenWithKeepInParent();
        }
    }
}
""".strip()
            )

            env = dict(os.environ)
            env["AOSP_DIR"] = str(root)

            subprocess.run([sys.executable, str(PATCHER)], check=True, cwd=REPO, env=env)

            row_text = row.read_text()
            stack_text = stack.read_text()
            controller_text = controller.read_text()

            self.assertIn("ARINA_NOTIFICATION_UNDO_ROW_WIRING", row_text)
            self.assertIn("stageRowDismiss", row_text)
            self.assertIn("ARINA_NOTIFICATION_CENTER_STACK_WIRING", stack_text)
            self.assertIn("styleFooter", stack_text)
            self.assertIn("stageClearAll", stack_text)
            self.assertIn("isUndoEnabled", stack_text)
            self.assertIn("ARINA_NOTIFICATION_UNDO_CONTROLLER_WIRING", controller_text)
            self.assertIn("isRowDismissPending", controller_text)

            first = (row_text, stack_text, controller_text)
            subprocess.run([sys.executable, str(PATCHER)], check=True, cwd=REPO, env=env)
            second = (row.read_text(), stack.read_text(), controller.read_text())

            self.assertEqual(first, second)


if __name__ == "__main__":
    unittest.main()
