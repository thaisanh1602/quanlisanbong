package model;

public class KhachHang {
    private int maKH;
    private int maTK;
    private String tenKhachHang;
    private String sdt;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(int maKH, int maTK, String tenKhachHang, String sdt, int diemTichLuy) {
        this.maKH = maKH;
        this.maTK = maTK;
        this.tenKhachHang = tenKhachHang;
        this.sdt = sdt;
        this.diemTichLuy = diemTichLuy;
    }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }
}
