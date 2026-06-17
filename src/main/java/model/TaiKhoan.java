package model;

public class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String sdt;
    private String matKhau;
    private String loaiTK;

    public TaiKhoan() {
    }

    public TaiKhoan(int maTK, String tenDangNhap, String matKhau, String loaiTK) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.sdt = "";
        this.matKhau = matKhau;
        this.loaiTK = loaiTK;
    }

    public TaiKhoan(int maTK, String tenDangNhap, String sdt, String matKhau, String loaiTK) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.sdt = sdt;
        this.matKhau = matKhau;
        this.loaiTK = loaiTK;
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
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", sdt='" + sdt + '\'' +
                ", loaiTK='" + loaiTK + '\'' +
                '}';
    }
}
