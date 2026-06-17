package network.client;

import network.call.VideoCallManager;
import network.call.VoiceCallManager;
import network.model.ChatMessage;
import network.util.EmojiPanel;
import network.util.SpeechUtil;
import network.util.SteganographyUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import dao.TaiKhoanDAO;
import model.TaiKhoan;
import java.util.List;
import java.util.ArrayList;

public class ChatClientGUI extends JPanel {

    private String username;
    private String role;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // UI Components
    private JPanel chatCards;
    private java.awt.CardLayout cardLayout;
    private java.util.Map<String, JTextPane> chatPanes = new java.util.HashMap<>();
    private String currentConversation;
    
    private JList<String> conversationList;
    private DefaultListModel<String> conversationModel;
    private JTextField inputField;
    private JButton btnSend, btnEmoji, btnFile, btnStego, btnVoice, btnVideo, btnSpeech;
    private JLabel roomNameLabel;
    // cbActiveUsers removed
    
    // Danh sách tất cả nhân viên và admin (dùng cho liên hệ chéo)
    private List<String> allNhanVien = new ArrayList<>();
    private List<String> allAdmin = new ArrayList<>();

    // Managers
    private VoiceCallManager voiceCall;
    private VideoCallManager videoCall;

    // Dedicated sequential processors for streams
    private final ExecutorService audioExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService videoExecutor = Executors.newSingleThreadExecutor();

    // =============================================
    // THEME — Messenger Gray Background
    // =============================================
    private static final Color BG_MAIN         = new Color(235, 237, 240);  // Nền xám nhạt
    private static final Color BG_HEADER       = new Color(255, 255, 255);  // Header trắng
    private static final Color BG_INPUT        = new Color(255, 255, 255);  // Ô nhập trắng
    private static final Color BG_BUBBLE_ME    = new Color(0, 132, 255);    // #0084FF
    private static final Color BG_BUBBLE_OTHER = new Color(255, 255, 255);  // Bóng người khác màu trắng
    private static final Color BG_HOVER        = new Color(225, 228, 232);

    private static final Color TEXT_WHITE  = new Color(5, 5, 5);            // Chữ chính (Black in light theme)
    private static final Color TEXT_MUTED  = new Color(101, 103, 107);      // Chữ phụ
    private static final Color TEXT_SYSTEM = new Color(101, 103, 107);      // Thông báo hệ thống
    private static final Color ACCENT_GREEN = new Color(49,  162, 76);      // Active status
    private static final Color MESSENGER_BLUE = new Color(0, 132, 255);

    private static final Color DIVIDER = new Color(220, 222, 225);

    private static final Font FONT_MAIN  = new Font("Segoe UI Emoji", Font.PLAIN, 15);
    private static final Font FONT_BOLD  = new Font("Segoe UI Emoji", Font.BOLD,  15);
    private static final Font FONT_SMALL = new Font("Segoe UI",       Font.PLAIN, 12);

    // =============================================

    public ChatClientGUI(String username, String role, String ip, int port) {
        this.username = username;
        this.role = role;
        
        // Nạp danh sách Nhân viên và Quản lý từ DB
        TaiKhoanDAO tkDao = new TaiKhoanDAO();
        for (TaiKhoan tk : tkDao.getAllTaiKhoan()) {
            String urole = tk.getLoaiTK();
            if (urole != null) {
                if ("Nhan Vien".equalsIgnoreCase(urole) || "Nhân viên".equalsIgnoreCase(urole)) {
                    allNhanVien.add(tk.getTenDangNhap());
                } else if ("Quan Ly".equalsIgnoreCase(urole) || "Quản lý".equalsIgnoreCase(urole)) {
                    allAdmin.add(tk.getTenDangNhap());
                }
            }
        }
        
        setLayout(new BorderLayout());
        if (!setupConnection(ip, port)) {
            add(new JLabel("Lỗi kết nối tới Server Chat", SwingConstants.CENTER));
            return;
        }
        voiceCall = new VoiceCallManager(username, this::sendMessage);
        videoCall = new VideoCallManager(username, this::sendMessage);
        videoCall.setOnEndCall(this::toggleVideoCall);
        buildUI();
        new Thread(this::listenLoop).start();
    }

