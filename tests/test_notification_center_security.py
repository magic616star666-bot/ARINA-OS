from pathlib import Path
import unittest


REPO = Path(__file__).resolve().parents[1]
CONTROLLER = (
    REPO
    / "frameworks/base/packages/SystemUI/src/com/android/systemui/arina/"
      "ArinaNotificationCenterController.java"
)


class NotificationCenterSecurityTest(unittest.TestCase):
    def test_history_and_clear_all_security_remain_aosp_owned(self):
        source = CONTROLLER.read_text()

        forbidden = [
            "ACTION_NOTIFICATION_HISTORY",
            "PendingIntent.send",
            "cancelNotification(",
            "cancelAllNotifications(",
            "NotificationManager.cancel",
            "setAccessibilityDelegate(",
            "keyguardDone",
            "dismissKeyguard",
        ]

        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_undo_is_generic_and_accessible(self):
        source = CONTROLLER.read_text()

        required = [
            "announceForAccessibility",
            "setContentDescription",
            "UNDO_WINDOW_MS",
            "stageRowDismiss",
            "stageClearAll",
            "isClearable()",
        ]

        for token in required:
            self.assertIn(token, source, token)


if __name__ == "__main__":
    unittest.main()
