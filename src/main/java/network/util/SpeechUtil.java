package network.util;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Speech-to-Text utility.
 * Since Windows SAPI offline doesn't natively support Vietnamese well without language packs,
 * the most reliable & modern way is to trigger Windows 10/11 built-in Voice Typing (Win + H).
 * This will pop up the native dictation UI and automatically type Vietnamese into the focused text field.
 */
public class SpeechUtil {

    /**
     * Triggers the Windows native Voice Typing shortcut (Win + H).
     * Must ensure the input text field has focus before calling this.
     */
    public static void startWindowsVoiceTyping() throws Exception {
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_WINDOWS);
        robot.keyPress(KeyEvent.VK_H);
        robot.keyRelease(KeyEvent.VK_H);
        robot.keyRelease(KeyEvent.VK_WINDOWS);
    }
}
