package dao;

import connection.DatabaseConnection;
import model.TaiKhoan;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    /**
     * Phương thức kiểm tra đăng nhập bằng username và password (đã dùng bcrypt)
     *
     * @param username tên đăng nhập
     * @param password mật khẩu người dùng nhập vào (chưa mã hoá)
     * @return Đối tượng TaiKhoan nếu đăng nhập thành công, ngược lại trả về null
     */
    public TaiKhoan kiemTraDangNhap(String username, String password) {
        String sql = "SELECT * FROM TaiKhoan WHERE TenDangNhap = ?";
        TaiKhoan taiKhoan = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, username);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDB = rs.getString("MatKhau");
                    
                    if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
                        taiKhoan = new TaiKhoan(
                                rs.getInt("MaTK"),
                                rs.getString("MaNV"),
                                rs.getString("TenDangNhap"),
                                rs.getString("Sdt"),
                                hashedPasswordFromDB,
                                rs.getString("LoaiTK")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taiKhoan;
    }

    public java.util.List<TaiKhoan> getAllTaiKhoan() {
        java.util.List<TaiKhoan> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM TaiKhoan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new TaiKhoan(
                    rs.getInt("MaTK"),
                    rs.getString("MaNV"),
                    rs.getString("TenDangNhap"),
                    rs.getString("Sdt"),
                    rs.getString("MatKhau"),
                    rs.getString("LoaiTK")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertTaiKhoan(TaiKhoan tk, String rawPassword) {
        String sql = "INSERT INTO TaiKhoan (MaNV, TenDangNhap, Sdt, MatKhau, LoaiTK) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, tk.getMaNV());
            pst.setString(2, tk.getTenDangNhap());
            pst.setString(3, tk.getSdt());
            pst.setString(4, BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
            pst.setString(5, tk.getLoaiTK());
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                if (tk.getMaNV() != null && !tk.getMaNV().isEmpty()) {
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            int generatedMaTK = rs.getInt(1);
                            String updateNVSql = "UPDATE NhanVien SET MaTK = ? WHERE MaNV = ?";
                            try (PreparedStatement pstUpdate = conn.prepareStatement(updateNVSql)) {
                                pstUpdate.setInt(1, generatedMaTK);
                                pstUpdate.setString(2, tk.getMaNV());
                                pstUpdate.executeUpdate();
                            }
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertTaiKhoanGetId(TaiKhoan tk, String rawPassword) {
        String sql = "INSERT INTO TaiKhoan (MaNV, TenDangNhap, Sdt, MatKhau, LoaiTK) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, tk.getMaNV());
            pst.setString(2, tk.getTenDangNhap());
            pst.setString(3, tk.getSdt());
            pst.setString(4, BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
            pst.setString(5, tk.getLoaiTK());
            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pst.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateTaiKhoan(TaiKhoan tk, String rawPassword) {
        boolean updatePass = rawPassword != null && !rawPassword.trim().isEmpty();
        String sql = updatePass ? 
                     "UPDATE TaiKhoan SET TenDangNhap = ?, Sdt = ?, MatKhau = ?, LoaiTK = ? WHERE MaTK = ?" :
                     "UPDATE TaiKhoan SET TenDangNhap = ?, Sdt = ?, LoaiTK = ? WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, tk.getTenDangNhap());
            pst.setString(2, tk.getSdt());
            if (updatePass) {
                pst.setString(3, BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
                pst.setString(4, tk.getLoaiTK());
                pst.setInt(5, tk.getMaTK());
            } else {
                pst.setString(3, tk.getLoaiTK());
                pst.setInt(4, tk.getMaTK());
            }
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTaiKhoan(int maTK) {
        String sql = "DELETE FROM TaiKhoan WHERE MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, maTK);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
