package view.panel;

import dao.SanBongDAO;
import model.SanBong;
import view.KhachHangMainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TrangChuKhachHangPanel extends JPanel {
    
    private JPanel gridPanel;
    private KhachHangMainFrame mainFrame;

    public TrangChuKhachHangPanel(KhachHangMainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("DANH SÁCH SÂN BÓNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(33, 37, 41));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Grid Sân bóng
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20)); // 3 cột, khoảng cách 20px
        gridPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadDanhSachSan();
    }

    private void loadDanhSachSan() {
        gridPanel.removeAll();
        SanBongDAO sbDao = new SanBongDAO();
        List<SanBong> listSan = sbDao.getAllSanBong();
        
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (SanBong sb : listSan) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
            ));
            card.setBackground(new Color(250, 250, 250));

            // Icon/Hình ảnh mô phỏng
            JLabel lblIcon = new JLabel("⚽", SwingConstants.CENTER);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblIcon);
            card.add(Box.createRigidArea(new Dimension(0, 10)));

            // Tên sân
            JLabel lblTen = new JLabel(sb.getTenSan(), SwingConstants.CENTER);
            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTen.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblTen);
            
            // Thông tin
            JLabel lblLoai = new JLabel("Loại: Sân " + sb.getLoaiSan() + " người");
            lblLoai.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblLoai);

            JLabel lblKhuVuc = new JLabel("Khu vực: " + sb.getKhuVuc());
            lblKhuVuc.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblKhuVuc);

            JLabel lblGia = new JLabel("Giá: " + nf.format(sb.getGiaTien()) + "/giờ");
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGia.setForeground(new Color(220, 53, 69)); // Màu đỏ
            lblGia.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(lblGia);

            // Trạng thái
            JLabel lblTrangThai = new JLabel(sb.getTrangThai() == 0 ? "Khả dụng" : "Đang bảo trì");
            lblTrangThai.setForeground(sb.getTrangThai() == 0 ? new Color(40, 167, 69) : new Color(255, 193, 7));
            lblTrangThai.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(lblTrangThai);

            card.add(Box.createRigidArea(new Dimension(0, 15)));

            // Nút Đặt sân
            JButton btnDatSan = new JButton("Đặt Sân Này");
            btnDatSan.setBackground(new Color(0, 123, 255));
            btnDatSan.setForeground(Color.WHITE);
            btnDatSan.setFocusPainted(false);
            btnDatSan.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            if (sb.getTrangThai() == 1) {
                btnDatSan.setEnabled(false);
                btnDatSan.setText("Bảo Trì");
            } else {
                btnDatSan.addActionListener(e -> {
                    // Chuyển hướng sang panel đặt sân
                    mainFrame.showPanel("KhachHangDatSanPanel");
                });
            }
            card.add(btnDatSan);

            gridPanel.add(card);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}
