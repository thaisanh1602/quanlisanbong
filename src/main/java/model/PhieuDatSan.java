package model;

import java.sql.Date;
import java.sql.Time;

public class PhieuDatSan {
    private int maPhieu;
    private String tenKhachHang;
    private String sdtKhach;
    private String maSan;
    private Date ngayThue;
    private Time gioBatDau;
    private Time gioKetThuc;
    private float duration;
    private int trangThaiTT; // 0: Chưa thanh toán, 1: Đã thanh toán
    private int maTK; // Thêm MaTK để xác định người đặt

    public PhieuDatSan() {
    }

    public PhieuDatSan(int maPhieu, String tenKhachHang, String sdtKhach, String maSan, Date ngayThue, Time gioBatDau, Time gioKetThuc, float duration, int trangThaiTT, int maTK) {
        this.maPhieu = maPhieu;
        this.tenKhachHang = tenKhachHang;
        this.sdtKhach = sdtKhach;
        this.maSan = maSan;
        this.ngayThue = ngayThue;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.duration = duration;
        this.trangThaiTT = trangThaiTT;
        this.maTK = maTK;
    }

    public int getMaPhieu() { return maPhieu; }
    public void setMaPhieu(int maPhieu) { this.maPhieu = maPhieu; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public String getMaSan() { return maSan; }
    public void setMaSan(String maSan) { this.maSan = maSan; }

    public Date getNgayThue() { return ngayThue; }
    public void setNgayThue(Date ngayThue) { this.ngayThue = ngayThue; }

    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }

    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public float getDuration() { return duration; }
    public void setDuration(float duration) { this.duration = duration; }

    public int getTrangThaiTT() { return trangThaiTT; }
    public void setTrangThaiTT(int trangThaiTT) { this.trangThaiTT = trangThaiTT; }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
}
