package view.panel;

import dao.HoaDonDAO;
import dao.PhieuDatSanDAO;
import model.HoaDon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import util.InvoicePrinter;

public class LichDatSanPanel extends JPanel {

    private JRadioButton rbThanhToanDon;
    private JRadioButton rbThanhToanTheoPhieu;
    private JButton btnHuyDat;

    private JTable table;
    private DefaultTableModel tableModel;

    private PhieuDatSanDAO phieuDao;
    private HoaDonDAO hoaDonDao;

    // Local data cache để bóc tách dữ liệu khi click
    private List<Object[]> currentDataList;

    public LichDatSanPanel() {
        phieuDao = new PhieuDatSanDAO();
        hoaDonDao = new HoaDonDAO();
        currentDataList = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. Top Panel (Công cụ) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Công cụ Lịch Đặt Sân"));

        rbThanhToanDon = new JRadioButton("Thanh toán đơn", true);
        rbThanhToanTheoPhieu = new JRadioButton("Thanh toán theo phiếu");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbThanhToanDon);
        bg.add(rbThanhToanTheoPhieu);

        btnHuyDat = new JButton("Hủy Đặt Sân (Bỏ chọn)");

        topPanel.add(rbThanhToanDon);
        topPanel.add(rbThanhToanTheoPhieu);


        JButton btnThanhToanChung = new JButton("Tìm Phiếu");
        btnThanhToanChung.setBackground(new Color(255, 193, 7)); // Yellow
        btnThanhToanChung.addActionListener(e -> xuLyThanhToanTheoPhieuTimKiem());
        
