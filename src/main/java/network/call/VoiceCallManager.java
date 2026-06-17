package network.call;

import network.model.ChatMessage;
import javax.sound.sampled.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Voice call manager using Java Sound API.
 * Audio data is sent as VOICE_DATA ChatMessage objects through the existing
 * TCP server connection (no P2P/NAT issues).
 */
public class VoiceCallManager {

    // Audio format: 16 kHz, 16-bit, mono (Standard VoIP Wideband Quality)
    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, false);
    private static final int CHUNK_SIZE = 4096; // bytes per packet (~128ms)

    private final Consumer<ChatMessage> sender;
    private final String username;

    private final AtomicBoolean active = new AtomicBoolean(false);
    private SourceDataLine speakers;
    private Thread captureThread;

    public VoiceCallManager(String username, Consumer<ChatMessage> sender) {
        this.username = username;
        this.sender   = sender;
    }

    // ─────────────────────── START / STOP ───────────────────────

    public void startCall() throws LineUnavailableException {
        if (active.get()) return;

        // Open playback line FIRST to test audio system
        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, FORMAT);
        speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
        speakers.open(FORMAT, 16384); // 0.5s playback buffer to absorb network jitter
        speakers.start();

        active.set(true);

        // Capture thread
        captureThread = new Thread(this::captureLoop, "VoiceCapture");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void stopCall() {
        active.set(false);
        if (captureThread != null) captureThread.interrupt();
        if (speakers != null) { 
            speakers.drain(); 
            speakers.close(); 
        }
    }

    public boolean isActive() { return active.get(); }

    // ─────────────────────── CAPTURE LOOP ───────────────────────

    private void captureLoop() {
        try {
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("[Voice] Microphone not supported at 44.1kHz.");
                active.set(false);
                return;
            }
            TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(micInfo);
            mic.open(FORMAT, 8192);
            mic.start();

            byte[] buffer = new byte[CHUNK_SIZE];
            while (active.get() && !Thread.currentThread().isInterrupted()) {
                int n = mic.read(buffer, 0, buffer.length);
                if (n > 0) {
                    byte[] packet = new byte[n];
                    System.arraycopy(buffer, 0, packet, 0, n);

                    ChatMessage msg = new ChatMessage(username, ChatMessage.Type.VOICE_DATA);
                    msg.setData(packet);
                    sender.accept(msg);
                }
            }
            mic.stop();
            mic.close();
        } catch (LineUnavailableException e) {
            System.err.println("[Voice] Capture error: " + e.getMessage());
            active.set(false);
        }
    }

    // ─────────────────────── RECEIVE AUDIO ──────────────────────

    /** Called when a VOICE_DATA packet arrives from the remote peer. */
    public void receiveAudio(byte[] data) {
        if (active.get() && speakers != null && speakers.isOpen()) {
            speakers.write(data, 0, data.length);
        }
    }
}
