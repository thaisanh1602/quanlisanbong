import org.mindrot.jbcrypt.BCrypt;

public class GenPassword {
    public static void main(String[] args) {
        // 1. Mật khẩu gốc bạn muốn mã hóa
        String rawPassword = "123456";

        // 2. Tạo mã hóa BCrypt
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        // 3. Kiểm tra xem mật khẩu gốc có khớp với mã hóa hay không
        boolean isMatch = BCrypt.checkpw(rawPassword, hashedPassword);

        // In kết quả
        System.out.println("Mat khau goc: " + rawPassword);
        System.out.println("Chuoi ma hoa BCrypt: " + hashedPassword);
        System.out.println("Kiem tra khop: " + isMatch);
    }
}
