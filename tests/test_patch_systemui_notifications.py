import os
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


REPO = Path(__file__).resolve().parents[1]
PATCHER = REPO / "scripts" / "patch-systemui-notifications.py"


class NotificationPatcherTest(unittest.TestCase):
    def test_patches_privacy_expansion_and_group_hooks_idempotently(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            entry = (
                root
                / "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
                "notification/collection/NotificationEntry.java"
            )
            row = (
                root
                / "frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/"
                "notification/row/ExpandableNotificationRow.java"
            )
            entry.parent.mkdir(parents=True)
            row.parent.mkdir(parents=True)

            entry.write_text(
                """
public final class NotificationEntry {
    public void setSensitive(boolean sensitive, boolean deviceSensitive) {
        getRow().setSensitive(sensitive, deviceSensitive);
    }
}
""".strip()
            )

            row.write_text(
                """
public class ExpandableNotificationRow {
    public void setUserExpanded(boolean userExpanded, boolean allowChildExpansion) {
        mFalsingManager.setNotificationExpanded();
    }

    public void setChildrenExpanded(boolean expanded) {
        mChildrenExpanded = expanded;
    }
}
""".strip()
            )

            env = dict(os.environ)
            env["AOSP_DIR"] = str(root)

            subprocess.run(
                [sys.executable, str(PATCHER)],
                check=True,
                cwd=REPO,
                env=env,
            )

            entry_text = entry.read_text()
            row_text = row.read_text()

            self.assertIn("ARINA_NOTIFICATION_PRIVACY_WIRING", entry_text)
            self.assertIn("onPrivacyStateChanged", entry_text)
            self.assertIn("ARINA_NOTIFICATION_INTERACTION_WIRING", row_text)
            self.assertIn("onUserExpansionRequested", row_text)
            self.assertIn("ARINA_NOTIFICATION_GROUP_WIRING", row_text)
            self.assertIn("onGroupExpansionChanged", row_text)

            first_entry = entry_text
            first_row = row_text

            subprocess.run(
                [sys.executable, str(PATCHER)],
                check=True,
                cwd=REPO,
                env=env,
            )

            self.assertEqual(first_entry, entry.read_text())
            self.assertEqual(first_row, row.read_text())


if __name__ == "__main__":
    unittest.main()
