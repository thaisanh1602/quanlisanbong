package view.panel;

import dao.NhanVienDAO;
import model.NhanVien;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class QuanLyNhanSuPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Form fields
    private JTextField txtHo, txtTen, txtSDT, txtEmail, txtLuong, txtNgayBatDau;
    private JComboBox<String> cbGioiTinh;
    private JPasswordField txtMatKhau;
    
    // Buttons
    private JButton btnThem, btnCapNhat, btnXoa, btnThoat;
    
    private NhanVienDAO nvDao;
    
    // Trạng thái hiện tại: Đang sửa row hay Đang thêm mới
    private NhanVien selectedNhanVien = null;

    public QuanLyNhanSuPanel() {
        nvDao = new NhanVienDAO();
        setLayout(new BorderLayout());
        
        // --- SplitPane chia 2 khu vực Bảng (Trái) và Form (Phải) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65); // Bảng chiếm 65%, Form 35%
        
        // 1. Panel Bên Trái: Bảng hiển thị nhân viên
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách Nhân Sự"));
        
        String[] columns = {"Mã NV", "Họ", "Tên", "SĐT", "Giới Tính", "Email", "Lương", "Bắt Đầu", "Chức Vụ"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        table = new JTable(tableModel);
        loadDataToTable();
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    fillFormFromSelectedRow(selectedRow);
                    switchMode(false); // Chế độ Sửa (Cập nhật / Xoá)
                }
            }
        });
        
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Căn phải cột Lương (Index 6)
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        
        // 2. Panel Bên Phải: Form thông tin & Ảnh minh hoạ
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // --- Thêm Ảnh Minh Hoạ Ở Trên Cùng ---
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(300, 150));
        try {
            URL imgUrl = getClass().getResource("/images/hr_banner.jpg");
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaled = originalIcon.getImage().getScaledInstance(350, 150, Image.SCALE_SMOOTH);
                JLabel lblImage = new JLabel(new ImageIcon(scaled));
                imagePanel.add(lblImage, BorderLayout.CENTER);
            } else {
                imagePanel.add(new JLabel("Ảnh minh họa (hr_banner.jpg)"), BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.add(imagePanel, BorderLayout.NORTH);
        
        // --- Form nhập liệu bên dưới ảnh ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Nhân Viên"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Khởi tạo fields
        txtHo = new JTextField();
        txtTen = new JTextField();
        txtSDT = new JTextField();
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
        txtEmail = new JTextField();
        txtEmail = new JTextField();
        txtLuong = new JTextField();
        txtNgayBatDau = new JTextField();
        txtNgayBatDau.setToolTipText("yyyy-MM-dd");
        
        // Đưa fields vào form
        int row = 0;
        addFormField(formPanel, gbc, "Họ:", txtHo, row++);
        addFormField(formPanel, gbc, "Tên:", txtTen, row++);
        addFormField(formPanel, gbc, "SĐT:", txtSDT, row++);
        addFormField(formPanel, gbc, "Giới tính:", cbGioiTinh, row++);
        addFormField(formPanel, gbc, "Email:", txtEmail, row++);
        addFormField(formPanel, gbc, "Lương (VND):", txtLuong, row++);
        addFormField(formPanel, gbc, "Ngày bắt đầu:", txtNgayBatDau, row++);
        
        // Panel chứa nút
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnThem = new JButton("Thêm");
        btnCapNhat = new JButton("Cập Nhật");
        btnXoa = new JButton("Xóa");
        btnThoat = new JButton("Trở lại Form Thêm");
        
        btnPanel.add(btnThem);
        btnPanel.add(btnCapNhat);
        btnPanel.add(btnXoa);
        btnPanel.add(btnThoat);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        // Gắn listener cho các nút
        btnThem.addActionListener(e -> actionThem());
        btnCapNhat.addActionListener(e -> actionCapNhat());
        btnXoa.addActionListener(e -> actionXoa());
        btnThoat.addActionListener(e -> switchMode(true)); // Quay lại form trống
        
        // Cuộn form phòng khi màn hình nhỏ
        JScrollPane scrollForm = new JScrollPane(formPanel);
        scrollForm.setBorder(null);
        rightPanel.add(scrollForm, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Khởi động với trạng thái "Thêm mới"
        switchMode(true);
    }
    
    private void addFormField(JPanel p, GridBagConstraints gbc, String labelInfo, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(new JLabel(labelInfo), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
    
    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<NhanVien> list = nvDao.getAllNhanVien();
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        for (NhanVien nv : list) {
            tableModel.addRow(new Object[]{
                nv.getMaNV(), nv.getHo(), nv.getTen(), nv.getSdt(),
                nv.getGioiTinh(), nv.getEmail(), nf.format(nv.getLuong()) + " đ", 
                nv.getNgayBatDau(), nv.getChucVu()
            });
        }
    }
    
    private void fillFormFromSelectedRow(int rowIndex) {
        String maNV = tableModel.getValueAt(rowIndex, 0).toString();
        // Tìm nhân viên trong DB (đoạn này có thể giữ sẵn List local để tra cứu nhanh nhưng ta dùng DB để luôn mới nhất)
        List<NhanVien> list = nvDao.getAllNhanVien();
        list.stream().filter(nv -> nv.getMaNV().equals(maNV)).findFirst().ifPresent(nv -> {
            selectedNhanVien = nv;
            txtHo.setText(nv.getHo());
            txtTen.setText(nv.getTen());
            txtSDT.setText(nv.getSdt());
            cbGioiTinh.setSelectedItem(nv.getGioiTinh());
            txtEmail.setText(nv.getEmail());
            txtEmail.setText(nv.getEmail());
            txtLuong.setText(String.format("%.0f", nv.getLuong())); // Bỏ phần thập phân .0
            txtNgayBatDau.setText(nv.getNgayBatDau() != null ? nv.getNgayBatDau().toString() : "");
        });
    }
    
    private void switchMode(boolean isAddMode) {
        if (isAddMode) {
            selectedNhanVien = null;
            txtHo.setText(""); txtTen.setText(""); txtSDT.setText(""); 
            txtEmail.setText(""); txtLuong.setText("");
            txtNgayBatDau.setText(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            table.clearSelection();
            
            btnThem.setVisible(true);
            btnCapNhat.setVisible(false);
            btnXoa.setVisible(false);
            btnThoat.setVisible(false);
        } else {
            btnThem.setVisible(false);
            btnCapNhat.setVisible(true);
            // Ẩn nút Xóa nếu nhân viên đang chọn có chức vụ là Quản lý
            if (selectedNhanVien != null && "Quản lý".equalsIgnoreCase(selectedNhanVien.getChucVu())) {
                btnXoa.setVisible(false);
            } else {
                btnXoa.setVisible(true);
            }
            btnThoat.setVisible(true);
        }
    }
    
    private NhanVien collectDataFromForm() {
        NhanVien nv = new NhanVien();
        if (selectedNhanVien != null) {
            nv.setMaNV(selectedNhanVien.getMaNV());
            nv.setMaTK(selectedNhanVien.getMaTK()); // ID tài khoản để update bên TaiKhoan table
            nv.setChucVu(selectedNhanVien.getChucVu()); // Giữ nguyên chức vụ không sửa
        }
        
        nv.setHo(txtHo.getText().trim());
        nv.setTen(txtTen.getText().trim());
        nv.setSdt(txtSDT.getText().trim());
        nv.setGioiTinh(cbGioiTinh.getSelectedItem().toString());
        nv.setEmail(txtEmail.getText().trim());
        
        try {
            nv.setLuong(Double.parseDouble(txtLuong.getText().trim()));
            nv.setNgayBatDau(Date.valueOf(txtNgayBatDau.getText().trim()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lương phải là số. Ngày bắt đầu phải đúng định dạng yyyy-MM-dd", "Lỗi format", JOptionPane.ERROR_MESSAGE);
            return null; // Invalid
        }
        return nv;
    }
    
    private void actionThem() {
        NhanVien nv = collectDataFromForm();
        if (nv == null) return; // Validation failed
        
        if (nvDao.insertNhanVien(nv, "123456")) { // Mật khẩu mặc định
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công.");
            loadDataToTable();
            switchMode(true);
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thêm mới. Có thể email đã tồn tại.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actionCapNhat() {
        NhanVien nv = collectDataFromForm();
        if (nv == null) return;
        
        if (nvDao.updateNhanVien(nv, "")) { // Bỏ qua mật khẩu
            JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            loadDataToTable();
            switchMode(true);
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật lỗi.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actionXoa() {
        if (selectedNhanVien == null) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắn chắn muốn xóa nhân viên này (Xoá luôn tài khoản đăng nhập)?", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (nvDao.deleteNhanVien(selectedNhanVien.getMaNV(), selectedNhanVien.getMaTK())) {
                JOptionPane.showMessageDialog(this, "Xóa thành công.");
                loadDataToTable();
                switchMode(true);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu. Do nhân viên đã có lịch đặt / hoá đơn (Khóa ngoại).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
