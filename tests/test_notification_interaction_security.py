from pathlib import Path
import unittest


REPO = Path(__file__).resolve().parents[1]
STYLER = (
    REPO
    / "frameworks/base/packages/SystemUI/src/com/android/systemui/arina/"
      "ArinaNotificationInteractionStyler.java"
)


class NotificationInteractionSecurityTest(unittest.TestCase):
    def test_arina_does_not_replace_aosp_interaction_or_security_handlers(self):
        source = STYLER.read_text()

        forbidden = [
            "setOnClickListener(",
            "setOnTouchListener(",
            "setAccessibilityDelegate(",
            "PendingIntent.send",
            "RemoteInput.addResultsToIntent",
            "dismissKeyguard",
            "keyguardDone",
        ]

        for token in forbidden:
            self.assertNotIn(token, source, token)

    def test_expected_real_aosp_surfaces_are_styled(self):
        source = STYLER.read_text()

        required = [
            "RemoteInputView",
            "SmartReplyView",
            "getProvider",
            "getMenuView",
            "actions_container",
            "contentDescription/onClick",
        ]

        for token in required:
            self.assertIn(token, source, token)


if __name__ == "__main__":
    unittest.main()
