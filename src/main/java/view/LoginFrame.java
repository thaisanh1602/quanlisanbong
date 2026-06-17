package view;

import com.formdev.flatlaf.FlatLightLaf;
import dao.TaiKhoanDAO;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    
    // Màu xanh pastel
    private final Color PASTEL_BLUE = new Color(224, 240, 255);

    public LoginFrame() {
        // Thiết lập giao diện cơ bản
        setTitle("Đăng Nhập Hệ Thống Quản Lý Sân Bóng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 550); // Kích thước cửa sổ
        setLocationRelativeTo(null); // Giữa màn hình
        setResizable(true); // Cửa sổ có thể phóng to, thu nhỏ
        setLayout(new GridLayout(1, 2)); // Chia đôi màn hình tự động trái phải

        initLeftPanel();
        initRightPanel();
    }

    private void initLeftPanel() {
        // Panel bên trái chứa hình nền, sử dụng paintComponent để auto scale ảnh khi resize
        JPanel leftPanel = new JPanel() {
            private Image backgroundImage;

            {
                try {
                    URL imageUrl = getClass().getResource("/images/bg_login.jpg");
                    if (imageUrl != null) {
                        backgroundImage = new ImageIcon(imageUrl).getImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    // Vẽ ảnh phủ kín toàn bộ kích thước của panel
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.dispose();
                } else {
                    g.setColor(Color.RED);
                    g.drawString("Không tìm thấy ảnh nền", getWidth() / 2 - 60, getHeight() / 2);
                }
            }
        };

        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(PASTEL_BLUE); // Nếu không có ảnh thì sẽ hiện màu này

        add(leftPanel);
    }

    private void initRightPanel() {
        // Panel bên phải chứa Form đăng nhập, thiết lập nền xanh pastel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout()); // Dùng GridBagLayout để căn giữa nội dung
        rightPanel.setBackground(PASTEL_BLUE); 

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PASTEL_BLUE); // Form cũng đồng màu với RightPanel
        formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Tiêu đề căn trái
        JLabel lblTitle = new JLabel("CHÀO MỪNG TRỞ LẠI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblTitle);
        
        JLabel lblSubTitle = new JLabel("Vui lòng đăng nhập tài khoản của bạn");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubTitle.setForeground(new Color(108, 117, 125));
        lblSubTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblSubTitle);

        formPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Input Username
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT); // Chữ lùi sang trái
        formPanel.add(lblUser);

        formPanel.add(Box.createRigidArea(new Dimension(0, 8))); // Khoảng cách nhỏ giữa Label và TextField

        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(350, 40)); // Cố định chiều rộng tối đa
        txtUsername.setPreferredSize(new Dimension(350, 40));
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập...");
        txtUsername.putClientProperty("JTextField.showClearButton", true);
        formPanel.add(txtUsername);

        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Input Password
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT); // Chữ lùi sang trái
        formPanel.add(lblPass);

        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(350, 40)); // Cố định chiều rộng tối đa
        txtPassword.setPreferredSize(new Dimension(350, 40));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        txtPassword.putClientProperty("JPasswordField.showRevealButton", true);
        formPanel.add(txtPassword);

        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Nút đăng nhập
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(0, 110, 255)); // Màu xanh dương hiện đại
        btnLogin.setFocusPainted(false);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT); 
        btnLogin.setMaximumSize(new Dimension(350, 45)); // Cố định chiều rộng tối đa bằng với TextField
        btnLogin.setPreferredSize(new Dimension(350, 45));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Sự kiện click nút đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xulyDangNhap();
            }
        });
        
        // Nhấn Enter ở ô mật khẩu cũng là đăng nhập
        txtPassword.addActionListener(e -> xulyDangNhap());

        // Nút đăng ký (dành cho Khách Hàng)
        JButton btnRegister = new JButton("ĐĂNG KÝ TÀI KHOẢN");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(new Color(0, 110, 255));
        btnRegister.setBackground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT); 
        btnRegister.setMaximumSize(new Dimension(350, 45));
        btnRegister.setPreferredSize(new Dimension(350, 45));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRegister.addActionListener(e -> {
            new DangKyFrame().setVisible(true);
            this.dispose();
        });

        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(btnRegister);

        // Bọc formPanel vào giữa của rightPanel
        rightPanel.add(formPanel, new GridBagConstraints());
        
        add(rightPanel);
    }

    private void xulyDangNhap() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tài khoản và Mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaiKhoanDAO tkDao = new TaiKhoanDAO();
        try {
            TaiKhoan taiKhoan = tkDao.kiemTraDangNhap(username, password);

            if (taiKhoan != null) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công với chức vụ: " + taiKhoan.getLoaiTK(), "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                // Mở cửa sổ chính tương ứng
                if (taiKhoan.getLoaiTK().equalsIgnoreCase("Khach Hang")) {
                    KhachHangMainFrame khFrame = new KhachHangMainFrame(taiKhoan);
                    khFrame.setVisible(true);
                } else {
                    MainFrame mainFrame = new MainFrame(taiKhoan);
                    mainFrame.setVisible(true);
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThemeSetup.applyTheme();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
