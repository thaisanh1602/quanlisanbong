package dao;

import connection.DatabaseConnection;
import model.PhieuDatSan;
import model.SanBong;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatSanDAO {

    /**
     * Tìm danh sách các sân (SanBong) KHẢ DỤNG trong một khoảng thời gian cụ thể
     * Điều kiện: Sân cùng loại (5,7) + Trạng thái = 0 (Không bảo trì)
     * VÀ Sân đó không bị trùng lặp giờ (Overlap) với bất kỳ PhieuDatSan nào trong cùng Ngày
     */
    public List<SanBong> findAvailableSan(List<Integer> listLoaiSan, String ngayThue, String gioBatDau, String gioKetThuc) {
        List<SanBong> list = new ArrayList<>();
        if (listLoaiSan == null || listLoaiSan.isEmpty()) return list;
        
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < listLoaiSan.size(); i++) {
            placeholders.append("?");
            if (i < listLoaiSan.size() - 1) placeholders.append(",");
        }
        
        String sql = "SELECT * FROM SanBong " +
                     "WHERE LoaiSan IN (" + placeholders.toString() + ") AND TrangThai = 0 AND DaXoa = 0 " +
                     "AND MaSan NOT IN (" +
                     "    SELECT MaSan FROM PhieuDatSan " +
                     "    WHERE NgayThue = ? AND TrangThaiTT != 2 " +
                     "    AND (GioBatDau < ? AND GioKetThuc > ?)" + 
                     ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
             
            int pIndex = 1;
            for (Integer loaiSanItem : listLoaiSan) {
                pst.setInt(pIndex++, loaiSanItem);
            }
            pst.setDate(pIndex++, Date.valueOf(ngayThue));
            pst.setTime(pIndex++, Time.valueOf(gioKetThuc + ":00")); // So sánh time MySQL cần ":00"
            pst.setTime(pIndex++, Time.valueOf(gioBatDau + ":00"));
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    list.add(new SanBong(
                        rs.getString("MaSan"),
                        rs.getString("TenSan"),
                        rs.getInt("LoaiSan"),
                        rs.getString("KhuVuc"),
                        rs.getDouble("GiaTien"),
                        rs.getInt("TrangThai")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra xem sân bóng có đang trống trong khung giờ nhất định hay không
     */
    public boolean isSanBongAvailable(String maSan, Date ngayThue, Time gioBatDau, Time gioKetThuc) {
        String sql = "SELECT COUNT(*) FROM PhieuDatSan WHERE MaSan = ? AND NgayThue = ? " +
                     "AND TrangThaiTT != 2 AND (GioBatDau < ? AND GioKetThuc > ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maSan);
            pst.setDate(2, ngayThue);
            pst.setTime(3, gioKetThuc);
            pst.setTime(4, gioBatDau);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Trả về true nếu không có phiếu nào trùng (count == 0)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lưu thông tin Đặt sân vào DB. Do khách có thể đặt nhiều sân cùng lúc, ta lưu List
     */
    public boolean insertPhieuDatSan(List<PhieuDatSan> listPhieu) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        
        String sql = "INSERT INTO PhieuDatSan (TenKhachHang, SDT_Khach, MaSan, NgayThue, GioBatDau, GioKetThuc, Duration, TrangThaiTT) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                for (PhieuDatSan pds : listPhieu) {
                    pst.setString(1, pds.getTenKhachHang());
                    pst.setString(2, pds.getSdtKhach());
                    pst.setString(3, pds.getMaSan());
                    pst.setDate(4, pds.getNgayThue());
                    pst.setTime(5, pds.getGioBatDau());
                    pst.setTime(6, pds.getGioKetThuc());
                    pst.setFloat(7, pds.getDuration());
                    pst.setInt(8, pds.getTrangThaiTT());
                    pst.addBatch(); // Gom lệnh để chạy nhanh hơn
                }
                pst.executeBatch();
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) {}
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
        return false;
    }

    /**
     * Lấy danh sách phiếu chưa thanh toán (TrangThaiTT = 0).
     * Bổ sung SĐT hoặc Ngày để lọc (nếu có). Trả về mảng Object[] để ráp bảng trực tiếp.
     */
    public List<Object[]> getPhieuChuaThanhToan(String sdt, String ngayLap, String gioBatDau) {
        return queryPhieuList(sdt, ngayLap, gioBatDau, 0);
    }

    /**
     * Lấy TẤT CẢ danh sách phiếu đặt sân đang hiển thị (0 và 1)
     */
    public List<Object[]> getAllPhieuDatSan(String sdt, String ngayLap, String gioBatDau) {
        return queryPhieuList(sdt, ngayLap, gioBatDau, -1); // -1 để lấy TrangThaiTT != 2
    }

    /**
     * Lấy danh sách phiếu ĐÃ thanh toán (TrangThaiTT = 1) để rải vô bảng Báo cáo Chi tiết
     */
    public List<Object[]> getPhieuDaThanhToan() {
        return queryPhieuList(null, null, null, 1);
    }

    /**
     * Lấy danh sách phiếu đặt sân của một số điện thoại cụ thể (Dành cho Lịch đặt của tôi)
     */
    public List<Object[]> getPhieuBySDT(String sdt) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT P.MaPhieu, P.TenKhachHang, P.SDT_Khach, P.MaSan, P.NgayThue, P.GioBatDau, P.GioKetThuc, P.Duration, S.LoaiSan, S.GiaTien, P.TrangThaiTT " +
                     "FROM PhieuDatSan P JOIN SanBong S ON P.MaSan = S.MaSan " +
                     "WHERE P.SDT_Khach = ? ORDER BY P.NgayThue DESC, P.GioBatDau DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int maphieu = rs.getInt("MaPhieu");
                    String masan = rs.getString("MaSan");
                    Date date = rs.getDate("NgayThue");
                    Time gb = rs.getTime("GioBatDau");
                    Time gk = rs.getTime("GioKetThuc");
                    double tongTien = rs.getDouble("GiaTien") * rs.getFloat("Duration");
                    int tt = rs.getInt("TrangThaiTT");
                    String status = (tt >= 1) ? "Đã thanh toán" : "Chưa thanh toán";
                    
                    list.add(new Object[]{ maphieu, masan, date, gb, gk, tongTien, status });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Core Query Tách Ra Thành Chung
    private List<Object[]> queryPhieuList(String sdt, String ngayLap, String gioBatDau, Integer trangThai) {
        List<Object[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT P.MaPhieu, P.TenKhachHang, P.SDT_Khach, P.MaSan, P.NgayThue, P.GioBatDau, P.GioKetThuc, P.Duration, S.LoaiSan, S.GiaTien, P.TrangThaiTT " +
            "FROM PhieuDatSan P " +
            "JOIN SanBong S ON P.MaSan = S.MaSan " +
            "WHERE 1=1"
        );
        
        if (trangThai != null) {
            if (trangThai == -1) {
                sql.append(" AND P.TrangThaiTT != 2");
            } else {
                sql.append(" AND P.TrangThaiTT = ?");
            }
        }
        if (sdt != null && !sdt.trim().isEmpty()) {
            sql.append(" AND P.SDT_Khach LIKE ?");
        }
        if (ngayLap != null && !ngayLap.trim().isEmpty()) {
            sql.append(" AND P.NgayThue = ?");
        }
        if (gioBatDau != null && !gioBatDau.trim().isEmpty()) {
            sql.append(" AND P.GioBatDau = ?");
        }
        sql.append(" ORDER BY P.NgayThue DESC, P.GioBatDau ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {
             
            int paramIndex = 1;
            if (trangThai != null && trangThai != -1) {
                pst.setInt(paramIndex++, trangThai);
            }
            
            if (sdt != null && !sdt.trim().isEmpty()) pst.setString(paramIndex++, "%" + sdt + "%");
            if (ngayLap != null && !ngayLap.trim().isEmpty()) pst.setDate(paramIndex++, Date.valueOf(ngayLap));
            if (gioBatDau != null && !gioBatDau.trim().isEmpty()) pst.setTime(paramIndex++, Time.valueOf(gioBatDau + ":00"));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int maphieu = rs.getInt("MaPhieu");
                    String tenkh = rs.getString("TenKhachHang");
                    String phone = rs.getString("SDT_Khach");
                    String masan = rs.getString("MaSan");
                    Date date = rs.getDate("NgayThue");
                    Time gb = rs.getTime("GioBatDau");
                    Time gk = rs.getTime("GioKetThuc");
                    float dur = rs.getFloat("Duration");
                    int loai = rs.getInt("LoaiSan");
                    double gia = rs.getDouble("GiaTien");
                    int tt = rs.getInt("TrangThaiTT");
                    list.add(new Object[]{ maphieu, tenkh, phone, masan, loai, date, gb, gk, dur, gia, tt });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Huỷ / Xoá phiếu đặt sân
     */
    public boolean deletePhieuDatSan(int maPhieu) {
        String sql = "DELETE FROM PhieuDatSan WHERE MaPhieu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Cập nhật trạng thái phiếu đặt sân
     */
    public boolean updateTrangThaiPhieu(int maPhieu, int newState) {
        String sql = "UPDATE PhieuDatSan SET TrangThaiTT = ? WHERE MaPhieu = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, newState);
            pst.setInt(2, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
