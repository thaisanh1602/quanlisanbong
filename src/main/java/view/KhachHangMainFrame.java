package view;

import model.TaiKhoan;
import view.panel.KhachHangDatSanPanel;
import view.panel.KhachHangLichDatPanel;
import view.panel.TrangChuKhachHangPanel;

import javax.swing.*;
import java.awt.*;

public class KhachHangMainFrame extends JFrame {

    public static TaiKhoan currentUser;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    public KhachHangMainFrame(TaiKhoan taiKhoan) {
        KhachHangMainFrame.currentUser = taiKhoan;
        
        String tenHienThi = taiKhoan.getTenDangNhap();

        setTitle("Đặt Sân Bóng - Xin chào " + tenHienThi);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        initMenuBar();
        initContentPanels();
    }

    private void initMenuBar() {
        JTabbedPane ribbonPane = new JTabbedPane();
        ribbonPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- TAB 1: TRANG CHỦ & ĐẶT SÂN ---
        JPanel pnlDatSan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JPanel groupDichVu = createRibbonGroup("Dịch vụ");
        
        JButton btnTrangChu = createRibbonButton("🏠", "Trang chủ");
        btnTrangChu.addActionListener(e -> showPanel("TrangChuKhachHangPanel"));
        groupDichVu.add(btnTrangChu, BorderLayout.WEST);

        JButton btnDatSan = createRibbonButton("⚽", "Đặt sân\nnhanh");
        btnDatSan.addActionListener(e -> showPanel("KhachHangDatSanPanel"));
        groupDichVu.add(btnDatSan, BorderLayout.EAST);

        pnlDatSan.add(groupDichVu);
        
        JPanel groupCaNhan = createRibbonGroup("Cá nhân");
        JButton btnLichSu = createRibbonButton("📅", "Lịch đặt\ncủa tôi");
        btnLichSu.addActionListener(e -> {
            KhachHangLichDatPanel lichPanel = (KhachHangLichDatPanel) getPanelByName("KhachHangLichDatPanel");
            if (lichPanel != null) lichPanel.loadData();
            showPanel("KhachHangLichDatPanel");
        });
        groupCaNhan.add(btnLichSu, BorderLayout.CENTER);
        
        pnlDatSan.add(groupCaNhan);
        ribbonPane.addTab("Thuê Sân", pnlDatSan);

        JPanel pnlHoTro = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel groupChat = createRibbonGroup("Liên hệ Ban Quản Lý");
        JButton btnChat = createRibbonButton("💬", "Chat trực tiếp\nAdmin");
        btnChat.addActionListener(e -> showPanel("ChatClientGUI"));
        groupChat.add(btnChat, BorderLayout.CENTER);
        
        pnlHoTro.add(groupChat);
        ribbonPane.addTab("Hỗ Trợ", pnlHoTro);

        // UI Đăng Xuất và Gắn panel
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(ribbonPane, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        
        JPanel logoutPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPnl.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        logoutPnl.add(btnLogout);
        topWrapper.add(logoutPnl, BorderLayout.EAST);

        add(topWrapper, BorderLayout.NORTH);
    }

    private JPanel createRibbonGroup(String groupName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        JLabel lblTitle = new JLabel(groupName, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(120, 120, 120));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        panel.add(lblTitle, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createRibbonButton(String emoji, String text) {
        JButton btn = new JButton("<html><center><font size='6'>" + emoji + "</font><br>"
                + text.replaceAll("\n", "<br>") + "</center></html>");
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(225, 235, 245));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setContentAreaFilled(false);
                btn.setBackground(UIManager.getColor("control"));
            }
        });
        return btn;
    }

    private void initContentPanels() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Khởi tạo các Panels
        mainContentPanel.add(new TrangChuKhachHangPanel(this), "TrangChuKhachHangPanel");
        mainContentPanel.add(new KhachHangDatSanPanel(), "KhachHangDatSanPanel");
        mainContentPanel.add(new KhachHangLichDatPanel(), "KhachHangLichDatPanel");
        
        // Khởi tạo Chat Panel (Kết nối tới localhost:5005)
        String chatName = currentUser.getTenDangNhap();
        network.client.ChatClientGUI chatPanel = new network.client.ChatClientGUI(chatName, "Khach Hang", "127.0.0.1", 5005);
        mainContentPanel.add(chatPanel, "ChatClientGUI");

        cardLayout.show(mainContentPanel, "TrangChuKhachHangPanel");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainContentPanel, panelName);
    }
    
    // Helper method to retrieve a panel instance safely
    private JPanel getPanelByName(String name) {
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp instanceof KhachHangLichDatPanel && name.equals("KhachHangLichDatPanel")) {
                return (JPanel) comp;
            }
            if (comp instanceof KhachHangDatSanPanel && name.equals("KhachHangDatSanPanel")) {
                return (JPanel) comp;
            }
        }
        return null;
    }
}
