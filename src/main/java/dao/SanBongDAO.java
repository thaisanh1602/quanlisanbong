package dao;

import connection.DatabaseConnection;
import model.SanBong;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SanBongDAO {

    /**
     * Đếm tổng số sân (Bao gồm cả đã xoá và bảo trì) thuộc một loại sân để sinh mã ID
     */
    public int countSanBongByLoai(int loaiSan) {
        String sql = "SELECT COUNT(*) FROM SanBong WHERE LoaiSan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, loaiSan);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lấy danh sách sân theo trạng thái (0: khả dụng, 1: bảo trì)
     */
    public List<SanBong> getSanBongByTrangThai(int trangThai) {
        List<SanBong> list = new ArrayList<>();
        String sql = "SELECT * FROM SanBong WHERE TrangThai = ? AND DaXoa = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, trangThai);
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

    public List<SanBong> getAllSanBong() {
        List<SanBong> list = new ArrayList<>();
        String sql = "SELECT * FROM SanBong WHERE DaXoa = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Chuyển trạng thái của sân (Bảo trì <-> Khả dụng)
     */
    public boolean toggleTrangThai(String maSan, int newStatus) {
        String sql = "UPDATE SanBong SET TrangThai = ? WHERE MaSan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, newStatus);
            pst.setString(2, maSan);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa sân
     */
    public boolean deleteSanBong(String maSan) {
        String sql = "UPDATE SanBong SET DaXoa = 1 WHERE MaSan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, maSan);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm sân bóng mới 
     */
    public boolean insertSanBong(SanBong sb) {
        String sql = "INSERT INTO SanBong (MaSan, TenSan, LoaiSan, KhuVuc, GiaTien, TrangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, sb.getMaSan());
            pst.setString(2, sb.getTenSan());
            pst.setInt(3, sb.getLoaiSan());
            pst.setString(4, sb.getKhuVuc());
            pst.setDouble(5, sb.getGiaTien());
            pst.setInt(6, sb.getTrangThai());
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
