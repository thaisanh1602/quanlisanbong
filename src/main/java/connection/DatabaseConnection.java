package connection;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Thông tin cấu hình database
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "QuanLySanBong";
    private static final String USERNAME = "root";
    // TODO: Cập nhật mật khẩu MySQL của bạn ở đây
    private static final String PASSWORD = "1234";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=false&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";

    /**
     * Phương thức lấy kết nối đến cơ sở dữ liệu
     * @return Connection object
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Đăng ký driver cho MySQL 8
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Tạo kết nối
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Kết nối cơ sở dữ liệu QuanLySanBong thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver MySQL: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Hàm test kết nối
     */
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối an toàn.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Kết nối thất bại. Vui lòng kiểm tra lại thông tin cấu hình (username, password, cổng).");
        }
    }
}
