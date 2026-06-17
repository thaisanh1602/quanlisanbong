package view.panel;

import dao.PhieuDatSanDAO;
import model.PhieuDatSan;
import model.SanBong;
import view.MainFrame;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DatSanPanel extends JPanel {

    private JCheckBox chk5, chk7;
    private JDateChooser dpNgayThue;
    private JComboBox<String> cbGioBatDau;
    private JComboBox<String> cbGioKetThuc;
    private JButton btnTimSan, btnTiepTucDat;
    
    // Bảng sân trống
    private JTable tblSanTrong;
    private DefaultTableModel modelSanTrong;
    
    private PhieuDatSanDAO phieuDao;

    public DatSanPanel() {
        phieuDao = new PhieuDatSanDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Vùng Lọc (Top Filter)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Công cụ Tìm Sân Trống"));
        
        filterPanel.add(new JLabel("Loại sân (Số người):"));
        chk5 = new JCheckBox("5"); chk5.setSelected(true);
        chk7 = new JCheckBox("7");
        filterPanel.add(chk5);
        filterPanel.add(chk7);
        
        filterPanel.add(new JLabel("Ngày thuê:"));
        dpNgayThue = new JDateChooser();
        dpNgayThue.setDate(new java.util.Date());
        dpNgayThue.setDateFormatString("yyyy-MM-dd");
        dpNgayThue.setPreferredSize(new Dimension(150, 26));
        filterPanel.add(dpNgayThue);
        
        filterPanel.add(new JLabel("Từ giờ:"));
        cbGioBatDau = taoComboBoxThoiGian();
        filterPanel.add(cbGioBatDau);
        
        filterPanel.add(new JLabel("Đến giờ:"));
        cbGioKetThuc = taoComboBoxThoiGian();
        cbGioKetThuc.setSelectedItem("07:00");
        filterPanel.add(cbGioKetThuc);
        
        btnTimSan = new JButton("Tìm Kiếm Sân");
        btnTimSan.setBackground(new Color(40, 167, 69));
        btnTimSan.setForeground(Color.WHITE);
        btnTimSan.setFocusPainted(false);
        filterPanel.add(btnTimSan);
        
        add(filterPanel, BorderLayout.NORTH);

        // 2. Vùng Bảng Hiển thị Kết Quả (Center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] cols = {"Chọn", "Mã Sân", "Loại", "Khu Vực", "Tên Sân", "Giá Tiền/Giờ"};
        modelSanTrong = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; // Checkbox để tick chọn nhiều sân
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Chỉ sửa (tick/untick) ô Checkbox
            }
        };
        tblSanTrong = new JTable(modelSanTrong);
        tblSanTrong.setRowHeight(30);
        tblSanTrong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablePanel.add(new JScrollPane(tblSanTrong), BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);

        // 3. Vùng Nút Thanh Toán (Bottom)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnTiepTucDat = new JButton("Tạo Phiếu Đặt Sân (Cho các sân đã tick)");
        btnTiepTucDat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTiepTucDat.setBackground(new Color(0, 123, 255));
        btnTiepTucDat.setForeground(Color.WHITE);
        
        bottomPanel.add(btnTiepTucDat);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // --- Gắn Sự Kiện ---
        btnTimSan.addActionListener(e -> xuLyTimSan());
        btnTiepTucDat.addActionListener(e -> moFormDatSan());
    }

    // Sinh comboBox giới hạn giờ từ 6h đến 22h, bước nhảy 30 phút
    private JComboBox<String> taoComboBoxThoiGian() {
        JComboBox<String> cb = new JComboBox<>();
        LocalTime time = LocalTime.of(6, 0);
        LocalTime endLimit = LocalTime.of(22, 0);
        
        while (!time.isAfter(endLimit)) {
            cb.addItem(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            time = time.plusMinutes(30);
        }
        return cb;
    }

    private void xuLyTimSan() {
        try {
            List<Integer> listLoai = new ArrayList<>();
            if (chk5.isSelected()) listLoai.add(5);
            if (chk7.isSelected()) listLoai.add(7);
            
            if (listLoai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một loại sân để tìm kiếm!", "Lỗi Logic", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.Date dNgay = dpNgayThue.getDate();
            if (dNgay == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc nhập ngày hợp lệ!", "Lỗi Dữ Liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String ngay = new SimpleDateFormat("yyyy-MM-dd").format(dNgay);

            String beginT = cbGioBatDau.getSelectedItem().toString();
            String endT = cbGioKetThuc.getSelectedItem().toString();
            
            // Validate Giờ 
            LocalTime t1 = LocalTime.parse(beginT);
            LocalTime t2 = LocalTime.parse(endT);
            if (!t2.isAfter(t1)) {
                JOptionPane.showMessageDialog(this, "Giờ kết thúc phải lớn hơn Giờ bắt đầu!", "Lỗi Logic", JOptionPane.ERROR_MESSAGE);
                return;
            }

            modelSanTrong.setRowCount(0);
            List<SanBong> results = phieuDao.findAvailableSan(listLoai, ngay, beginT, endT);
            
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rất tiếc, không còn sân nào trống thoả mãn cụm điều kiện này.");
            } else {
                for (SanBong sb : results) {
                    modelSanTrong.addRow(new Object[]{
                        false, // Checkbox false ban đầu
                        sb.getMaSan(),
                        sb.getLoaiSan(),
                        sb.getKhuVuc(),
                        sb.getTenSan(),
                        String.format("%,.0f Đ", sb.getGiaTien())
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(), "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moFormDatSan() {
        List<SanBong> selectedSans = new ArrayList<>();
        for (int i = 0; i < tblSanTrong.getRowCount(); i++) {
            Boolean isChecked = (Boolean) tblSanTrong.getValueAt(i, 0);
            if (isChecked) {
                SanBong sb = new SanBong();
                sb.setMaSan((String) tblSanTrong.getValueAt(i, 1)); // Mã Sân
                sb.setLoaiSan((int) tblSanTrong.getValueAt(i, 2)); // Loại
                selectedSans.add(sb);
            }
        }

        if (selectedSans.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy tích chọn ít nhất 1 sân ở bảng trên để tiến hành đặt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiện Dialog Điền thông tin khách
        hienThiDialogDatSan(selectedSans);
    }

    private void hienThiDialogDatSan(List<SanBong> selectedSans) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xác Nhận Đặt Sân", true);
        dialog.setSize(700, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Form điền Tên, SĐT
        JPanel topPnl = new JPanel(new GridLayout(2, 2, 10, 10));
        topPnl.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        
        JTextField txtHoTen = new JTextField();
        JTextField txtSDT = new JTextField();
        
        topPnl.add(new JLabel("Họ tên Khách Hàng:"));
        topPnl.add(txtHoTen);
        topPnl.add(new JLabel("Số Điện Thoại Khách:"));
        topPnl.add(txtSDT);
        
        dialog.add(topPnl, BorderLayout.NORTH);

        // Bảng xác nhận chi tiết các sân đã chọn
        String[] chiTietCols = {"Mã Sân", "Loại", "Ngày Thuê", "Bắt Đầu", "Kết Thúc", "Tổng Giờ (Duration)"};
        DefaultTableModel chiTietModel = new DefaultTableModel(chiTietCols, 0);
        
        java.util.Date dNgay = dpNgayThue.getDate();
        String ngay = new SimpleDateFormat("yyyy-MM-dd").format(dNgay);
        String beginT = cbGioBatDau.getSelectedItem().toString();
        String endT = cbGioKetThuc.getSelectedItem().toString();
        
        LocalTime t1 = LocalTime.parse(beginT);
        LocalTime t2 = LocalTime.parse(endT);
        float duration = ChronoUnit.MINUTES.between(t1, t2) / 60.0f; // Tính số giờ lẻ (VD 1.5 tiếng)

        for (SanBong sb : selectedSans) {
            chiTietModel.addRow(new Object[]{
                sb.getMaSan(),
                sb.getLoaiSan(),
                ngay,
                beginT,
                endT,
                duration
            });
        }
        
        JTable tblChiTiet = new JTable(chiTietModel);
        dialog.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        // Nút chốt đơn
        JButton btnChot = new JButton("XÁC NHẬN CHỐT ĐẶT SÂN");
        btnChot.setBackground(new Color(220, 53, 69)); // Màu đỏ
        btnChot.setForeground(Color.WHITE);
        btnChot.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        btnChot.addActionListener(ev -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSDT.getText().trim();
            if (hoTen.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Tuyệt đối không để trống Tên Cuộc Cú Điện Thoại (SĐT) Khách.");
                return;
            }

            // Tiến hành ghi vào CSDL Data List các Phiếu
            List<PhieuDatSan> phieus = new ArrayList<>();
            for (SanBong sb : selectedSans) {
                PhieuDatSan pds = new PhieuDatSan(
                    0, hoTen, sdt, sb.getMaSan(), Date.valueOf(ngay),
                    Time.valueOf(beginT + ":00"), Time.valueOf(endT + ":00"), duration, 0, 0
                );
                phieus.add(pds);
            }

            if (phieuDao.insertPhieuDatSan(phieus)) {
                JOptionPane.showMessageDialog(dialog, "Đặt sân thành công! Chuyển đến bảng thanh toán.");
                dialog.dispose();
                
                // Tìm MainFrame và chuyển sang bảng Lịch Đặt Sân
                Window parent = SwingUtilities.getWindowAncestor(this);
                if (parent instanceof MainFrame) {
                    ((MainFrame) parent).showLichDatSan();
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi Server - Lưu thất bại.");
            }
        });

        JPanel botPnl = new JPanel();
        botPnl.add(btnChot);
        dialog.add(botPnl, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