        topPanel.add(btnThanhToanChung);
        topPanel.add(btnHuyDat);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Panel (Bảng) ---
        String[] columns = {"Khách hàng", "SĐT", "Mã Sân", "Vào (Bắt đầu)", "Ra (Kết thúc)", "Giá Tiền/h", "Thanh Toán"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Chỉ cho phép ô "Thanh toán" là click được
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Cài đặt nút Thanh toán từng dòng (phù hợp "Thanh toán đơn")
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRendererTToan());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditorTToan(new JCheckBox()));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Events
        btnHuyDat.addActionListener(e -> xulyHuyDatSan());

        refreshTableData(null, null);
    }

    public void refreshTableData(String sdt, String ngayLap) {
        tableModel.setRowCount(0);
        currentDataList = phieuDao.getPhieuChuaThanhToan(sdt, ngayLap, null);

        for (Object[] obj : currentDataList) {
            String khach = (String) obj[1];
            String phone = (String) obj[2];
            String masan = (String) obj[3];
            Date ngay = (Date) obj[5];
            Time batDau = (Time) obj[6];
            Time ketThuc = (Time) obj[7];
            double gia = (double) obj[9];

            String timeVao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.valueOf(ngay.toString() + " " + batDau.toString()));
            String timeRa = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.valueOf(ngay.toString() + " " + ketThuc.toString()));

            tableModel.addRow(new Object[]{
                khach, phone, masan, timeVao, timeRa, String.format("%,.0f", gia), "THANH TOÁN"
            });
        }
    }

    private void xulyHuyDatSan() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hàng để huỷ!");
            return;
        }
        int maPhieu = (int) currentDataList.get(row)[0];
        int confirm = JOptionPane.showConfirmDialog(this, "Chắc chắn hủy lịch đặt này?", "Hủy", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (phieuDao.deletePhieuDatSan(maPhieu)) {
                refreshTableData(null, null);
            }
        }
    }

    // Nút "Thanh Toán Theo Phiếu": Mở Form Tìm Phiếu -> Ra Form Thanh Toán
    private void xuLyThanhToanTheoPhieuTimKiem() {
        if (rbThanhToanDon.isSelected()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn '[x] Thanh toán theo phiếu' hoặc click dể thanh toán đơn ở Bảng bên dưới.");
            return;
        }

        JDialog searchDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Form Tìm Phiếu", true);
        searchDialog.setSize(400, 150);
        searchDialog.setLocationRelativeTo(this);
        searchDialog.setLayout(new BorderLayout());

        JPanel searchPnl = new JPanel(new GridLayout(1, 2, 5, 15));
        searchPnl.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JTextField txtSdt = new JTextField();
        
        searchPnl.add(new JLabel("SĐT Khách hàng:")); searchPnl.add(txtSdt);

        searchDialog.add(searchPnl, BorderLayout.CENTER);

        JPanel botPnl = new JPanel();
        JButton btnSubmit = new JButton("Tìm Kiếm");
        btnSubmit.addActionListener(ev -> {
            String sdt = txtSdt.getText().trim();
            if (sdt.isEmpty()) {
                JOptionPane.showMessageDialog(searchDialog, "Vui lòng nhập SĐT khách hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            List<Object[]> queryList = phieuDao.getPhieuChuaThanhToan(sdt, null, null);
            if (queryList.isEmpty()) {
                JOptionPane.showMessageDialog(searchDialog, "Không tìm thấy phiếu nào chưa thanh toán cho SĐT: " + sdt);
            } else {
                searchDialog.dispose();
                hienThiFormThanhToan(queryList); 
            }
        });
        botPnl.add(btnSubmit);
        searchDialog.add(botPnl, BorderLayout.SOUTH);

        searchDialog.setVisible(true);
    }

    /**
     * Mở form Hoá đơn (Hỗ trợ n dòng phiếu - tuỳ vào Thanh Toán Theo Phiếu hay Theo Đơn)
     */
    private void hienThiFormThanhToan(List<Object[]> listToPay) {
        if (listToPay.isEmpty()) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xử Lý Thanh Toán Hóa Đơn", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // Dữ liệu khách bóc từ dòng đầu tiên
        Object[] firstObj = listToPay.get(0);
        String sdtKhach = (String) firstObj[2];
        
        // --- TOP: Mã hóa đơn, Ngày lập, SĐT... (Readonly) ---
        JPanel topPnl = new JPanel(new GridLayout(2, 4, 10, 10));
        topPnl.setBorder(BorderFactory.createTitledBorder("Thông tin Hóa Đơn"));
        
        topPnl.add(new JLabel("Mã Hóa Đơn:")); 
        JTextField txtMaHD = new JTextField("Tự động hệ thống"); txtMaHD.setEditable(false); topPnl.add(txtMaHD);
        
        topPnl.add(new JLabel("Ngày tạo:")); 
        JTextField txtNgayLap = new JTextField(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())); txtNgayLap.setEditable(false); topPnl.add(txtNgayLap);
        
        topPnl.add(new JLabel("SĐT Khách:")); 
        JTextField txtSDT = new JTextField(sdtKhach); txtSDT.setEditable(false); topPnl.add(txtSDT);
        
        dialog.add(topPnl, BorderLayout.NORTH);

        // --- CENTER: Bảng Chi tiết Thanh Toán ---
        String[] cols = {"Mã Sân", "Loại", "Thuê lúc", "B.Đầu", "K.Thúc", "Thời gian(h)", "Đơn giá", "Thành tiền"};
        DefaultTableModel hdModel = new DefaultTableModel(cols, 0);
        double tongTienSan = 0;

        List<Integer> listMaPhieu = new ArrayList<>();

        for (Object[] obj : listToPay) {
            listMaPhieu.add((int) obj[0]); // maPhieu
            String masan = (String) obj[3];
            int loai = (int) obj[4];
            Date ngay = (Date) obj[5];
            Time gb = (Time) obj[6];
            Time gk = (Time) obj[7];
            float dur = (float) obj[8];
            double gia = (double) obj[9];
            double thanhTien = dur * gia;
            tongTienSan += thanhTien;

            hdModel.addRow(new Object[]{masan, loai, ngay.toString(), gb.toString(), gk.toString(), dur, gia, thanhTien});
        }

        dialog.add(new JScrollPane(new JTable(hdModel)), BorderLayout.CENTER);

        // --- BOTTOM: Các ô tính toán tiền tệ ---
        JPanel botPnl = new JPanel(new BorderLayout());
        JPanel calcPnl = new JPanel(new GridLayout(3, 4, 10, 10));
        calcPnl.setBorder(BorderFactory.createTitledBorder("Tổng kết"));

        calcPnl.add(new JLabel("Giảm giá (%):"));
        JTextField txtGiamGia = new JTextField("0"); calcPnl.add(txtGiamGia);
        
        calcPnl.add(new JLabel("Thuế VAT (%):"));
        JTextField txtThue = new JTextField("5"); txtThue.setEditable(false); calcPnl.add(txtThue);
        
        calcPnl.add(new JLabel("Tổng Tiến (VNĐ):"));
        JTextField txtTongTien = new JTextField(); txtTongTien.setEditable(false); 
        txtTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14)); txtTongTien.setForeground(Color.RED);
        calcPnl.add(txtTongTien);
        
        calcPnl.add(new JLabel("Số Tiền Nhận (VNĐ):"));
        JTextField txtTienNhan = new JTextField("0"); calcPnl.add(txtTienNhan);
        
        calcPnl.add(new JLabel("Trả Lại Khách (VNĐ):"));
        JTextField txtTraLai = new JTextField(); txtTraLai.setEditable(false); calcPnl.add(txtTraLai);

        // Hàm ẩn Helper tính toán tiền
        double finalTongTienSan = tongTienSan;
        Runnable tinhTien = () -> {
            try {
                int giam = Integer.parseInt(txtGiamGia.getText().trim());
                double tienSauThue = finalTongTienSan * 1.05;
                double tongTien = tienSauThue * (1 - giam / 100.0);
                txtTongTien.setText(String.format("%.0f", tongTien));
                
                double nhan = Double.parseDouble(txtTienNhan.getText().trim());
                txtTraLai.setText(String.format("%.0f", nhan - tongTien));
            } catch (Exception ex) {}
        };

        txtGiamGia.getDocument().addDocumentListener(new BasicDocListener(tinhTien));
        txtTienNhan.getDocument().addDocumentListener(new BasicDocListener(tinhTien));
        tinhTien.run();

        botPnl.add(calcPnl, BorderLayout.CENTER);

        JButton btnChot = new JButton("THANH TOÁN HÓA ĐƠN");
        btnChot.setBackground(new Color(40, 167, 69));
        btnChot.setForeground(Color.WHITE);
        btnChot.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        btnChot.addActionListener(e -> {
            HoaDon hd = new HoaDon(
                0, new Timestamp(System.currentTimeMillis()), sdtKhach,
                finalTongTienSan, Integer.parseInt(txtGiamGia.getText()), 5,
                Double.parseDouble(txtTongTien.getText()), Double.parseDouble(txtTienNhan.getText()), Double.parseDouble(txtTraLai.getText())
            );

            if (hd.getSoTienNhan() < hd.getTongThanhToan()) {
                JOptionPane.showMessageDialog(dialog, "Tiền nhận chưa đủ. Vui lòng kiểm tra lại!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Lưu vào CSDL trước
            if (hoaDonDao.taoHoaDonVaThanhToan(hd, listMaPhieu)) {
                // 2. Nếu thành công, hỏi in hóa đơn
                int print = JOptionPane.showConfirmDialog(dialog, "Thanh toán thành công! Bạn có muốn in hóa đơn không?", "Thành Công", JOptionPane.YES_NO_OPTION);
                if (print == JOptionPane.YES_OPTION) {
                    InvoicePrinter.printInvoice(hd, listToPay);
                }
                
                // 3. Đóng dialog và refresh bảng
                dialog.dispose();
                refreshTableData(null, null);
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi lưu dữ liệu vào CSDL. Vui lòng thử lại!", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        botPnl.add(btnChot, BorderLayout.SOUTH);
        dialog.add(botPnl, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /* ------- CLASS HỔ TRỢ VẼ NÚT BẢNG ------- */
    class ButtonRendererTToan extends JButton implements TableCellRenderer {
        public ButtonRendererTToan() {
            setOpaque(true);
            setText("THANH TOÁN");
            setBackground(new Color(0, 123, 255));
            setForeground(Color.WHITE);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditorTToan extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;

        public ButtonEditorTToan(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("THANH TOÁN...");
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            clickedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            SwingUtilities.invokeLater(() -> {
                // Check Radio "Đơn" hay "Phiếu"
                if (rbThanhToanDon.isSelected()) {
                    List<Object[]> payList = new ArrayList<>();
                    payList.add(currentDataList.get(clickedRow));
                    hienThiFormThanhToan(payList);
                } else {
                    JOptionPane.showMessageDialog(button, "Bạn đang chọn chế độ 'Thanh toán theo phiếu'. Hãy nhấn nút 'Thanh Toán Bằng Tìm Kiếm' màu vàng ở trên.");
                }
            });
            return "THANH TOÁN";
        }
    }

    // Helper rút gọn cho DocumentListener
    class BasicDocListener implements DocumentListener {
        Runnable r;
        public BasicDocListener(Runnable r) { this.r = r; }
        @Override public void insertUpdate(DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(DocumentEvent e) { r.run(); }
    }
}
