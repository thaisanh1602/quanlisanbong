package network.call;

import network.model.ChatMessage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Video call manager.
 * 
 * Supports Sarxos WebcamCapture JAR via reflection if available.
 * If not available, uses a simulated moving placeholder instead of
 * screen capturing the desktop to avoid confusion.
 */
public class VideoCallManager {

    private static final int FPS        = 10;        
    private static final int FRAME_W    = 320;
    private static final int FRAME_H    = 240;
    private static final float JPEG_Q   = 0.5f;     

    private final Consumer<ChatMessage> sender;
    private final String username;
    private Runnable onEndCall;

    private final AtomicBoolean active = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;

    private final java.util.Map<String, JLabel> remoteLabels = new java.util.concurrent.ConcurrentHashMap<>();
    private JPanel remoteContainer;

    // UI
    private JFrame videoFrame;
    private JLabel localLabel;

    // Webcam Reflection
    private Object webcam = null;
    
    // For simulated camera fallback
    private int simX = 0;
    private int simY = 0;
    private int simDx = 5;
    private int simDy = 5;

    public VideoCallManager(String username, Consumer<ChatMessage> sender) {
        this.username = username;
        this.sender   = sender;
    }

    public void setOnEndCall(Runnable onEndCall) {
        this.onEndCall = onEndCall;
    }

    // ─────────────────────── START / STOP ───────────────────────

    public void startCall() {
        if (active.get()) return;
        active.set(true);

        // Try to initialize and open webcam
        try {
            Class<?> wcClass = Class.forName("com.github.sarxos.webcam.Webcam");
            webcam = wcClass.getMethod("getDefault").invoke(null);
            if (webcam != null) {
                wcClass.getMethod("open").invoke(webcam);
            }
        } catch (Throwable ignored) {
            webcam = null;
        }

        buildVideoWindow();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VideoCapture");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::captureAndSendSafe, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
    }

    public void stopCall() {
        active.set(false);
        if (scheduler != null) scheduler.shutdownNow();
        if (videoFrame != null) SwingUtilities.invokeLater(() -> videoFrame.dispose());
        
        // Close webcam safely
        if (webcam != null) {
            try {
                webcam.getClass().getMethod("close").invoke(webcam);
            } catch (Throwable ignored) {}
            webcam = null;
        }
        remoteLabels.clear();
    }

    public boolean isActive() { return active.get(); }

    // ─────────────────────── CAPTURE ───────────────────────────

    private void captureAndSendSafe() {
        try {
            captureAndSend();
        } catch (Throwable t) {
            System.err.println("[Video] Critical capture error: " + t.getMessage());
        }
    }

    private void captureAndSend() throws Exception {
        BufferedImage frame = captureFrame();
        if (frame == null) return;

        // Update local preview
        Image scaled = frame.getScaledInstance(FRAME_W, FRAME_H, Image.SCALE_FAST);
        SwingUtilities.invokeLater(() -> {
            if (localLabel != null) localLabel.setIcon(new ImageIcon(scaled));
        });

        // Compress to JPEG
        byte[] jpg = toJpeg(frame);

        ChatMessage msg = new ChatMessage(username, ChatMessage.Type.VIDEO_FRAME);
        msg.setData(jpg);
        sender.accept(msg);
    }

