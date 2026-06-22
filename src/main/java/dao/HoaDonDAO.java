package dao;

import connection.DatabaseConnection;
import model.HoaDon;
import java.sql.*;

public class HoaDonDAO {

    public boolean taoHoaDonVaThanhToan(HoaDon hd, java.util.List<Integer> listMaPhieu) {
        String sqlHD = "INSERT INTO HoaDon (NgayLap, MaNV, SDT_Khach, TongTienSan, GiamGia, Thue, TongThanhToan, SoTienNhan, TienTraLai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdatePhieu = "UPDATE PhieuDatSan SET TrangThaiTT = 1, MaHD = ? WHERE MaPhieu = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);
            
            // 1. Thêm Hóa đơn
            int generatedMaHD = -1;
            try (PreparedStatement pstHD = conn.prepareStatement(sqlHD, Statement.RETURN_GENERATED_KEYS)) {
                pstHD.setTimestamp(1, hd.getNgayLap());
                pstHD.setString(2, hd.getMaNV());
                pstHD.setString(3, hd.getSdtKhach());
                pstHD.setDouble(4, hd.getTongTienSan());
                pstHD.setInt(5, hd.getGiamGia());
                pstHD.setInt(6, hd.getThue());
                pstHD.setDouble(7, hd.getTongThanhToan());
                pstHD.setDouble(8, hd.getSoTienNhan());
                pstHD.setDouble(9, hd.getTienTraLai());
                pstHD.executeUpdate();
                
                try (ResultSet rs = pstHD.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedMaHD = rs.getInt(1);
                        hd.setMaHD(generatedMaHD); // Cập nhật ngược lại vào object HoaDon
                    }
                }
            }

            if (generatedMaHD == -1) {
                conn.rollback();
                return false;
            }

            // 2. Chuyển trạng thái phiếu đặt sân thành đã thanh toán
            try (PreparedStatement pstPDS = conn.prepareStatement(sqlUpdatePhieu)) {
                for (Integer maPhieu : listMaPhieu) {
                    pstPDS.setInt(1, generatedMaHD);
                    pstPDS.setInt(2, maPhieu);
                    pstPDS.addBatch();
                }
                pstPDS.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) {}
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    /**
     * Thống kê tổng tiền thanh toán theo từng ngày lập hóa đơn
     */
    public java.util.List<Object[]> getThongKeDoanhThuNgay() {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String sql = "SELECT DATE(NgayLap) as DateOnly, SUM(TongThanhToan) as Revenue " +
                     "FROM HoaDon " +
                     "GROUP BY DATE(NgayLap) " +
                     "ORDER BY DateOnly ASC";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
             
            while (rs.next()) {
                String date = rs.getDate("DateOnly").toString();
                double rev = rs.getDouble("Revenue");
                result.add(new Object[]{date, rev});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Thống kê doanh thu theo ngày trong vòng 30 ngày gần nhất
     */
    public java.util.List<Object[]> getDoanhThu30NgayGanNhat() {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String sql = "SELECT P.NgayThue AS DateOnly, SUM(P.Duration * S.GiaTien) AS Revenue " +
                     "FROM PhieuDatSan P " +
                     "JOIN SanBong S ON P.MaSan = S.MaSan " +
                     "WHERE P.TrangThaiTT >= 1 AND P.NgayThue >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                     "GROUP BY P.NgayThue " +
                     "ORDER BY DateOnly ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[]{ rs.getDate("DateOnly").toString(), rs.getDouble("Revenue") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Lấy danh sách phiếu đã thanh toán, có thể lọc theo NgayThue.
     * ngayThue: định dạng "yyyy-MM-dd". Nếu null thì lấy tất cả.
     */
    public java.util.List<Object[]> getChiTietThanhToan(String ngayThue, String ignoredParam) {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT P.MaPhieu, P.TenKhachHang, P.SDT_Khach, P.MaSan, " +
            "       P.NgayThue, P.GioBatDau, P.GioKetThuc, P.Duration, " +
            "       S.LoaiSan, S.GiaTien, H.MaNV " +
            "FROM PhieuDatSan P " +
            "JOIN SanBong S ON S.MaSan = P.MaSan " +
            "LEFT JOIN HoaDon H ON P.MaHD = H.MaHD " +
            "WHERE P.TrangThaiTT >= 1"
        );
        if (ngayThue != null && !ngayThue.isEmpty()) {
            sql.append(" AND P.NgayThue = ?");
        }
        sql.append(" ORDER BY P.NgayThue DESC, P.GioBatDau ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
            if (ngayThue != null && !ngayThue.isEmpty()) {
                pst.setDate(1, java.sql.Date.valueOf(ngayThue));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getInt("MaPhieu"),
                        rs.getString("MaNV"),          // placeholder MaHD is now MaNV
                        rs.getString("SDT_Khach"),
                        rs.getString("TenKhachHang"),
                        rs.getString("MaSan"),
                        rs.getDate("NgayThue"),
                        rs.getTime("GioBatDau"),
                        rs.getTime("GioKetThuc"),
                        rs.getFloat("Duration"),
                        rs.getDouble("GiaTien"),
                        0,                             // giảm giá (không có trong phiếu)
                        rs.getFloat("Duration") * rs.getDouble("GiaTien")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Thống kê số lượng đặt sân 5 và sân 7 theo khoảng ngày
     */
    public int[] getThongKeSoLuongDatSan(String tuNgay, String denNgay) {
        int[] result = new int[2]; // [sân 5, sân 7]
        String sql = "SELECT S.LoaiSan, COUNT(P.MaPhieu) AS SoLuong " +
                     "FROM PhieuDatSan P " +
                     "JOIN SanBong S ON P.MaSan = S.MaSan " +
                     "WHERE P.TrangThaiTT >= 1 ";
        boolean hasDate = (tuNgay != null && denNgay != null);
        if (hasDate) {
            sql += "AND P.NgayThue >= ? AND P.NgayThue <= ? ";
        }
        sql += "GROUP BY S.LoaiSan";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
             
            if (hasDate) {
                pst.setDate(1, java.sql.Date.valueOf(tuNgay));
                pst.setDate(2, java.sql.Date.valueOf(denNgay));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int loai = rs.getInt("LoaiSan");
                    int sl = rs.getInt("SoLuong");
                    if (loai == 5) result[0] += sl;
                    else if (loai == 7) result[1] += sl;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
