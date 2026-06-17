package view.panel;

import dao.PhieuDatSanDAO;
import view.KhachHangMainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KhachHangLichDatPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public KhachHangLichDatPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("LỊCH SỬ ĐẶT SÂN CỦA TÔI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(33, 37, 41));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(0, 123, 255));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã Phiếu", "Mã Sân", "Ngày Thuê", "Giờ Bắt Đầu", "Giờ Kết Thúc", "Tổng Tiền", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        if (KhachHangMainFrame.currentUser == null) return;
        
        String sdt = KhachHangMainFrame.currentUser.getSdt();
        if (sdt == null || sdt.trim().isEmpty()) {
            sdt = KhachHangMainFrame.currentUser.getTenDangNhap();
        }
        PhieuDatSanDAO dao = new PhieuDatSanDAO();
        List<Object[]> history = dao.getPhieuBySDT(sdt);
        
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        for (Object[] row : history) {
            // Định dạng lại tiền tệ
            double tongTien = (double) row[5];
            row[5] = format.format(tongTien);
            tableModel.addRow(row);
        }
    }
}
