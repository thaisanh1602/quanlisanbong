package dao;

import connection.DatabaseConnection;
import model.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KhachHangDAO {

    public boolean insertKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (MaTK, TenKhachHang, SDT, DiemTichLuy) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, kh.getMaTK());
            pst.setString(2, kh.getTenKhachHang());
            pst.setString(3, kh.getSdt());
            pst.setInt(4, kh.getDiemTichLuy());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public KhachHang getKhachHangByMaTK(int maTK) {
        String sql = "SELECT * FROM KhachHang WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, maTK);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getInt("MaKH"),
                            rs.getInt("MaTK"),
                            rs.getString("TenKhachHang"),
                            rs.getString("SDT"),
                            rs.getInt("DiemTichLuy")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