    private boolean setupConnection(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            sendMessage(new ChatMessage(username, ChatMessage.Type.USERNAME).extra(role));
            return true;
        } catch (Exception e) {
            System.err.println("Không thể kết nối tới Server Chat: " + e.getMessage());
            return false;
        }
    }

    private void addFormRow(JPanel form, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_MUTED);
        form.add(lbl); form.add(field);
    }

    private JTextField createDialogField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_MUTED);
        tf.setCaretColor(TEXT_WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT_WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().trim().isEmpty()) {
                    tf.setForeground(TEXT_MUTED);
                    tf.setText(placeholder);
                }
            }
        });
        return tf;
    }

    private String getRawName(String sel) {
        if (sel == null) return null;
        int idx = sel.indexOf(" (");
        if (idx != -1 && !sel.equals("Phòng Nội Bộ (Admin & Nhân Viên)") && !sel.equals("Chăm sóc khách hàng (Chung)")) {
            return sel.substring(0, idx).trim();
        }
        return sel.trim();
    }
    
    private JTextPane getOrCreateChatPane(String convName) {
        if (!chatPanes.containsKey(convName)) {
            JTextPane tp = new JTextPane();
            tp.setEditable(false);
            tp.setBackground(BG_MAIN);
            tp.setForeground(TEXT_WHITE);
            tp.setMargin(new Insets(14, 22, 10, 22));
            
            JScrollPane scroll = new JScrollPane(tp);
            scroll.setBorder(null);
            scroll.setBackground(BG_MAIN);
            scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
            scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
            
            chatCards.add(scroll, convName);
            chatPanes.put(convName, tp);
        }
        return chatPanes.get(convName);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_MAIN);
        
        main.add(buildHeader(), BorderLayout.NORTH);

        cardLayout = new java.awt.CardLayout();
        chatCards = new JPanel(cardLayout);
        chatCards.setBackground(BG_MAIN);
        
        main.add(chatCards, BorderLayout.CENTER);
        main.add(buildBottom(), BorderLayout.SOUTH);

        conversationModel = new DefaultListModel<>();
        conversationList = new JList<>(conversationModel);
        conversationList.setFont(FONT_MAIN);
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.setBackground(Color.WHITE);
        conversationList.setCellRenderer(new ConversationCellRenderer());
        
        // Cải tiến giao diện JList
        conversationList.setFixedCellHeight(55);
        conversationList.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        conversationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = getRawName(conversationList.getSelectedValue());
                if (sel != null && !sel.isEmpty()) {
                    currentConversation = sel;
                    getOrCreateChatPane(sel);
                    cardLayout.show(chatCards, sel);
                    if (roomNameLabel != null) {
                        roomNameLabel.setText("Đang chat với: " + sel);
                    }
                    updateHeaderButtonsState();
                }
            }
        });
        
        JScrollPane sidebar = new JScrollPane(conversationList);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, DIVIDER));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, main);
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(1);
        
        add(splitPane, BorderLayout.CENTER);
        
        String defaultRoom;
        if ("Khach Hang".equalsIgnoreCase(role)) {
            defaultRoom = allNhanVien.isEmpty() ? null : allNhanVien.get(0);
        } else {
            defaultRoom = "Phòng Nội Bộ (Admin & Nhân Viên)";
        }
        currentConversation = defaultRoom;
        if (defaultRoom != null) {
            getOrCreateChatPane(defaultRoom);
            cardLayout.show(chatCards, defaultRoom);
            appendSystemMessage(defaultRoom, "Chào mừng bạn đã kết nối thành công!");
        }
        
        inputField.requestFocus();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                new EmptyBorder(14, 22, 14, 22)));

        // Chấm online + tên phòng
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_GREEN);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(10, 14));
        dot.setOpaque(false);

        roomNameLabel = new JLabel("Phòng Chat");
        roomNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        roomNameLabel.setForeground(TEXT_WHITE);
        left.add(dot); left.add(roomNameLabel);



        // Nút chức năng, Avatar + username
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        btnVoice  = mkIconBtn("📞", "Gọi Thoại");
        btnVoice.addActionListener(e  -> toggleVoiceCall());

        btnVideo  = mkIconBtn("📹", "Gọi Video");
        btnVideo.addActionListener(e  -> toggleVideoCall());

        btnSpeech = mkIconBtn("🎤", "Nhận diện giọng nói");
        btnSpeech.addActionListener(e -> recordSpeech());

        right.add(btnVoice); right.add(btnVideo); right.add(btnSpeech);
        
        updateHeaderButtonsState();

        String initial = username.substring(0, 1).toUpperCase();
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MESSENGER_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                        (getWidth()  - fm.stringWidth(initial)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);

        JLabel userLbl = new JLabel(username);
        userLbl.setFont(FONT_SMALL);
        userLbl.setForeground(TEXT_MUTED);

        right.add(userLbl); right.add(avatar);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(BG_MAIN);
        bottom.setBorder(new EmptyBorder(10, 18, 18, 18));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        toolbar.setOpaque(false);

        btnFile   = mkIconBtn("📁", "Gửi File");
        btnStego  = mkIconBtn("🛡️", "Ảnh Mật Mã");

        btnFile.addActionListener(e   -> sendFile());
        btnStego.addActionListener(e  -> sendStegoImage());

        toolbar.add(btnFile); toolbar.add(btnStego);

        // Input row (Modern pill shape)
        JPanel inputRow = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                g2.dispose();
            }
        };
        inputRow.setOpaque(false);
        inputRow.setBorder(new EmptyBorder(6, 18, 6, 6));

        inputField = new JTextField();
        inputField.setFont(FONT_MAIN);
        inputField.setOpaque(false);
        inputField.setBackground(new Color(0,0,0,0));
        inputField.setForeground(TEXT_WHITE);
        inputField.setCaretColor(TEXT_WHITE);
        inputField.setBorder(null);
        inputField.addActionListener(e -> sendText());

        btnEmoji  = mkIconBtn("😀", "Emoji");
        final EmojiPanel[] emojiPanel = new EmojiPanel[1];
        btnEmoji.addActionListener(e  -> {
            if (emojiPanel[0] == null) {
                emojiPanel[0] = new EmojiPanel(btnEmoji, emoji -> {
                    inputField.replaceSelection(emoji);
                    inputField.requestFocus();
                });
            }
            emojiPanel[0].showNear(btnEmoji);
        });

        btnSend = new JButton("✈️");
        btnSend.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        btnSend.setForeground(MESSENGER_BLUE);
        btnSend.setOpaque(false);
        btnSend.setContentAreaFilled(false);
        btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> sendText());

        JPanel rightInput = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightInput.setOpaque(false);
        rightInput.add(btnEmoji);
        rightInput.add(btnSend);

        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(rightInput, BorderLayout.EAST);

        bottom.add(toolbar,   BorderLayout.WEST);
        bottom.add(inputRow,  BorderLayout.CENTER);
        return bottom;
    }

    private JButton mkIconBtn(String icon, String tip) {
        JButton btn = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_HOVER);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setToolTipText(tip);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setForeground(MESSENGER_BLUE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    // =============================================
    // SEND ACTIONS
    // =============================================

    private String getSelectedReceiver() {
        if (currentConversation == null || currentConversation.equals("Phòng Nội Bộ (Admin & Nhân Viên)") || currentConversation.equals("Chăm sóc khách hàng (Chung)")) {
            return null;
        }
        return currentConversation;
    }

    private void sendMessage(ChatMessage msg) {
        try {
            if (out != null) {
                synchronized (out) {
                    out.writeObject(msg);
                    out.flush();
                    out.reset();
                }
            }
        } catch (IOException e) {
            appendSystemMessage(currentConversation, "Lỗi kết nối: " + e.getMessage());
        }
    }

    private void sendText() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        sendMessage(new ChatMessage(username, ChatMessage.Type.TEXT).text(text).receiver(getSelectedReceiver()));
        inputField.setText("");
    }

    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                sendMessage(new ChatMessage(username, ChatMessage.Type.FILE_DATA)
                        .text(file.getName()).data(data).receiver(getSelectedReceiver()));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi đọc file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendStegoImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    JOptionPane.showMessageDialog(this, "Không phải file ảnh hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
                JTextField txtField = new JTextField();
                JPasswordField passField = new JPasswordField();
                p.add(new JLabel("Văn bản cần ẩn:")); p.add(txtField);
                p.add(new JLabel("Mật khẩu:"));      p.add(passField);

                if (JOptionPane.showConfirmDialog(this, p, "Bảo mật hình ảnh (Steganography)",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    String txt = txtField.getText();
                    String pwd = new String(passField.getPassword());
                    if (txt.isEmpty() || pwd.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    byte[] stegoData = SteganographyUtil.encode(image, txt, pwd);
                    sendMessage(new ChatMessage(username, ChatMessage.Type.STEGO_IMAGE)
                            .text(file.getName()).data(stegoData).receiver(getSelectedReceiver()));
                    appendSystemMessage(currentConversation, "Đã gửi ảnh kèm nội dung ẩn bí mật.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ảnh không phù hợp hoặc lỗi xử lý: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleVoiceCall() {
        String receiver = getSelectedReceiver();
        if (voiceCall.isActive()) {
            voiceCall.stopCall();
            btnVoice.setForeground(MESSENGER_BLUE);
            sendMessage(new ChatMessage(username, ChatMessage.Type.VOICE_CALL_END).receiver(receiver));
            appendSystemMessage(currentConversation, "Đã kết thúc gọi thoại.");
        } else {
            sendMessage(new ChatMessage(username, ChatMessage.Type.VOICE_CALL_REQUEST).receiver(receiver));
            appendSystemMessage(currentConversation, receiver == null ? "Đang bắt đầu cuộc gọi thoại nhóm..." : "Đang gọi thoại...");
        }
    }

    private void toggleVideoCall() {
        String receiver = getSelectedReceiver();
        if (videoCall.isActive()) {
            videoCall.stopCall();
            btnVideo.setForeground(MESSENGER_BLUE);
            if (voiceCall.isActive()) {
                voiceCall.stopCall();
                btnVoice.setForeground(MESSENGER_BLUE);
            }
            sendMessage(new ChatMessage(username, ChatMessage.Type.VIDEO_CALL_END).receiver(receiver));
            appendSystemMessage(currentConversation, "Đã kết thúc gọi video.");
        } else {
            sendMessage(new ChatMessage(username, ChatMessage.Type.VIDEO_CALL_REQUEST).receiver(receiver));
            appendSystemMessage(currentConversation, receiver == null ? "Đang bắt đầu cuộc gọi video nhóm..." : "Đang gọi video...");
        }
    }

    private void recordSpeech() {
        try {
            inputField.requestFocus();
            SpeechUtil.startWindowsVoiceTyping();
            appendSystemMessage(currentConversation, "Đã bật nhận diện giọng nói Windows. Hãy nói...");
        } catch (Exception e) {
            appendSystemMessage(currentConversation, "Lỗi không thể mở Voice Typing: " + e.getMessage());
        }
    }

    // =============================================
    // LISTEN LOOP — không thay đổi
    // =============================================

    private void listenLoop() {
        try {
            ChatMessage msg;
            while ((msg = (ChatMessage) in.readObject()) != null) handleIncomingMessage(msg);
        } catch (Exception e) {
            appendSystemMessage(currentConversation, "Mất kết nối tới server!");
            e.printStackTrace();
        }
    }

    private void handleIncomingMessage(ChatMessage msg) {
        boolean isMe = msg.getSender().equals(username);

        // Process heavy stream data ASYNCHRONOUSLY but SEQUENTIALLY to prevent network/GUI blocking and maintain packet order
        if (msg.getType() == ChatMessage.Type.VOICE_DATA) {
            if (!isMe) audioExecutor.submit(() -> voiceCall.receiveAudio(msg.getData()));
            return;
        }
        if (msg.getType() == ChatMessage.Type.VIDEO_FRAME) {
            if (!isMe) videoExecutor.submit(() -> videoCall.receiveFrame(msg.getSender(), msg.getData()));
            return;
        }

        // Process UI updates ON the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            switch (msg.getType()) {
                case TEXT:          appendChatBubble(msg, isMe); break;
                case EMOJI:         appendEmojiBubble(msg, isMe); break;
                case FILE_DATA:     handleFileReceive(msg); break;
                case STEGO_IMAGE:   handleStegoReceive(msg); break;
                case USER_LIST:     updateUserList(msg.getText()); break;
                case USER_JOINED:
                case USER_LEFT:     appendSystemMessage(currentConversation, msg.getText()); break;
                case VOICE_CALL_REQUEST:
                    if (!isMe) handleVoiceRequest(msg.getSender()); break;
                case VOICE_CALL_ACCEPT:
                    if (!isMe) {
                        try {
                            voiceCall.startCall();
                            btnVoice.setForeground(ACCENT_GREEN);
                            appendSystemMessage(currentConversation, msg.getSender() + " đã chấp nhận gọi thoại.");
                        } catch (Exception e) {
                            appendSystemMessage(currentConversation, "Lỗi thiết bị âm thanh: " + e.getMessage());
                        }
                    }
                    break;
                case VOICE_CALL_REJECT:
                    if (!isMe) appendSystemMessage(currentConversation, msg.getSender() + " đã từ chối gọi thoại."); break;
                case VOICE_CALL_END:
                    if (!isMe && voiceCall.isActive()) {
                        voiceCall.stopCall(); btnVoice.setForeground(MESSENGER_BLUE);
                        appendSystemMessage(currentConversation, "Cuộc gọi thoại đã kết thúc.");
                    }
                    break;
                case VIDEO_CALL_REQUEST:
                    if (!isMe) handleVideoRequest(msg.getSender()); break;
                case VIDEO_CALL_ACCEPT:
                    if (!isMe) {
                        videoCall.startCall(); btnVideo.setForeground(ACCENT_GREEN);
                        try { voiceCall.startCall(); btnVoice.setForeground(ACCENT_GREEN); } catch (Exception e) {}
                        appendSystemMessage(currentConversation, msg.getSender() + " đã chấp nhận gọi video (có âm thanh).");
                    }
                    break;
                case VIDEO_CALL_REJECT:
                    if (!isMe) appendSystemMessage(currentConversation, msg.getSender() + " đã từ chối gọi video."); break;
                case VIDEO_CALL_END:
                    if (!isMe && videoCall.isActive()) {
                        videoCall.stopCall(); btnVideo.setForeground(MESSENGER_BLUE);
                        if (voiceCall.isActive()) {
                            voiceCall.stopCall(); btnVoice.setForeground(MESSENGER_BLUE);
                        }
                        appendSystemMessage(currentConversation, "Cuộc gọi video đã kết thúc.");
                    }
                    break;
                default: break;
            }
        });
    }

    private void handleFileReceive(ChatMessage msg) {
        appendFileBubble(msg, msg.getSender().equals(username));
    }

    private void handleStegoReceive(ChatMessage msg) {
        boolean isMe = msg.getSender().equals(username);
        ActionListener onClick = e -> {
            JPasswordField passField = new JPasswordField();
            if (JOptionPane.showConfirmDialog(this, passField,
                    "Nhập mật khẩu để giải mã ảnh của " + msg.getSender() + ":",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String pwd = new String(passField.getPassword());
                try {
                    BufferedImage img = SteganographyUtil.bytesToImage(msg.getData());
                    String hidden = SteganographyUtil.decode(img, pwd);
                    JTextArea ta = new JTextArea(10, 30);
                    ta.setText(hidden); ta.setWrapStyleWord(true); ta.setLineWrap(true);
                    ta.setCaretPosition(0); ta.setEditable(false);
                    JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Nội dung bí mật", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Sai mật khẩu hoặc ảnh bị lỗi!", "Giải mã thất bại", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        appendImageBubble(msg, isMe, onClick);
    }

    private void handleVoiceRequest(String caller) {
        int ans = JOptionPane.showConfirmDialog(this,
                caller + " đang mời gọi thoại. Bạn có muốn tham gia?",
                "Cuộc gọi đến 📞", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                voiceCall.startCall();
                btnVoice.setForeground(ACCENT_GREEN);
                sendMessage(new ChatMessage(username, ChatMessage.Type.VOICE_CALL_ACCEPT).receiver(caller));
            } catch (Exception e) { appendSystemMessage(currentConversation, "Lỗi thiết bị thu âm."); }
        } else {
            sendMessage(new ChatMessage(username, ChatMessage.Type.VOICE_CALL_REJECT).receiver(caller));
        }
    }

    private void handleVideoRequest(String caller) {
        int ans = JOptionPane.showConfirmDialog(this,
                caller + " đang mời gọi video. Bạn có muốn tham gia?",
                "Cuộc gọi đến 🎥", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            videoCall.startCall();
            btnVideo.setForeground(ACCENT_GREEN);
            try { voiceCall.startCall(); btnVoice.setForeground(ACCENT_GREEN); } catch (Exception e) {}
            sendMessage(new ChatMessage(username, ChatMessage.Type.VIDEO_CALL_ACCEPT).receiver(caller));
        } else {
            sendMessage(new ChatMessage(username, ChatMessage.Type.VIDEO_CALL_REJECT).receiver(caller));
        }
    }

    private void updateUserList(String listData) {
        if (conversationModel == null) return;
        String currentSelection = conversationList.getSelectedValue();
        
        // Keep existing customers that messaged this staff
        java.util.Set<String> existingCustomers = new java.util.HashSet<>();
        for (int i=0; i<conversationModel.getSize(); i++) {
            if (conversationModel.getElementAt(i).contains("(Khách Hàng)")) {
                existingCustomers.add(conversationModel.getElementAt(i));
            }
        }
        
        conversationModel.removeAllElements();
        
        if (!"Khach Hang".equalsIgnoreCase(this.role)) {
            conversationModel.addElement("Phòng Nội Bộ (Admin & Nhân Viên)");
        }
        
        java.util.Set<String> onlineUsers = new java.util.HashSet<>();
        
        if (listData != null && !listData.isEmpty()) {
            String[] users = listData.split(",");
            for (String u : users) {
                String[] parts = u.split("\\|");
                if (parts.length >= 2) {
                    onlineUsers.add(parts[0]);
                }
            }
        }
        
        if ("Khach Hang".equalsIgnoreCase(this.role)) {
            for (String nv : allNhanVien) {
                if (onlineUsers.contains(nv)) {
                    conversationModel.addElement(nv + " (Đang hoạt động)");
                } else {
                    conversationModel.addElement(nv + " (Ngoại tuyến)");
                }
            }
        } else if ("Nhan Vien".equalsIgnoreCase(this.role) || "Nhân viên".equalsIgnoreCase(this.role)) {
            // Nhân Viên: Hiển thị tất cả Admin (online & offline)
            for (String ad : allAdmin) {
                if (onlineUsers.contains(ad)) {
                    conversationModel.addElement(ad + " (Đang hoạt động)");
                } else {
                    conversationModel.addElement(ad + " (Ngoại tuyến)");
                }
            }
            // Nhân Viên: Chỉ hiện cuộc hội thoại khách hàng khi có khách nhắn đến
            for (String c : existingCustomers) {
                conversationModel.addElement(c);
            }
        } else if ("Quan Ly".equalsIgnoreCase(this.role) || "Quản lý".equalsIgnoreCase(this.role)) {
            // Admin: Hiển thị tất cả Nhân Viên (online & offline)
            for (String nv : allNhanVien) {
                if (onlineUsers.contains(nv)) {
                    conversationModel.addElement(nv + " (Đang hoạt động)");
                } else {
                    conversationModel.addElement(nv + " (Ngoại tuyến)");
                }
            }
            // Đồng thời hiển thị các Admin khác nếu có
            for (String ad : allAdmin) {
                if (ad.equals(this.username)) continue;
                if (onlineUsers.contains(ad)) {
                    conversationModel.addElement(ad + " (Đang hoạt động)");
                } else {
                    conversationModel.addElement(ad + " (Ngoại tuyến)");
                }
            }
        }
        
        if (currentSelection != null) {
            conversationList.setSelectedValue(currentSelection, true);
        } else {
            conversationList.setSelectedIndex(0);
        }
    }

    // =============================================
    // UI APPEND
    // =============================================

    /** Header chung: tên + giờ + Trạng thái Private */
    private void insertBubbleHeader(StyledDocument doc, ChatMessage msg, boolean isMe) throws BadLocationException {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, TEXT_MUTED);
        StyleConstants.setFontFamily(attrs, "Segoe UI");
        StyleConstants.setFontSize(attrs, 12);
        StyleConstants.setAlignment(attrs, isMe ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);

        String time = new SimpleDateFormat("HH:mm").format(new Date());
        
        String receiverText = "";
        String targetConv = "Khach Hang".equals(this.role) ? "Chăm sóc khách hàng (Chung)" : "Phòng Nội Bộ (Admin & Nhân Viên)";
        
        if (msg.getReceiver() != null && !msg.getReceiver().isEmpty()) {
            receiverText = isMe ? " (Tới " + msg.getReceiver() + ")" : "";
            targetConv = isMe ? msg.getReceiver() : msg.getSender();
            
            if (!isMe && ("Nhan Vien".equalsIgnoreCase(this.role) || "Nhân viên".equalsIgnoreCase(this.role))) {
                String sender = msg.getSender();
                boolean exists = false;
                for (int i = 0; i < conversationModel.getSize(); i++) {
                    String item = conversationModel.getElementAt(i);
                    if (item.equals(sender) || item.startsWith(sender + " ")) { exists = true; break; }
                }
                if (!exists) {
                    conversationModel.addElement(sender + " (Khách Hàng)");
                }
            }
        }
        
        String header = isMe ? time + receiverText + " \n" : msg.getSender() + receiverText + "  •  " + time + "\n";

        doc.setParagraphAttributes(doc.getLength(), 1, attrs, false);
        doc.insertString(doc.getLength(), header, attrs);
    }

    private void appendChatBubble(ChatMessage msg, boolean isMe) {
        try {
            String targetConv = (msg.getReceiver() != null && !msg.getReceiver().isEmpty())
                    ? (isMe ? msg.getReceiver() : msg.getSender())
                    : ("Khach Hang".equals(this.role) ? "Chăm sóc khách hàng (Chung)" : "Phòng Nội Bộ (Admin & Nhân Viên)");
            JTextPane tPane = getOrCreateChatPane(targetConv);
            StyledDocument doc = tPane.getStyledDocument();
            insertBubbleHeader(doc, msg, isMe);

            JPanel bubble = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            bubble.setOpaque(false);
            // Tin nhắn private có màu cam thay vì xanh
            Color bgBubble = isMe ? (msg.getReceiver() != null ? new Color(243, 156, 18) : BG_BUBBLE_ME) : BG_BUBBLE_OTHER;
            bubble.setBackground(bgBubble);
            bubble.setBorder(new EmptyBorder(10, 16, 10, 16));

            JTextArea ta = new JTextArea(msg.getText());
            ta.setOpaque(false);
            ta.setEditable(false);
            ta.setLineWrap(false);
            ta.setFont(FONT_MAIN);
            ta.setForeground(isMe ? Color.WHITE : TEXT_WHITE);

            int w = ta.getPreferredSize().width;
            int h = ta.getPreferredSize().height;
            ta.setPreferredSize(new Dimension(w, h));

            bubble.add(ta, BorderLayout.CENTER);

            JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(0, 0, 6, isMe ? 18 : 0));
            wrapper.add(bubble);

            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isMe ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);

            tPane.setCaretPosition(doc.getLength());
            tPane.insertComponent(wrapper);
            doc.insertString(doc.getLength(), "\n", align);

        } catch (BadLocationException e) { e.printStackTrace(); }
    }

    private void appendEmojiBubble(ChatMessage msg, boolean isMe) {
        try {
            String targetConv = (msg.getReceiver() != null && !msg.getReceiver().isEmpty())
                    ? (isMe ? msg.getReceiver() : msg.getSender())
                    : ("Khach Hang".equals(this.role) ? "Chăm sóc khách hàng (Chung)" : "Phòng Nội Bộ (Admin & Nhân Viên)");
            JTextPane tPane = getOrCreateChatPane(targetConv);
            StyledDocument doc = tPane.getStyledDocument();
            insertBubbleHeader(doc, msg, isMe);

            JLabel lbl = new JLabel(msg.getText());
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
            lbl.setOpaque(false);

            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isMe ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);

            tPane.setCaretPosition(doc.getLength());
            tPane.insertComponent(lbl);
            doc.insertString(doc.getLength(), "\n\n", align);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void appendFileBubble(ChatMessage msg, boolean isMe) {
        try {
            String targetConv = (msg.getReceiver() != null && !msg.getReceiver().isEmpty())
                    ? (isMe ? msg.getReceiver() : msg.getSender())
                    : ("Khach Hang".equals(this.role) ? "Chăm sóc khách hàng (Chung)" : "Phòng Nội Bộ (Admin & Nhân Viên)");
            JTextPane tPane = getOrCreateChatPane(targetConv);
            StyledDocument doc = tPane.getStyledDocument();
            insertBubbleHeader(doc, msg, isMe);

            JPanel filePanel = new JPanel(new BorderLayout(14, 0)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            filePanel.setOpaque(false);
            // Đổi màu nếu là tin private
            Color bgBubble = isMe ? (msg.getReceiver() != null ? new Color(243, 156, 18) : BG_BUBBLE_ME) : BG_BUBBLE_OTHER;
            filePanel.setBackground(bgBubble);
            filePanel.setBorder(new EmptyBorder(12, 16, 12, 12));

            // Icon vòng tròn
            JPanel iconCircle = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isMe ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 15));
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            iconCircle.setOpaque(false);
            iconCircle.setPreferredSize(new Dimension(44, 44));
            JLabel iconLbl = new JLabel("📄", SwingConstants.CENTER);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            iconLbl.setForeground(isMe ? Color.WHITE : TEXT_WHITE);
            iconCircle.add(iconLbl, BorderLayout.CENTER);

            // Tên + dung lượng
            String filename = msg.getText();
            String displayName = filename;
            if (filename.length() > 22) {
                int extIdx = filename.lastIndexOf('.');
                if (extIdx > 0 && filename.length() - extIdx <= 5)
                    displayName = filename.substring(0, 10) + "…" + filename.substring(extIdx);
                else
                    displayName = filename.substring(0, 19) + "…";
            }
            JLabel nameLbl = new JLabel(displayName);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLbl.setForeground(isMe ? Color.WHITE : TEXT_WHITE);
            nameLbl.setToolTipText(filename);

            byte[] fileData = msg.getData();
            String sizeStr = fileData.length < 1_048_576
                    ? (fileData.length / 1024) + " KB"
                    : String.format("%.2f MB", fileData.length / 1_048_576.0);
            JLabel sizeLbl = new JLabel(sizeStr);
            sizeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            sizeLbl.setForeground(isMe ? new Color(230, 240, 255) : TEXT_MUTED);

            JPanel textInfo = new JPanel(new GridLayout(2, 1, 0, 3));
            textInfo.setOpaque(false);
            textInfo.add(nameLbl); textInfo.add(sizeLbl);

            JPanel leftContent = new JPanel(new BorderLayout(12, 0));
            leftContent.setOpaque(false);
            leftContent.add(iconCircle, BorderLayout.WEST);
            leftContent.add(textInfo,   BorderLayout.CENTER);
            filePanel.add(leftContent, BorderLayout.CENTER);

            // Nút download
            JButton dlBtn = new JButton("↓");
            dlBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            dlBtn.setForeground(isMe ? Color.WHITE : TEXT_MUTED);
            dlBtn.setFocusPainted(false);
            dlBtn.setContentAreaFilled(false);
            dlBtn.setBorderPainted(false);
            dlBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dlBtn.setToolTipText("Tải xuống " + filename);
            dlBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { dlBtn.setForeground(ACCENT_GREEN); }
                public void mouseExited(MouseEvent e)  { dlBtn.setForeground(isMe ? Color.WHITE : TEXT_MUTED); }
            });
            dlBtn.addActionListener(e -> {
                JFileChooser ch = new JFileChooser();
                ch.setSelectedFile(new File(filename));
                if (ch.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        Files.write(ch.getSelectedFile().toPath(), fileData);
                        JOptionPane.showMessageDialog(this, "Lưu file thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi lưu file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            filePanel.add(dlBtn, BorderLayout.EAST);

            JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(0, 0, 6, isMe ? 18 : 0));
            wrapper.add(filePanel);

            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isMe ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);

            tPane.setCaretPosition(doc.getLength());
            tPane.insertComponent(wrapper);
            doc.insertString(doc.getLength(), "\n\n", align);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void appendImageBubble(ChatMessage msg, boolean isMe, ActionListener onClick) {
        try {
            String targetConv = (msg.getReceiver() != null && !msg.getReceiver().isEmpty())
                    ? (isMe ? msg.getReceiver() : msg.getSender())
                    : ("Khach Hang".equals(this.role) ? "Chăm sóc khách hàng (Chung)" : "Phòng Nội Bộ (Admin & Nhân Viên)");
            JTextPane tPane = getOrCreateChatPane(targetConv);
            StyledDocument doc = tPane.getStyledDocument();
            insertBubbleHeader(doc, msg, isMe);

            byte[] imageData = msg.getData();
            BufferedImage img = SteganographyUtil.bytesToImage(imageData);
            int maxW = 240, w = img.getWidth(), h = img.getHeight();
            if (w > maxW) { h = (int)(h * ((double)maxW / w)); w = maxW; }
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);

            JButton imgBtn = new JButton(new ImageIcon(scaled));
            imgBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imgBtn.setBorder(BorderFactory.createLineBorder(
                    isMe ? BG_BUBBLE_ME : DIVIDER, 2));
            imgBtn.setContentAreaFilled(false);
            imgBtn.setToolTipText("Ảnh mật mã — click để giải mã");
            if (onClick != null) imgBtn.addActionListener(onClick);

            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, isMe ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(doc.getLength(), 1, align, false);

            tPane.setCaretPosition(doc.getLength());
            tPane.insertComponent(imgBtn);
            doc.insertString(doc.getLength(), "\n\n", align);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void appendSystemMessage(String conversation, String text) {
        try {
            JTextPane tPane = getOrCreateChatPane(conversation);
            StyledDocument doc = tPane.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);

            SimpleAttributeSet sysAttrs = new SimpleAttributeSet();
            StyleConstants.setForeground(sysAttrs, TEXT_SYSTEM);
            StyleConstants.setItalic(sysAttrs, true);
            StyleConstants.setFontFamily(sysAttrs, "Segoe UI");
            StyleConstants.setFontSize(sysAttrs, 12);
            StyleConstants.setAlignment(sysAttrs, StyleConstants.ALIGN_CENTER);

            doc.setParagraphAttributes(doc.getLength(), 1, center, false);
            doc.insertString(doc.getLength(), "— " + text + " —\n\n", sysAttrs);
            tPane.setCaretPosition(doc.getLength());

        } catch (BadLocationException e) { e.printStackTrace(); }
    }

    private void appendSystemMessage(String text) {
        appendSystemMessage(currentConversation, text);
    }

    // =============================================
    // CUSTOM SCROLLBAR
    // =============================================
    private static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 202, 206);
            this.trackColor = BG_MAIN;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 1, r.y, r.width - 2, r.height, 6, 6);
            g2.dispose();
        }
    }

    private void updateHeaderButtonsState() {
        if (btnVoice != null && btnVideo != null) {
            btnVoice.setVisible(true);
            btnVideo.setVisible(true);
        }
    }


}

