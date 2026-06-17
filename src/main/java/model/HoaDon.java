package model;

import java.sql.Timestamp;

public class HoaDon {
    private int maHD;
    private Timestamp ngayLap;
    private String sdtKhach;
    private double tongTienSan;
    private int giamGia;
    private int thue;
    private double tongThanhToan;
    private double soTienNhan;
    private double tienTraLai;

    public HoaDon() {
    }

    public HoaDon(int maHD, Timestamp ngayLap, String sdtKhach, double tongTienSan, int giamGia, int thue, double tongThanhToan, double soTienNhan, double tienTraLai) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.sdtKhach = sdtKhach;
        this.tongTienSan = tongTienSan;
        this.giamGia = giamGia;
        this.thue = thue;
        this.tongThanhToan = tongThanhToan;
        this.soTienNhan = soTienNhan;
        this.tienTraLai = tienTraLai;
    }

    // Getters and Setters
    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }
    public double getTongTienSan() { return tongTienSan; }
    public void setTongTienSan(double tongTienSan) { this.tongTienSan = tongTienSan; }
    public int getGiamGia() { return giamGia; }
    public void setGiamGia(int giamGia) { this.giamGia = giamGia; }
    public int getThue() { return thue; }
    public void setThue(int thue) { this.thue = thue; }
    public double getTongThanhToan() { return tongThanhToan; }
    public void setTongThanhToan(double tongThanhToan) { this.tongThanhToan = tongThanhToan; }
    public double getSoTienNhan() { return soTienNhan; }
    public void setSoTienNhan(double soTienNhan) { this.soTienNhan = soTienNhan; }
    public double getTienTraLai() { return tienTraLai; }
    public void setTienTraLai(double tienTraLai) { this.tienTraLai = tienTraLai; }
}
