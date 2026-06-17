package view.panel;

import dao.HoaDonDAO;
import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class  BaoCaoPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private HoaDonDAO hoaDonDao;

    // Tab Chi tiết – giữ ref để refresh
    private DefaultTableModel detailModel;
    private JLabel lblTongDoanhThu;
    private JLabel lblSoHoaDon;
    private JDateChooser dpChonNgay;

    // Tab Thống kê số lượng
    private JDateChooser dpTuNgay;
    private JDateChooser dpDenNgay;
    private JLabel lblSlSan5;
    private JLabel lblSlSan7;

    public BaoCaoPanel() {
        hoaDonDao = new HoaDonDAO();
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Biểu đồ Doanh Thu", buildBieuDoTab());
        tabbedPane.addTab("Chi tiết Thanh Toán", buildChiTietTab());
        tabbedPane.addTab("Thống kê Đặt Sân", buildThongKeDatSanTab());

        // Refresh khi chuyển tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                refreshBieuDo();
            } else if (tabbedPane.getSelectedIndex() == 1) {
                loadChiTiet();
            } else {
                loadThongKeDatSan();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1: BIỂU ĐỒ
    // ══════════════════════════════════════════════════════════
    private JPanel bieuDoContainer;

    private JPanel buildBieuDoTab() {
        bieuDoContainer = new JPanel(new BorderLayout());
        bieuDoContainer.setBorder(new EmptyBorder(16, 16, 16, 16));
        refreshBieuDo();
        return bieuDoContainer;
    }

    private void refreshBieuDo() {
        bieuDoContainer.removeAll();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = hoaDonDao.getDoanhThu30NgayGanNhat();

        for (Object[] row : data) {
            String ngay = (String) row[0];
            double tong = (double) row[1];
            // Hiển thị dd/MM
            String label = ngay.length() == 10 ? ngay.substring(8) + "/" + ngay.substring(5, 7) : ngay;
            dataset.addValue(tong, "Doanh thu", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "DOANH THU 30 NGÀY GẦN NHẤT",
                "Ngày", "Doanh thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Tùy chỉnh giao diện biểu đồ
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI" , Font.BOLD, 16));
        chart.getTitle().setPaint(new Color(20, 50, 90));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 255));
        plot.setDomainGridlinePaint(new Color(210, 215, 225));
        plot.setRangeGridlinePaint(new Color(210, 215, 225));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(23, 80, 160));
        renderer.setMaximumBarWidth(0.05);
        renderer.setShadowVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 420));

        // Nhãn tóm tắt
        double tongAll = data.stream().mapToDouble(r -> (double) r[1]).sum();
        JLabel summary = new JLabel(String.format(
                "  Tổng doanh thu 30 ngày: %s VNĐ  |  Số ngày có giao dịch: %d ngày",
                NumberFormat.getNumberInstance(new Locale("vi", "VN")).format((long) tongAll),
                data.size()
        ));
        summary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summary.setForeground(new Color(20, 90, 20));
        summary.setBorder(new EmptyBorder(10, 8, 4, 8));

        bieuDoContainer.add(chartPanel, BorderLayout.CENTER);
        bieuDoContainer.add(summary, BorderLayout.SOUTH);
        bieuDoContainer.revalidate();
        bieuDoContainer.repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2: CHI TIẾT
    // ══════════════════════════════════════════════════════════
    private JPanel buildChiTietTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ── Toolbar lọc ──────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolbar.setBorder(BorderFactory.createTitledBorder("🔍  Lọc theo Ngày Thanh Toán"));

        dpChonNgay = new JDateChooser();
        dpChonNgay.setDateFormatString("dd/MM/yyyy");
        dpChonNgay.setPreferredSize(new Dimension(140, 28));
        // Mặc định: không chọn ngày = hiển thị tất cả

        JButton btnLoc = new JButton("Lọc");
        btnLoc.setBackground(new Color(23, 80, 160));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setFocusPainted(false);
        btnLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnTatCa = new JButton("Tất cả");
        btnTatCa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTatCa.setToolTipText("Xóa bộ lọc, hiển thị toàn bộ lịch sử");

        toolbar.add(new JLabel("Chọn ngày:"));
        toolbar.add(dpChonNgay);
        toolbar.add(btnLoc);
        toolbar.add(btnTatCa);

        // ── Bảng dữ liệu ─────────────────────────────────────
        String[] cols = {"Mã Phiếu", "Tên Khách Hàng", "SĐT", "Mã Sân",
                         "Ngày Thuê", "Giờ Vào", "Giờ Ra", "Số Giờ", "Đơn Giá", "Thành Tiền"};
        detailModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(detailModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(23, 55, 100));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220, 225, 235));
        table.setSelectionBackground(new Color(210, 225, 245));

        // Căn phải cột tiền (cột 8, 9)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        for (int c : new int[]{8, 9}) {
            table.getColumnModel().getColumn(c).setCellRenderer(rightRenderer);
        }
        // Căn giữa một số cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int c : new int[]{0, 3, 5, 6, 7}) {
            table.getColumnModel().getColumn(c).setCellRenderer(centerRenderer);
        }

        // Độ rộng cột (10 cột)
        int[] widths = {70, 160, 110, 70, 100, 70, 70, 70, 100, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // ── Footer tổng kết ───────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        footer.setBackground(new Color(235, 240, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 190, 210)));

        lblSoHoaDon = new JLabel("Số dòng: 0");
        lblSoHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblTongDoanhThu = new JLabel("Tổng thành tiền: 0 VNĐ");
        lblTongDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongDoanhThu.setForeground(new Color(160, 20, 20));

        footer.add(lblSoHoaDon);
        footer.add(lblTongDoanhThu);

        root.add(toolbar, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        // Sự kiện
        btnLoc.addActionListener(e -> loadChiTiet());
        btnTatCa.addActionListener(e -> {
            dpChonNgay.setDate(null);
            loadChiTiet();
        });

        loadChiTiet(); // Load lần đầu
        return root;
    }

    private void loadChiTiet() {
        detailModel.setRowCount(0);

        String ngayLoc = null;
        SimpleDateFormat sdf    = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfD   = new SimpleDateFormat("dd/MM/yyyy");

        if (dpChonNgay != null && dpChonNgay.getDate() != null)
            ngayLoc = sdf.format(dpChonNgay.getDate());

        List<Object[]> data = hoaDonDao.getChiTietThanhToan(ngayLoc, ngayLoc);

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        double tongTatCa = 0;

        for (Object[] r : data) {
            int   maPhieu  = (int)            r[0];
            // r[1] placeholder MaHD
            String sdt     = (String)         r[2];
            String tenKH   = (String)         r[3];
            String maSan   = (String)         r[4];
            java.sql.Date ngayThue = (java.sql.Date) r[5];
            Time   gioBD   = (Time)           r[6];
            Time   gioKT   = (Time)           r[7];
            float  dur     = (float)          r[8];
            double donGia  = (double)         r[9];
            double thanhTien = (double)       r[11];
            tongTatCa += thanhTien;

            detailModel.addRow(new Object[]{
                "P-" + maPhieu,
                tenKH,
                sdt,
                maSan,
                sdfD.format(ngayThue),
                gioBD.toString().substring(0, 5),
                gioKT.toString().substring(0, 5),
                String.format("%.1f h", dur),
                nf.format((long) donGia) + " đ",
                nf.format((long) thanhTien) + " VNĐ"
            });
        }

        lblSoHoaDon.setText("Số phiếu: " + data.size());
        lblTongDoanhThu.setText("Tổng thành tiền: " + nf.format((long) tongTatCa) + " VNĐ");
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3: THỐNG KÊ ĐẶT SÂN
    // ══════════════════════════════════════════════════════════
    private JPanel buildThongKeDatSanTab() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        toolbar.setBorder(BorderFactory.createTitledBorder("🔍 Lọc theo khoảng ngày (tùy chọn)"));

        dpTuNgay = new JDateChooser();
        dpTuNgay.setDateFormatString("dd/MM/yyyy");
        dpTuNgay.setPreferredSize(new Dimension(140, 28));
        
        dpDenNgay = new JDateChooser();
        dpDenNgay.setDateFormatString("dd/MM/yyyy");
        dpDenNgay.setPreferredSize(new Dimension(140, 28));

        JButton btnThongKe = new JButton("Thống Kê");
        btnThongKe.setBackground(new Color(23, 80, 160));
        btnThongKe.setForeground(Color.WHITE);
        btnThongKe.setFocusPainted(false);
        btnThongKe.setFont(new Font("Segoe UI", Font.BOLD, 12));

        toolbar.add(new JLabel("Từ ngày:"));
        toolbar.add(dpTuNgay);
        toolbar.add(new JLabel("Đến ngày:"));
        toolbar.add(dpDenNgay);
        toolbar.add(btnThongKe);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        
        lblSlSan5 = new JLabel("Số lượng đặt Sân 5: 0 lượt");
        lblSlSan5.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSlSan5.setForeground(new Color(20, 90, 20));
        
        lblSlSan7 = new JLabel("Số lượng đặt Sân 7: 0 lượt");
        lblSlSan7.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSlSan7.setForeground(new Color(160, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(15, 0, 15, 0);
        contentPanel.add(lblSlSan5, gbc);
        
        gbc.gridy = 1;
        contentPanel.add(lblSlSan7, gbc);

        root.add(toolbar, BorderLayout.NORTH);
        root.add(contentPanel, BorderLayout.CENTER);

        btnThongKe.addActionListener(e -> loadThongKeDatSan());

        loadThongKeDatSan();

        return root;
    }

    private void loadThongKeDatSan() {
        String tuNgay = null;
        String denNgay = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if (dpTuNgay.getDate() != null && dpDenNgay.getDate() != null) {
            tuNgay = sdf.format(dpTuNgay.getDate());
            denNgay = sdf.format(dpDenNgay.getDate());
            if (dpTuNgay.getDate().after(dpDenNgay.getDate())) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (dpTuNgay.getDate() != null || dpDenNgay.getDate() != null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ 'Từ ngày' và 'Đến ngày', hoặc để trống cả hai để xem toàn bộ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int[] kq = hoaDonDao.getThongKeSoLuongDatSan(tuNgay, denNgay);
        lblSlSan5.setText("Số lượng đặt Sân 5: " + kq[0] + " lượt");
        lblSlSan7.setText("Số lượng đặt Sân 7: " + kq[1] + " lượt");
    }
}