// =========================================================================
// CUSTOM CELL RENDERER — MESSENGER LIKE SIDEBAR ITEMS
// =========================================================================
class ConversationCellRenderer extends JPanel implements ListCellRenderer<String> {
    private JLabel lblName = new JLabel();
    private JLabel lblStatus = new JLabel();
    private JPanel avatarPanel;
    private String displayChar = "👥";
    private boolean isOnline = false;

    public ConversationCellRenderer() {
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setOpaque(true);

        avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ ảnh đại diện hình tròn
                g2.setColor(new Color(0, 132, 255));
                g2.fillOval(0, 0, 36, 36);
                
                // Vẽ ký tự viết tắt
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(displayChar)) / 2;
                int y = ((36 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(displayChar, x, y);
                
                // Điểm xanh trạng thái hoạt động
                if (isOnline) {
                    g2.setColor(new Color(49, 162, 76)); // Xanh lá cây
                    g2.fillOval(26, 26, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(26, 26, 10, 10);
                }
                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(36, 36));
        avatarPanel.setOpaque(false);

        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(120, 120, 120));

        JPanel textPnl = new JPanel(new GridLayout(2, 1, 1, 1));
        textPnl.setOpaque(false);
        textPnl.add(lblName);
        textPnl.add(lblStatus);

        add(avatarPanel, BorderLayout.WEST);
        add(textPnl, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(new Color(230, 240, 255)); // Màu hover Messenger
            lblName.setForeground(new Color(0, 132, 255));
        } else {
            setBackground(Color.WHITE);
            lblName.setForeground(new Color(5, 5, 5));
        }

        if (value != null) {
            if (value.equals("Chăm sóc khách hàng (Chung)") || value.equals("Phòng Nội Bộ (Admin & Nhân Viên)")) {
                lblName.setText(value);
                lblStatus.setText("Thảo luận nhóm");
                displayChar = "👥";
                isOnline = false;
            } else {
                int idx = value.indexOf(" (");
                String name = (idx != -1) ? value.substring(0, idx) : value;
                lblName.setText(name);
                displayChar = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
                
                if (value.contains("Đang hoạt động")) {
                    lblStatus.setText("Đang hoạt động");
                    isOnline = true;
                } else if (value.contains("Ngoại tuyến")) {
                    lblStatus.setText("Ngoại tuyến");
                    isOnline = false;
                } else if (value.contains("Khách Hàng")) {
                    lblStatus.setText("Khách hàng cần tư vấn");
                    isOnline = true;
                } else {
                    lblStatus.setText("");
                    isOnline = false;
                }
            }
        }
        return this;
    }
}