package model;

import java.sql.Date;

public class NhanVien {
    private String maNV;
    private int maTK;
    private String ho;
    private String ten;
    private String sdt;
    private String gioiTinh;
    private String email;
    private double luong;
    private Date ngayBatDau;
    private String chucVu;
    
    // Thuộc tính kết bảng để hiển thị mật khẩu ở view
    private String matKhau;

    public NhanVien() {
    }

    public NhanVien(String maNV, int maTK, String ho, String ten, String sdt, String gioiTinh, String email, double luong, Date ngayBatDau, String chucVu, String matKhau) {
        this.maNV = maNV;
        this.maTK = maTK;
        this.ho = ho;
        this.ten = ten;
        this.sdt = sdt;
        this.gioiTinh = gioiTinh;
        this.email = email;
        this.luong = luong;
        this.ngayBatDau = ngayBatDau;
        this.chucVu = chucVu;
        this.matKhau = matKhau;
    }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }

    public String getHo() { return ho; }
    public void setHo(String ho) { this.ho = ho; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getLuong() { return luong; }
    public void setLuong(double luong) { this.luong = luong; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
}
