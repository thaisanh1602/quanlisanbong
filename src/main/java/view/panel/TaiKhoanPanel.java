package view.panel;

import dao.TaiKhoanDAO;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class TaiKhoanPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Form fields
    private JTextField txtTenDangNhap;
    private JTextField txtMatKhau;
    private JComboBox<String> cbLoaiTK;
    
    // Buttons
    private JButton btnThem, btnCapNhat, btnXoa, btnThoat;
    
    private TaiKhoanDAO tkDao;
    
    private TaiKhoan selectedTaiKhoan = null;

    public TaiKhoanPanel() {
        tkDao = new TaiKhoanDAO();
        setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        
        // --- Bảng Hiển Thị (Trái) ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách Tài Khoản Hệ Thống"));
        
        String[] columns = {"Mã TK", "Tên Đăng Nhập", "Mật Khẩu (Bcrypt Hash)", "Loại Tài Khoản"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        loadDataToTable();
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    fillFormFromSelectedRow(selectedRow);
                    switchMode(false);
                }
            }
        });
        
        leftPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // --- Form Xử Lý (Phải) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Quản lý thông tin Tài khoản"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        txtTenDangNhap = new JTextField();
        txtMatKhau = new JTextField();
        txtMatKhau.setToolTipText("Mật khẩu gốc (Chưa mã hoá) - Chỉ điền khi thêm/đổi");
        cbLoaiTK = new JComboBox<>(new String[]{"Quản lý", "Nhân viên"});
        
        int row = 0;
        addFormField(formPanel, gbc, "Tên Đăng Nhập:", txtTenDangNhap, row++);
        addFormField(formPanel, gbc, "Mật khẩu:", txtMatKhau, row++);
        addFormField(formPanel, gbc, "Loại Tài Khoản:", cbLoaiTK, row++);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnThem = new JButton("Thêm Mới");
        btnCapNhat = new JButton("Cập Nhật");
        btnXoa = new JButton("Xóa");
        btnThoat = new JButton("Huỷ chọn");
        
        btnThem.setBackground(new Color(40, 167, 69)); btnThem.setForeground(Color.WHITE);
        btnCapNhat.setBackground(new Color(0, 123, 255)); btnCapNhat.setForeground(Color.WHITE);
        btnXoa.setBackground(new Color(220, 53, 69)); btnXoa.setForeground(Color.WHITE);
        
        btnPanel.add(btnThem);
        btnPanel.add(btnCapNhat);
        btnPanel.add(btnXoa);
        btnPanel.add(btnThoat);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);
        
        rightPanel.add(formPanel, BorderLayout.NORTH); // Đẩy lên trên cho gọn
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Sự kiện
        btnThem.addActionListener(e -> actionThem());
        btnCapNhat.addActionListener(e -> actionCapNhat());
        btnXoa.addActionListener(e -> actionXoa());
        btnThoat.addActionListener(e -> switchMode(true));
        
        switchMode(true);
    }
    
    private void addFormField(JPanel p, GridBagConstraints gbc, String label, Component comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
    
    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<TaiKhoan> list = tkDao.getAllTaiKhoan();
        for (TaiKhoan tk : list) {
            tableModel.addRow(new Object[]{
                tk.getMaTK(), tk.getTenDangNhap(), "******** (Đã mã hoá 1 chiều)", tk.getLoaiTK()
            });
        }
    }
    
    private void fillFormFromSelectedRow(int rowIndex) {
        int maTK = (int) tableModel.getValueAt(rowIndex, 0);
        List<TaiKhoan> list = tkDao.getAllTaiKhoan();
        for (TaiKhoan tk : list) {
            if (tk.getMaTK() == maTK) {
                selectedTaiKhoan = tk;
                txtTenDangNhap.setText(tk.getTenDangNhap());
                txtMatKhau.setText("");
                cbLoaiTK.setSelectedItem(tk.getLoaiTK());
                break;
            }
        }
    }
    
    private void switchMode(boolean isAdd) {
        if (isAdd) {
            selectedTaiKhoan = null;
            txtTenDangNhap.setText("");
            txtMatKhau.setText("");
            cbLoaiTK.setSelectedIndex(1);
            table.clearSelection();
            
            btnThem.setVisible(true);
            btnCapNhat.setVisible(false);
            btnXoa.setVisible(false);
            btnThoat.setVisible(false);
        } else {
            btnThem.setVisible(false);
            btnCapNhat.setVisible(true);
            btnXoa.setVisible(true);
            btnThoat.setVisible(true);
        }
    }
    
    private void actionThem() {
        String user = txtTenDangNhap.getText().trim();
        String pass = txtMatKhau.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập và Mật khẩu không được rỗng!");
            return;
        }
        
        TaiKhoan tk = new TaiKhoan(0, null, user, "", cbLoaiTK.getSelectedItem().toString());
        if (tkDao.insertTaiKhoan(tk, pass)) {
            JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công!");
            loadDataToTable();
            switchMode(true);
        } else {
            JOptionPane.showMessageDialog(this, "Tài khoản có thể bị trùng, thêm thất bại!");
        }
    }
    
    private void actionCapNhat() {
        if (selectedTaiKhoan == null) return;
        String user = txtTenDangNhap.getText().trim();
        String pass = txtMatKhau.getText();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không rỗng!");
            return;
        }
        
        selectedTaiKhoan.setTenDangNhap(user);
        selectedTaiKhoan.setLoaiTK(cbLoaiTK.getSelectedItem().toString());
        
        if (tkDao.updateTaiKhoan(selectedTaiKhoan, pass)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
            switchMode(true);
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
        }
    }
    
    private void actionXoa() {
        if (selectedTaiKhoan == null) return;
        
        // Cấm tự xoá chính tài khoản đang đăng nhập
        if (view.MainFrame.currentUser != null && selectedTaiKhoan.getMaTK() == view.MainFrame.currentUser.getMaTK()) {
            JOptionPane.showMessageDialog(this, "Không thể tự thao tác xoá trên tài khoản đang được đăng nhập!", "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int config = JOptionPane.showConfirmDialog(this, "Chắn chắn xoá tài khoản: " + selectedTaiKhoan.getTenDangNhap() + "?");
        if (config == JOptionPane.YES_OPTION) {
            if (tkDao.deleteTaiKhoan(selectedTaiKhoan.getMaTK())) {
                JOptionPane.showMessageDialog(this, "Xoá thành công!");
                loadDataToTable();
                switchMode(true);
            } else {
                JOptionPane.showMessageDialog(this, "Tài khoản đang bị ràng buộc khoá ngoại (NhanVien), xoá thất bại!");
            }
        }
    }
}
