package view.panel;

import com.toedter.calendar.JDateChooser;
import dao.PhieuDatSanDAO;
import dao.SanBongDAO;
import model.PhieuDatSan;
import model.SanBong;
import view.KhachHangMainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KhachHangDatSanPanel extends JPanel {

    private JComboBox<SanBongItem> cbSanBong;
    private JDateChooser dateChooser;
    private JComboBox<String> cbGioBatDau;
    private JComboBox<String> cbGioKetThuc;
    private JLabel lblTongTien;
    
    private JTextField txtTenKhachHang;
    private JTextField txtSDT;
    
    private double currentGiaTien = 0;

    public KhachHangDatSanPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 40, 20, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("ĐẶT SÂN & THANH TOÁN ONLINE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(33, 37, 41));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Chọn Sân Bóng
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Chọn Sân Bóng:"), gbc);
        cbSanBong = new JComboBox<>();
        loadDanhSachSan();
        cbSanBong.addActionListener(e -> tinhTongTien());
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(cbSanBong, gbc);

        // Họ và Tên (Thêm mới)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(createLabel("Họ và Tên:"), gbc);
        txtTenKhachHang = new JTextField();
        txtTenKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtTenKhachHang, gbc);

        // Số điện thoại (Thêm mới)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(createLabel("Số điện thoại:"), gbc);
        String prefilledSdt = "";
        if (KhachHangMainFrame.currentUser != null) {
            prefilledSdt = KhachHangMainFrame.currentUser.getSdt();
            if (prefilledSdt == null || prefilledSdt.trim().isEmpty()) {
                prefilledSdt = KhachHangMainFrame.currentUser.getTenDangNhap();
            }
        }
        txtSDT = new JTextField(prefilledSdt);
        txtSDT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSDT.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        formPanel.add(txtSDT, gbc);

        // Ngày Thuê
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(createLabel("Ngày Thuê:"), gbc);
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new java.util.Date());
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        formPanel.add(dateChooser, gbc);

        // Giờ Bắt Đầu
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(createLabel("Giờ Bắt Đầu:"), gbc);
        String[] hours = {"06:00", "07:00", "08:00", "09:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"};
        cbGioBatDau = new JComboBox<>(hours);
        cbGioBatDau.addActionListener(e -> tinhTongTien());
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        formPanel.add(cbGioBatDau, gbc);

        // Giờ Kết Thúc
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        formPanel.add(createLabel("Giờ Kết Thúc:"), gbc);
        String[] endHours = {"07:00", "08:00", "09:00", "10:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        cbGioKetThuc = new JComboBox<>(endHours);
        cbGioKetThuc.addActionListener(e -> tinhTongTien());
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
        formPanel.add(cbGioKetThuc, gbc);

        // Tổng Tiền
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        formPanel.add(createLabel("Tạm Tính:"), gbc);
        lblTongTien = new JLabel("0 VNĐ");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTien.setForeground(new Color(220, 53, 69)); // Đỏ
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        formPanel.add(lblTongTien, gbc);

        // Nút Thanh Toán
        JButton btnThanhToan = new JButton("THANH TOÁN BẰNG MÃ QR");
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnThanhToan.setBackground(new Color(40, 167, 69)); // Xanh lá
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.addActionListener(e -> xuLyDatSanVaThanhToan());
        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(btnThanhToan, gbc);

        add(formPanel, BorderLayout.CENTER);
        tinhTongTien();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private void loadDanhSachSan() {
        SanBongDAO sbDao = new SanBongDAO();
        List<SanBong> list = sbDao.getSanBongByTrangThai(0); // Chỉ lấy sân Khả dụng
        for (SanBong sb : list) {
            cbSanBong.addItem(new SanBongItem(sb));
        }
    }

    private void tinhTongTien() {
        SanBongItem item = (SanBongItem) cbSanBong.getSelectedItem();
        if (item == null) return;
        
        currentGiaTien = item.sanBong.getGiaTien();
        
        String batDauStr = (String) cbGioBatDau.getSelectedItem();
        String ketThucStr = (String) cbGioKetThuc.getSelectedItem();
        
        try {
            int h1 = Integer.parseInt(batDauStr.split(":")[0]);
            int h2 = Integer.parseInt(ketThucStr.split(":")[0]);
            
            float duration = h2 - h1;
            if (duration <= 0) {
                lblTongTien.setText("Giờ không hợp lệ!");
                return;
            }
            
            double tongTien = currentGiaTien * duration;
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            lblTongTien.setText(nf.format(tongTien));
            
        } catch (Exception ex) {
            lblTongTien.setText("0 VNĐ");
        }
    }

    private void xuLyDatSanVaThanhToan() {
        String ten = txtTenKhachHang.getText().trim();
        String sdt = txtSDT.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ Tên và Số Điện Thoại để liên hệ!");
            return;
        }

        SanBongItem item = (SanBongItem) cbSanBong.getSelectedItem();
        if (item == null) return;
        
        String batDauStr = (String) cbGioBatDau.getSelectedItem();
        String ketThucStr = (String) cbGioKetThuc.getSelectedItem();
        java.util.Date selectedDate = dateChooser.getDate();

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày!");
            return;
        }

        int h1 = Integer.parseInt(batDauStr.split(":")[0]);
        int h2 = Integer.parseInt(ketThucStr.split(":")[0]);
        float duration = h2 - h1;

        if (duration <= 0) {
            JOptionPane.showMessageDialog(this, "Giờ kết thúc phải lớn hơn giờ bắt đầu!");
            return;
        }

        // Tính tổng tiền
        double tongTien = currentGiaTien * duration;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String sqlDate = sdf.format(selectedDate);
        
        // Kiểm tra xem sân có bị trùng giờ không trước khi thanh toán
        PhieuDatSanDAO phieuDao = new PhieuDatSanDAO();
        boolean isAvailable = phieuDao.isSanBongAvailable(
                item.sanBong.getMaSan(), 
                Date.valueOf(sqlDate), 
                Time.valueOf(batDauStr + ":00"), 
                Time.valueOf(ketThucStr + ":00")
        );
        
        if (!isAvailable) {
            JOptionPane.showMessageDialog(this, "Sân bóng này đã có người đặt trong khoảng thời gian bạn chọn!\nVui lòng chọn giờ hoặc sân khác.", "Lỗi Trùng Lịch", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PhieuDatSan phieu = new PhieuDatSan();
        phieu.setTenKhachHang(ten);
        phieu.setSdtKhach(sdt);
        phieu.setMaSan(item.sanBong.getMaSan());
        phieu.setNgayThue(Date.valueOf(sqlDate));
        phieu.setGioBatDau(Time.valueOf(batDauStr + ":00"));
        phieu.setGioKetThuc(Time.valueOf(ketThucStr + ":00"));
        phieu.setDuration(duration);
        phieu.setTrangThaiTT(1); // 1 = Đã thanh toán
        
        // Gọi hàm hiển thị mã QR thực tế
        showThanhToanQR(tongTien, phieu);
    }

    private void showThanhToanQR(double tongTien, PhieuDatSan phieu) {
        String amount = String.valueOf((long) tongTien);
        String addInfo = "Dat San " + phieu.getMaSan() + " SDT " + phieu.getSdtKhach();
        addInfo = addInfo.replaceAll(" ", "%20");
        
        // Cấu hình VietQR: MB Bank, STK: 0336261171, Tên: Tran Thai Anh
        String urlString = "https://img.vietqr.io/image/MB-0336261171-compact2.png?amount=" + amount + "&addInfo=" + addInfo + "&accountName=Tran%20Thai%20Anh";
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thanh Toán VietQR (MB Bank)", true);
        dialog.setSize(400, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JLabel lblImage = new JLabel("Đang tạo mã QR thanh toán...", SwingConstants.CENTER);
        dialog.add(lblImage, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnDone = new JButton("Đã chuyển khoản thành công");
        btnDone.setBackground(new Color(40, 167, 69));
        btnDone.setForeground(Color.WHITE);
        btnDone.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        bottomPanel.add(btnDone);
        bottomPanel.add(btnCancel);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnDone.addActionListener(e -> {
            dialog.dispose();
            // Lưu phiếu vào hệ thống sau khi khách bấm xác nhận đã chuyển khoản
            List<PhieuDatSan> listPhieu = new ArrayList<>();
            listPhieu.add(phieu);
            PhieuDatSanDAO phieuDao = new PhieuDatSanDAO();
            if (phieuDao.insertPhieuDatSan(listPhieu)) {
                JOptionPane.showMessageDialog(this, "Thanh toán THÀNH CÔNG!\nPhiếu đặt sân đã được lưu trên hệ thống.");
                // Reset form
                dateChooser.setDate(new java.util.Date());
                cbGioBatDau.setSelectedIndex(0);
                cbGioKetThuc.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu đặt sân, sân có thể đã bị trùng giờ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Tải ảnh QR dưới background bằng SwingWorker để không làm treo phần mềm
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                java.net.URL url = new java.net.URL(urlString);
                java.awt.Image image = javax.imageio.ImageIO.read(url);
                java.awt.Image scaled = image.getScaledInstance(350, 420, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }

            @Override
            protected void done() {
                try {
                    lblImage.setIcon(get());
                    lblImage.setText(""); // Xóa chữ "Đang tạo mã QR..."
                } catch (Exception e) {
                    lblImage.setText("Lỗi: Không thể tải mã QR từ ngân hàng. Kiểm tra kết nối mạng!");
                }
            }
        }.execute();
        
        dialog.setVisible(true);
    }

    // Helper class để hiển thị đẹp trong JComboBox
    class SanBongItem {
        SanBong sanBong;
        public SanBongItem(SanBong sanBong) {
            this.sanBong = sanBong;
        }
        @Override
        public String toString() {
            return sanBong.getTenSan() + " - Loại: " + sanBong.getLoaiSan() + " người (Khu " + sanBong.getKhuVuc() + ")";
        }
    }
}
