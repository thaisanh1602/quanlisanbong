package view;


import dao.TaiKhoanDAO;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DangKyFrame extends JFrame {
    private JTextField txtUsername;
    private JTextField txtSDT;
    private JPasswordField txtPassword;
    private JPasswordField txtRePassword;
    private JButton btnRegister;
    private JButton btnBack;

    private final Color PASTEL_BLUE = new Color(224, 240, 255);

    public DangKyFrame() {
        setTitle("Đăng Ký Tài Khoản Khách Hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(PASTEL_BLUE);
        formPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblTitle);

        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Tên Đăng Nhập
        formPanel.add(createLabel("Tên đăng nhập:"));
        txtUsername = createTextField();
        formPanel.add(txtUsername);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Số điện thoại
        formPanel.add(createLabel("Số điện thoại:"));
        txtSDT = createTextField();
        formPanel.add(txtSDT);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Mật khẩu
        formPanel.add(createLabel("Mật khẩu:"));
        txtPassword = createPasswordField();
        formPanel.add(txtPassword);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nhập lại mật khẩu
        formPanel.add(createLabel("Nhập lại mật khẩu:"));
        txtRePassword = createPasswordField();
        formPanel.add(txtRePassword);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));



        // Buttons
        btnRegister = new JButton("ĐĂNG KÝ");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBackground(new Color(0, 110, 255));
        btnRegister.setFocusPainted(false);
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(350, 40));
        btnRegister.addActionListener(e -> xulyDangKy());
        formPanel.add(btnRegister);

        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        btnBack = new JButton("Quay lại Đăng Nhập");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setForeground(new Color(108, 117, 125));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        formPanel.add(btnBack);

        add(formPanel, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(350, 35));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setMaximumSize(new Dimension(350, 35));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setAlignmentX(Component.CENTER_ALIGNMENT);
        return pf;
    }

    private void xulyDangKy() {
        String username = txtUsername.getText().trim();
        String sdt = txtSDT.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String rePass = new String(txtRePassword.getPassword());

        if (username.isEmpty() || sdt.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Validate số điện thoại cơ bản
        if (!sdt.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 số.");
            return;
        }

        if (!pass.equals(rePass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không khớp!");
            return;
        }

        TaiKhoanDAO tkDao = new TaiKhoanDAO();
        TaiKhoan tk = new TaiKhoan(0, username, sdt, "", "Khach Hang"); // Role Khach Hang
        
        if (tkDao.insertTaiKhoan(tk, pass)) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.");
            new LoginFrame().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập / Số điện thoại đã tồn tại hoặc có lỗi xảy ra!");
        }
    }
}
