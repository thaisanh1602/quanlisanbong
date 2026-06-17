package view;

import dao.NhanVienDAO;
import model.NhanVien;
import model.TaiKhoan;
import view.panel.*;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.text.NumberFormat;
import java.util.Locale;

public class MainFrame extends JFrame {

    public static TaiKhoan currentUser;

    // Quản lý các màn hình (Panel) bằng CardLayout
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private LichDatSanPanel lichDatSanPanel;

    public MainFrame() {
        this(new TaiKhoan(1, "admin", "", "Quản lý"));
    }

    public MainFrame(TaiKhoan taiKhoan) {
        this.currentUser = taiKhoan;

        setTitle("Phần Mềm Quản Lý Sân Bóng - Xin chào " + taiKhoan.getTenDangNhap());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); // Mở rộng kích thước cho đủ chỗ làm việc
        setLocationRelativeTo(null);
        setResizable(true);

        initMenuBar();
        initContentPanels();

        // Kiểm tra thông báo lương khi đăng nhập
        checkPaydayNotification();
    }

    /**
     * Khởi tạo Thanh Điều Hướng Ribbon (Ribbon Navigation Bar)
     */
    private void initMenuBar() {
        JTabbedPane ribbonPane = new JTabbedPane();
        ribbonPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- TAB 1: TÀI KHOẢN (Chỉ Admin) ---
        if ("Quản lý".equalsIgnoreCase(currentUser.getLoaiTK())) {
            JPanel pnlTaiKhoan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            JPanel groupTaiKhoan = createRibbonGroup("Tài khoản");
            JButton btnQuanLyTK = createRibbonButton("⚙️", "Quản lý\ntài khoản");
            btnQuanLyTK.addActionListener(e -> showPanel("TaiKhoanPanel"));
            groupTaiKhoan.add(btnQuanLyTK, BorderLayout.CENTER);

            pnlTaiKhoan.add(groupTaiKhoan);
            ribbonPane.addTab("Tài khoản", pnlTaiKhoan);
        }

        JPanel pnlHeThong = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Nhóm "Chức năng"
        JPanel groupChucNangSys = createRibbonGroup("Chức năng");
        JPanel btnsChucNangSys = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDatSan = createRibbonButton("⚽", "Đặt sân");
        btnDatSan.addActionListener(e -> showPanel("DatSanPanel"));
        JButton btnLichDatSan = createRibbonButton("📅", "Lịch đặt\nsân");
        btnLichDatSan.addActionListener(e -> showPanel("LichDatSanPanel"));
        btnsChucNangSys.add(btnDatSan);
        btnsChucNangSys.add(btnLichDatSan);
        groupChucNangSys.add(btnsChucNangSys, BorderLayout.CENTER);

        // Nhóm "Giao tiếp"
        JPanel groupGiaoTiep = createRibbonGroup("Giao tiếp");
        JButton btnChat = createRibbonButton("💬", "Chat nội bộ\n& Khách");
        btnChat.addActionListener(e -> showPanel("ChatClientGUI"));
        groupGiaoTiep.add(btnChat, BorderLayout.CENTER);

        pnlHeThong.add(groupChucNangSys);
        pnlHeThong.add(groupGiaoTiep);
        ribbonPane.addTab("Hệ Thống", pnlHeThong);

        // --- TAB 3: QUẢN LÝ ---
        JPanel pnlQuanLy = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JPanel groupNhanSu = createRibbonGroup("Nhân sự");
        JButton btnQLNS = createRibbonButton("👥", "Quản lí\nnhân sự");
        btnQLNS.addActionListener(e -> showPanel("QuanLyNhanSuPanel"));
        groupNhanSu.add(btnQLNS, BorderLayout.CENTER);

        if ("Quản lý".equalsIgnoreCase(currentUser.getLoaiTK())) {
            pnlQuanLy.add(groupNhanSu);
        }

        JPanel groupChucNangQL = createRibbonGroup("Chức năng");
        JButton btnXemBangSan = createRibbonButton("📋", "Xem bảng\nsân");
        btnXemBangSan.addActionListener(e -> showPanel("XemBangSanPanel"));
        groupChucNangQL.add(btnXemBangSan, BorderLayout.CENTER);

        JPanel groupThongKe = createRibbonGroup("Thống kê");
        JButton btnBaoCao = createRibbonButton("📊", "Báo cáo");
        btnBaoCao.addActionListener(e -> showPanel("BaoCaoPanel"));
        groupThongKe.add(btnBaoCao, BorderLayout.CENTER);

        pnlQuanLy.add(groupChucNangQL);
        pnlQuanLy.add(groupThongKe);
        ribbonPane.addTab("Quản lý", pnlQuanLy);

        // UI Đăng Xuất và Gắn panel
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(ribbonPane, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Đăng xuất (" + currentUser.getTenDangNhap() + ")");
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

        // Hover effect mô phỏng nút trên Ribbon
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

    /**
     * Tích hợp các Màn hình (Panels) bằng CardLayout
     */
    private void initContentPanels() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Khởi tạo các Mock Panel đã tạo trước đó
        mainContentPanel.add(new JPanel(), "TrangChu"); // Panel trống ban đầu lót nền
        mainContentPanel.add(new TaiKhoanPanel(), "TaiKhoanPanel");
        mainContentPanel.add(new QuanLyNhanSuPanel(), "QuanLyNhanSuPanel");
        mainContentPanel.add(new XemBangSanPanel(), "XemBangSanPanel");
        mainContentPanel.add(new DatSanPanel(), "DatSanPanel");

        lichDatSanPanel = new LichDatSanPanel();
        mainContentPanel.add(lichDatSanPanel, "LichDatSanPanel");
        mainContentPanel.add(new BaoCaoPanel(), "BaoCaoPanel");
        
        // Khởi tạo Chat Panel (Kết nối tới localhost:5005)
        String role = currentUser.getLoaiTK();
        String chatName = currentUser.getTenDangNhap();
        network.client.ChatClientGUI chatPanel = new network.client.ChatClientGUI(chatName, role, "127.0.0.1", 5005);
        mainContentPanel.add(chatPanel, "ChatClientGUI");

        // Set panel TrangChu là trang hiển thị mặc định khi chạy
        cardLayout.show(mainContentPanel, "TrangChu");

        // Thêm mainContentPanel vào khu vực giữa của JFrame
        add(mainContentPanel, BorderLayout.CENTER);
    }

    /**
     * Hàm dùng để lật (hoán đổi) các Màn hình
     * 
     * @param panelName Tên định danh của Panel đã được khai báo ở initContentPanels
     */
    public void showPanel(String panelName) {
        cardLayout.show(mainContentPanel, panelName);
    }

    /**
     * Chuyển đến trang Lịch đặt sân và làm mới bảng dữ liệu
     */
    public void showLichDatSan() {
        if (lichDatSanPanel != null) {
            lichDatSanPanel.refreshTableData(null, null);
        }
        showPanel("LichDatSanPanel");
    }

    private void checkPaydayNotification() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int PAYDAY = 7; // Ngày 31 hàng tháng là ngày nhận lương (Để test vì hôm nay là 31)

        String message = "";
        String title = "Thông Báo Lương";
        int messageType = JOptionPane.INFORMATION_MESSAGE;

        if (day == PAYDAY) {
            message = "📢 Hôm nay là ngày nhận lương (Ngày " + PAYDAY + " hàng tháng)!\n" +
                    "Chúc bạn một ngày làm việc vui vẻ và hiệu quả.";
            title = "🎉 Thông Báo Nhận Lương";
        } else if (day > PAYDAY - 3 && day < PAYDAY) {
            message = "🔔 Sắp đến ngày nhận lương của nhân viên (Ngày " + PAYDAY + " hàng tháng).\n" +
                    "Hãy chuẩn bị bảng lương và các công việc liên quan.";
            title = "Thông Báo Sắp Nhận Lương";
        }

        if (!message.isEmpty()) {
            String finalMessage = message;
            String finalTitle = title;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, finalMessage, finalTitle, messageType);
            });
        }
    }

    public static void main(String[] args) {
        ThemeSetup.applyTheme();

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