    /**
     * Tries to capture from webcam via Sarxos.
     * Falls back to a simulated generated frame if no webcam is present.
     */
    private BufferedImage captureFrame() {
        if (webcam != null) {
            try {
                return (BufferedImage) webcam.getClass().getMethod("getImage").invoke(webcam);
            } catch (Throwable ignored) {}
        }
        
        // ── Fallback: Simulated Camera (Moving Object) ──
        BufferedImage img = new BufferedImage(FRAME_W, FRAME_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        // Background
        g.setColor(new Color(40, 42, 54));
        g.fillRect(0, 0, FRAME_W, FRAME_H);
        
        // Update simulation coordinates
        simX += simDx;
        simY += simDy;
        if (simX <= 0 || simX >= FRAME_W - 50) simDx = -simDx;
        if (simY <= 0 || simY >= FRAME_H - 50) simDy = -simDy;
        
        // Draw moving square
        g.setColor(new Color(88, 101, 242));
        g.fillRoundRect(simX, simY, 50, 50, 15, 15);
        
        // Draw Text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.drawString("Mô phỏng Camera", 90, FRAME_H / 2 - 10);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("(Không tìm thấy thiết bị Webcam)", 70, FRAME_H / 2 + 15);
        
        g.dispose();
        return img;
    }

    // ─────────────────────── RECEIVE FRAME ─────────────────────

    public void receiveFrame(String senderName, byte[] jpegData) {
        if (!active.get()) return;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(jpegData));
            if (img == null) return;
            Image scaled = img.getScaledInstance(FRAME_W, FRAME_H, Image.SCALE_FAST);
            SwingUtilities.invokeLater(() -> {
                JLabel lbl = remoteLabels.get(senderName);
                if (lbl == null) {
                    lbl = makeVideoLabel("Đang chờ...", new Color(30, 30, 46));
                    remoteLabels.put(senderName, lbl);
                    if (remoteContainer != null) {
                        remoteContainer.add(wrapLabel(lbl, senderName, new Color(30, 30, 46)));
                        remoteContainer.revalidate();
                        remoteContainer.repaint();
                        
                        // Dynamically adjust window size based on participants
                        if (videoFrame != null) {
                            videoFrame.setSize(FRAME_W * (remoteLabels.size() + 1) + 80, FRAME_H + 130);
                        }
                    }
                }
                lbl.setIcon(new ImageIcon(scaled));
            });
        } catch (Exception e) {
            System.err.println("[Video] Receive error: " + e.getMessage());
        }
    }

    // ─────────────────────── UI ────────────────────────────────

    private void buildVideoWindow() {
        SwingUtilities.invokeLater(() -> {
            videoFrame = new JFrame("📹 Group Video Call");
            videoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            videoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    if (onEndCall != null) onEndCall.run();
                    else stopCall();
                }
            });

            Color bg = new Color(30, 30, 46);
            JPanel content = new JPanel(new BorderLayout(10, 10));
            content.setBackground(bg);
            content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            remoteContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            remoteContainer.setBackground(bg);

            JLabel defaultRemote = makeVideoLabel("Đang chờ đối phương...", bg);
            remoteLabels.put("Chờ kết nối", defaultRemote);
            remoteContainer.add(wrapLabel(defaultRemote, "Đối tác", bg));

            localLabel  = makeVideoLabel("Camera của bạn", bg);

            content.add(remoteContainer, BorderLayout.CENTER);
            content.add(wrapLabel(localLabel, "Bạn (Camera)", bg), BorderLayout.EAST);

            JButton endBtn = new JButton("🔴 Kết thúc cuộc gọi");
            endBtn.setBackground(new Color(243, 139, 168)); // Red color
            endBtn.setForeground(Color.WHITE);
            endBtn.setFocusPainted(false);
            endBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            endBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            endBtn.addActionListener(e -> {
                if (onEndCall != null) onEndCall.run();
                else stopCall();
            });

            JPanel south = new JPanel();
            south.setBackground(bg);
            south.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            south.add(endBtn);

            videoFrame.getContentPane().setBackground(bg);
            videoFrame.add(content, BorderLayout.CENTER);
            videoFrame.add(south, BorderLayout.SOUTH);
            videoFrame.setSize(FRAME_W * 2 + 60, FRAME_H + 130);
            videoFrame.setLocationRelativeTo(null);
            videoFrame.setVisible(true);
        });
    }

    private JLabel makeVideoLabel(String placeholder, Color bg) {
        JLabel lbl = new JLabel(placeholder, SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(FRAME_W, FRAME_H));
        lbl.setBackground(new Color(24, 24, 37));
        lbl.setForeground(new Color(166, 173, 200));
        lbl.setOpaque(true);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Soft border
        lbl.setBorder(BorderFactory.createLineBorder(new Color(69, 71, 90), 2));
        return lbl;
    }

    private JPanel wrapLabel(JLabel lbl, String title, Color bg) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(bg);
        JLabel ttl = new JLabel(title, SwingConstants.CENTER);
        ttl.setForeground(Color.WHITE);
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(ttl, BorderLayout.NORTH);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────── COMPRESSION ───────────────────────

    private byte[] toJpeg(BufferedImage img) throws Exception {
        BufferedImage small = new BufferedImage(FRAME_W, FRAME_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.drawImage(img, 0, 0, FRAME_W, FRAME_H, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPEG_Q);
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new javax.imageio.IIOImage(small, null, null), param);
        writer.dispose();
        return baos.toByteArray();
    }
}
