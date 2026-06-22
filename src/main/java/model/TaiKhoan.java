package model;

public class TaiKhoan {
    private int maTK;
    private String maNV;
    private String tenDangNhap;
    private String sdt;
    private String matKhau;
    private String loaiTK;

    public TaiKhoan() {
    }

    public TaiKhoan(int maTK, String maNV, String tenDangNhap, String matKhau, String loaiTK) {
        this.maTK = maTK;
        this.maNV = maNV;
        this.tenDangNhap = tenDangNhap;
        this.sdt = "";
        this.matKhau = matKhau;
        this.loaiTK = loaiTK;
    }

    public TaiKhoan(int maTK, String maNV, String tenDangNhap, String sdt, String matKhau, String loaiTK) {
        this.maTK = maTK;
        this.maNV = maNV;
        this.tenDangNhap = tenDangNhap;
        this.sdt = sdt;
        this.matKhau = matKhau;
        this.loaiTK = loaiTK;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public int getMaTK() {
        return maTK;
    }

    public void setMaTK(int maTK) {
        this.maTK = maTK;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getLoaiTK() {
        return loaiTK;
    }

    public void setLoaiTK(String loaiTK) {
        this.loaiTK = loaiTK;
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTK=" + maTK +
                ", maNV='" + maNV + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", sdt='" + sdt + '\'' +
                ", loaiTK='" + loaiTK + '\'' +
                '}';
    }
}
