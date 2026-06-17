package view.panel;

import dao.SanBongDAO;
import model.SanBong;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class XemBangSanPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private JTable tblDanhSachSan;
    private DefaultTableModel modelDanhSachSan;
    private JTable tblSanBaoTri;
    private DefaultTableModel modelSanBaoTri;
    private SanBongDAO sbDao;

    public XemBangSanPanel() {
        sbDao = new SanBongDAO();
        setLayout(new BorderLayout());

        // Sidebar dạng TabbedPane nằm ngang hoặc dọc. Ở đây dùng LEFT
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Khởi tạo 2 panel con
        initTabDanhSachSan();
        initTabSanBaoTri();

        // Gắn sự kiện chuyển tab để làm mới dữ liệu
        tabbedPane.addChangeListener(e -> refreshData());

        add(tabbedPane, BorderLayout.CENTER);

        // Load data lần đầu
        refreshData();
    }

    private void initTabDanhSachSan() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Khối chứa nút chức năng (ví dụ Thêm sân)
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnThemSan = new JButton("Thêm Sân Mới");
        btnThemSan.setBackground(new Color(40, 167, 69));
        btnThemSan.setForeground(Color.WHITE);
        btnThemSan.addActionListener(e -> hienThiFormThemSan());
        toolsPanel.add(btnThemSan);
        pnl.add(toolsPanel, BorderLayout.NORTH);

        String[] cols = {"Mã Sân", "Loại Sân", "Khu Vực", "Tên Sân", "Bảo Trì", "Xóa"};
        modelDanhSachSan = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // Chỉ cho phép click ở cột nút
            }
        };
        tblDanhSachSan = new JTable(modelDanhSachSan);
        tblDanhSachSan.setRowHeight(35);
        tblDanhSachSan.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Cài đặt nút
        tblDanhSachSan.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Đưa vào bảo trì"));
        tblDanhSachSan.getColumnModel().getColumn(4).setCellEditor(new ButtonEditorDanhSach(new JCheckBox(), true));
        
        tblDanhSachSan.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer("Xóa"));
        tblDanhSachSan.getColumnModel().getColumn(5).setCellEditor(new ButtonEditorDanhSach(new JCheckBox(), false));

        pnl.add(new JScrollPane(tblDanhSachSan), BorderLayout.CENTER);
        tabbedPane.addTab("Danh sách sân (Khả dụng)", pnl);
    }

    private void initTabSanBaoTri() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã Sân", "Loại Sân", "Khu Vực", "Tên Sân", "Bảo Trì"};
        modelSanBaoTri = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Cột cho phép click nút
            }
        };
        tblSanBaoTri = new JTable(modelSanBaoTri);
        tblSanBaoTri.setRowHeight(35);
        tblSanBaoTri.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Nút Khôi phục nằm ở cột Bảo Trì 
        tblSanBaoTri.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("Hủy bảo trì"));
        tblSanBaoTri.getColumnModel().getColumn(4).setCellEditor(new ButtonEditorBaoTri(new JCheckBox()));

        pnl.add(new JScrollPane(tblSanBaoTri), BorderLayout.CENTER);
        tabbedPane.addTab("Sân bảo trì", pnl);
    }

    // Refresh toàn bộ Data
    private void refreshData() {
        // Table Danh Sách Sân Khả dụng (Status = 0)
        modelDanhSachSan.setRowCount(0);
        List<SanBong> listAvailable = sbDao.getSanBongByTrangThai(0);
        for (SanBong sb : listAvailable) {
            modelDanhSachSan.addRow(new Object[]{
                sb.getMaSan(),
                sb.getLoaiSan() + " Người",
                sb.getKhuVuc(),
                sb.getTenSan(),
                "Bảo trì",
                "Xóa"
            });
        }

        // Table Sân Bảo trì (Status = 1)
        modelSanBaoTri.setRowCount(0);
        List<SanBong> listMaintenance = sbDao.getSanBongByTrangThai(1);
        for (SanBong sb : listMaintenance) {
            modelSanBaoTri.addRow(new Object[]{
                sb.getMaSan(),
                sb.getLoaiSan() + " Người",
                sb.getKhuVuc(),
                sb.getTenSan(),
                "Hủy bảo trì / Mở lại sân"
            });
        }
    }

    private void hienThiFormThemSan() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Sân Bóng Mới", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel pnl = new JPanel(new GridLayout(6, 2, 10, 10));
        pnl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField txtMaSan = new JTextField();
        txtMaSan.setEditable(false);
        txtMaSan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtMaSan.setForeground(Color.RED);
        
        JTextField txtTenSan = new JTextField();
        txtTenSan.setEditable(false);
        txtTenSan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTenSan.setForeground(Color.BLUE);
        
        JComboBox<Integer> cbLoaiSan = new JComboBox<>(new Integer[]{5, 7});
        
        // Auto Generate Logic
        Runnable renderAutoIds = () -> {
            int loai = (Integer) cbLoaiSan.getSelectedItem();
            int count = sbDao.countSanBongByLoai(loai);
            int thuTu = count + 1;
            
            String maPrefix = "S" + (loai < 10 ? "0" + loai : loai) + "_";
            String maSuffix = (thuTu < 10 ? "0" + thuTu : String.valueOf(thuTu));
            txtMaSan.setText(maPrefix + maSuffix);
            
            txtTenSan.setText("Sân " + loai + " - Số " + thuTu);
        };
        cbLoaiSan.addActionListener(e -> renderAutoIds.run());
        renderAutoIds.run(); // Render cấu hình mặc định (Loại 5) ban đầu

        JTextField txtKhuVuc = new JTextField();
        JTextField txtGiaTien = new JTextField();
        
        pnl.add(new JLabel("Mã Sân:")); pnl.add(txtMaSan);
        pnl.add(new JLabel("Tên Sân:")); pnl.add(txtTenSan);
        pnl.add(new JLabel("Loại Sân (Số lượng người):")); pnl.add(cbLoaiSan);
        pnl.add(new JLabel("Khu Vực (VD: Sân A, Sân TR1):")); pnl.add(txtKhuVuc);
        pnl.add(new JLabel("Giá Tiền 1 Giờ Khung Cơ Sở:")); pnl.add(txtGiaTien);
        
        JButton btnLuu = new JButton("Lưu Dữ Liệu Sân");
        btnLuu.setBackground(new Color(0, 123, 255));
        btnLuu.setForeground(Color.WHITE);
        
        pnl.add(new JLabel()); // Khoảng trống
        pnl.add(btnLuu);
        
        btnLuu.addActionListener(ev -> {
            try {
                String ma = txtMaSan.getText().trim();
                String ten = txtTenSan.getText().trim();
                if (ma.isEmpty() || ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Mã sân và Tên sân không được để trống!");
                    return;
                }
                
                SanBong sb = new SanBong(
                    ma, ten, (Integer) cbLoaiSan.getSelectedItem(),
                    txtKhuVuc.getText().trim(), Double.parseDouble(txtGiaTien.getText().trim()), 0
                );
                
                if (sbDao.insertSanBong(sb)) {
                    JOptionPane.showMessageDialog(dialog, "Thêm sân bóng thành công!");
                    dialog.dispose();
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thêm thất bại. Có thể Mã Sân đã tồn tại.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi Nhập Liệu! (Giá tiền phải là số)", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        dialog.add(pnl, BorderLayout.CENTER);
        dialog.setVisible(true);
    }


    /* --- CLASS HỖ TRỢ VẼ NÚT TRÊN BẢNG --- */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setOpaque(true);
            setText(text);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // value is the string of the button set by DefaultTableModel
            if (value != null) {
                setText(value.toString());
            }
            if ("Xóa".equals(value)) {
                setBackground(Color.RED);
                setForeground(Color.WHITE);
            } else if (getText().contains("Hủy")) {
                setBackground(new Color(40, 167, 69)); // Xanh lá
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(255, 193, 7)); // Vàng (Warning) cho Bảo trì
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    /* --- CLASS SỰ KIỆN NÚT CHO BẢNG: DANH SÁCH SÂN KHẢ DỤNG --- */
    class ButtonEditorDanhSach extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isBaoTriBtn;
        private String maSanClick;

        public ButtonEditorDanhSach(JCheckBox checkBox, boolean isBaoTriBtn) {
            super(checkBox);
            this.isBaoTriBtn = isBaoTriBtn;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            maSanClick = table.getValueAt(row, 0).toString(); // Cột 0 là chuỗi Mã Sân
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isBaoTriBtn) {
                int confirm = JOptionPane.showConfirmDialog(button, "Đưa sân " + maSanClick + " vào trạng thái bảo trì?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    sbDao.toggleTrangThai(maSanClick, 1);
                    SwingUtilities.invokeLater(() -> refreshData()); // Trì hoãn refresh để kết thúc edit cell
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(button, "Chắn chắn xóa sân " + maSanClick + "?\nCác dữ liệu liên quan cũng có thể bị mất.", "Cảnh báo", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (sbDao.deleteSanBong(maSanClick)) {
                        JOptionPane.showMessageDialog(button, "Đã xóa sân thành công.");
                    } else {
                        JOptionPane.showMessageDialog(button, "Xóa thất bại (Sân đang có lịch đặt chờ thanh toán).", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                    }
                    SwingUtilities.invokeLater(() -> refreshData());
                }
            }
            return label; // Giữ nguyên dòng Text cũ để tránh lỗi vẽ
        }
    }

    /* --- CLASS SỰ KIỆN NÚT CHO BẢNG: SÂN BẢO TRÌ --- */
    class ButtonEditorBaoTri extends DefaultCellEditor {
        private JButton button;
        private String label;
        private String maSanClick;

        public ButtonEditorBaoTri(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            maSanClick = table.getValueAt(row, 0).toString();
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            int confirm = JOptionPane.showConfirmDialog(button, "Mở lại trạng thái KHẢ DỤNG cho sân " + maSanClick + "?", "Khôi phục", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sbDao.toggleTrangThai(maSanClick, 0);
                SwingUtilities.invokeLater(() -> refreshData());
            }
            return label;
        }
    }
}
