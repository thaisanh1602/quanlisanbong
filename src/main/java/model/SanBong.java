package model;

public class SanBong {
    private String maSan;
    private String tenSan;
    private int loaiSan;
    private String khuVuc;
    private double giaTien;
    private int trangThai; // 0: Khả dụng, 1: Đang bảo trì

    public SanBong() {
    }

    public SanBong(String maSan, String tenSan, int loaiSan, String khuVuc, double giaTien, int trangThai) {
        this.maSan = maSan;
        this.tenSan = tenSan;
        this.loaiSan = loaiSan;
        this.khuVuc = khuVuc;
        this.giaTien = giaTien;
        this.trangThai = trangThai;
    }

    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }

    public String getTenSan() { return tenSan; }
    public void setTenSan(String tenSan) { this.tenSan = tenSan; }

    public int getLoaiSan() { return loaiSan; }
    public void setLoaiSan(int loaiSan) { this.loaiSan = loaiSan; }

    public String getKhuVuc() { return khuVuc; }
    public void setKhuVuc(String khuVuc) { this.khuVuc = khuVuc; }

    public double getGiaTien() { return giaTien; }
    public void setGiaTien(double giaTien) { this.giaTien = giaTien; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
}
