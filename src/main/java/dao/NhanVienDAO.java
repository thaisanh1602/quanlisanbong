package dao;

import connection.DatabaseConnection;
import model.NhanVien;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    /**
     * Lấy toàn bộ danh sách NhanVien có join với TaiKhoan để lấy mật khẩu (chuỗi đã mã hoá hoặc để hiển thị **** tuỳ view).
     */
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT NV.*, TK.MatKhau, TK.MaTK AS TK_MaTK FROM NhanVien NV " +
                     "LEFT JOIN TaiKhoan TK ON NV.MaNV = TK.MaNV " +
                     "ORDER BY NV.MaNV ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
             
            while (rs.next()) {
                list.add(new NhanVien(
                    rs.getString("MaNV"),
                    rs.getInt("TK_MaTK"),
                    rs.getString("Ho"),
                    rs.getString("Ten"),
                    rs.getString("SDT"),
                    rs.getString("GioiTinh"),
                    rs.getString("Email"),
                    rs.getDouble("Luong"),
                    rs.getDate("NgayBatDau"),
                    rs.getString("ChucVu"),
                    rs.getString("MatKhau") // Chuỗi hash
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Cập nhật thông tin NhanVien (không chứa mã hóa mật khẩu ở đây vì chưa rõ update mật khẩu hay không)
     * Nếu nhập vào ô mật khẩu 1 chuỗi khác chuỗi hash cũ, ta sẽ mã hoá lại.
     */
    public boolean updateNhanVien(NhanVien nv, String rawPassword) {
        String sqlNV = "UPDATE NhanVien SET Ho = ?, Ten = ?, SDT = ?, GioiTinh = ?, Email = ?, Luong = ?, NgayBatDau = ? WHERE MaNV = ?";
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false); // Bật transaction
            
            // 1. Cập nhật nhân viên (Không sửa chức vụ theo yêu cầu)
            try (PreparedStatement pstNV = conn.prepareStatement(sqlNV)) {
                pstNV.setString(1, nv.getHo());
                pstNV.setString(2, nv.getTen());
                pstNV.setString(3, nv.getSdt());
                pstNV.setString(4, nv.getGioiTinh());
                pstNV.setString(5, nv.getEmail());
                pstNV.setDouble(6, nv.getLuong());
                pstNV.setDate(7, nv.getNgayBatDau());
                pstNV.setString(8, nv.getMaNV());
                pstNV.executeUpdate();
            }

            // 2. Cập nhật mật khẩu nếu có nhập thay đổi
            if (rawPassword != null && !rawPassword.trim().isEmpty() && !rawPassword.startsWith("$2a$")) {
                String sqlTK = "UPDATE TaiKhoan SET MatKhau = ?, TenDangNhap = ? WHERE MaTK = ?";
                try (PreparedStatement pstTK = conn.prepareStatement(sqlTK)) {
                    String hashedPw = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
                    pstTK.setString(1, hashedPw);
                    pstTK.setString(2, nv.getEmail()); // Dùng email làm tên đăng nhập
                    pstTK.setInt(3, nv.getMaTK());
                    pstTK.executeUpdate();
                }
            } else {
                // Chỉ cập nhật username là email
                String sqlTK = "UPDATE TaiKhoan SET TenDangNhap = ? WHERE MaTK = ?";
                try (PreparedStatement pstTK = conn.prepareStatement(sqlTK)) {
                    pstTK.setString(1, nv.getEmail());
                    pstTK.setInt(2, nv.getMaTK());
                    pstTK.executeUpdate();
                }
            }

            conn.commit();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return isSuccess;
    }

    /**
     * Xóa nhân viên và tài khoản
     */
    public boolean deleteNhanVien(String maNV, int maTK) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        
        try {
            conn.setAutoCommit(false);

            // Xóa ở NhanVien trước (FK)
            try (PreparedStatement pstNV = conn.prepareStatement("DELETE FROM NhanVien WHERE MaNV = ?")) {
                pstNV.setString(1, maNV);
                pstNV.executeUpdate();
            }

            // Xóa ở TaiKhoan (Nếu báo lỗi khóa ngoại ở Hóa đơn thì phải xử lý mồ côi)
            try (PreparedStatement pstTK = conn.prepareStatement("DELETE FROM TaiKhoan WHERE MaTK = ?")) {
                pstTK.setInt(1, maTK);
                pstTK.executeUpdate();
            }

            conn.commit();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return isSuccess;
    }

    /**
     * Thêm nhân viên mới (tự tạo mã, hash mật khẩu)
     */
    public boolean insertNhanVien(NhanVien nv, String rawPassword) {
        boolean isSuccess = false;
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;
        
        try {
            conn.setAutoCommit(false);
            
            // 1. Generate MaNV first
            String maNV = "NV001";
            String sqlGetMaxId = "SELECT MaNV FROM NhanVien WHERE MaNV LIKE 'NV%'";
            int maxId = 0;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlGetMaxId)) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    try {
                        int num = Integer.parseInt(id.substring(2));
                        // Ignore previously generated random IDs (length > 5 or num >= 10000)
                        if (num < 10000 && num > maxId) {
                            maxId = num;
                        }
                    } catch (Exception e) {}
                }
            }
            maNV = String.format("NV%03d", maxId + 1);

            // 2. Không tự động thêm TaiKhoan nữa
            // int genMaTK = -1;
            // (Đã xóa code tự thêm TaiKhoan)

            // 3. Thêm NhanVien
            String sqlNV = "INSERT INTO NhanVien (MaNV, MaTK, Ho, Ten, SDT, GioiTinh, Email, Luong, NgayBatDau, ChucVu) VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstNV = conn.prepareStatement(sqlNV)) {
                pstNV.setString(1, maNV);
                pstNV.setString(2, nv.getHo());
                pstNV.setString(3, nv.getTen());
                pstNV.setString(4, nv.getSdt());
                pstNV.setString(5, nv.getGioiTinh());
                pstNV.setString(6, nv.getEmail());
                pstNV.setDouble(7, nv.getLuong());
                pstNV.setDate(8, nv.getNgayBatDau());
                pstNV.setString(9, "Nhân viên"); // Mặc định
                pstNV.executeUpdate();
            }

            conn.commit();
            isSuccess = true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return isSuccess;
    }
    /**
     * Lấy thông tin nhân viên từ mã tài khoản (MaTK)
     */
    public NhanVien getNhanVienByMaTK(int maTK) {
        String sql = "SELECT NV.*, TK.MatKhau, TK.MaTK AS TK_MaTK FROM NhanVien NV " +
                     "LEFT JOIN TaiKhoan TK ON NV.MaNV = TK.MaNV " +
                     "WHERE TK.MaTK = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, maTK);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new NhanVien(
                        rs.getString("MaNV"),
                        rs.getInt("TK_MaTK"),
                        rs.getString("Ho"),
                        rs.getString("Ten"),
                        rs.getString("SDT"),
                        rs.getString("GioiTinh"),
                        rs.getString("Email"),
                        rs.getDouble("Luong"),
                        rs.getDate("NgayBatDau"),
                        rs.getString("ChucVu"),
                        rs.getString("MatKhau")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
