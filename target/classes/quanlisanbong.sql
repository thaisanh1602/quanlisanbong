CREATE DATABASE QuanLySanBong
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

use QuanLySanBong;

CREATE TABLE SanBong (
    MaSan VARCHAR(10) PRIMARY KEY,
    TenSan NVARCHAR(50),
    LoaiSan INT, -- 5, 7, hoặc 11 người
    KhuVuc NVARCHAR(50),
    GiaTien DECIMAL(18, 2), -- Giá mỗi giờ hoặc mỗi ca
    TrangThai INT DEFAULT 0, -- 0: Khả dụng, 1: Đang bảo trì
    DaXoa Int DEFAULT  0         -- 0: chưa xóa, 1: đã xóa
);

CREATE TABLE TaiKhoan (
    MaTK INT PRIMARY KEY AUTO_INCREMENT,
    MaNV VARCHAR(10),
    TenDangNhap VARCHAR(50) UNIQUE,
    Sdt VARCHAR(15),
    MatKhau VARCHAR(255),
    LoaiTK NVARCHAR(20) -- 'Quan Ly', 'Nhan Vien' hoặc 'Khach Hang'
);

CREATE TABLE NhanVien (
    MaNV VARCHAR(10) PRIMARY KEY,
    MaTK INT,
    Ho NVARCHAR(30),
    Ten NVARCHAR(20),
    SDT VARCHAR(15),
    GioiTinh NVARCHAR(5),
    Email VARCHAR(50),
    Luong DECIMAL(18, 2),
    NgayBatDau DATE,
    ChucVu NVARCHAR(50) DEFAULT 'Nhan vien'
);

CREATE TABLE PhieuDatSan (
    MaPhieu INT PRIMARY KEY AUTO_INCREMENT,
    TenKhachHang NVARCHAR(100),
    SDT_Khach VARCHAR(15),
    MaSan VARCHAR(10),
    NgayThue DATE,
    GioBatDau TIME,
    GioKetThuc TIME,
    Duration FLOAT, -- Tính bằng (GioKetThuc - GioBatDau)
    TrangThaiTT INT DEFAULT 0, -- 0: Chưa thanh toán, 1: Đã thanh toán (Chờ trả sân), 2: Đã trả sân (Hoàn thành)
    MaHD INT DEFAULT NULL,
    FOREIGN KEY (MaSan) REFERENCES SanBong(MaSan)
);

CREATE TABLE HoaDon (
    MaHD INT PRIMARY KEY AUTO_INCREMENT,
    NgayLap DATETIME DEFAULT CURRENT_TIMESTAMP,
    MaNV VARCHAR(10),
    SDT_Khach VARCHAR(15), -- Lưu lại để tra cứu nhanh
    TongTienSan DECIMAL(18, 2), -- Tổng tiền trước thuế
    GiamGia INT DEFAULT 0, -- Đơn vị %
    Thue INT DEFAULT 5, -- Mặc định 5%
    TongThanhToan DECIMAL(18, 2), -- (TienSan * 1.05) * (1 - GiamGia/100)
    SoTienNhan DECIMAL(18, 2),
    TienTraLai DECIMAL(18, 2),
    FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
);
INSERT INTO SanBong (MaSan, TenSan, LoaiSan, KhuVuc, GiaTien, TrangThai, DaXoa) VALUES
-- Nhóm Sân 5 (Giá khoảng 200k - 250k)
('S05_01', N'Sân 5 - Số 1', 5, N'Khu A', 200000, 0, 0),
('S05_02', N'Sân 5 - Số 2', 5, N'Khu A', 200000, 0, 0),
('S05_03', N'Sân 5 - Số 3', 5, N'Khu B', 220000, 0, 0),
('S05_04', N'Sân 5 - Số 4', 5, N'Khu B', 220000, 1, 0), -- Đang bảo trì

-- Nhóm Sân 7 (Giá khoảng 350k - 450k)
('S07_01', N'Sân 7 - Số 1', 7, N'Khu B', 350000, 0, 0),
('S07_02', N'Sân 7 - Số 2', 7, N'Khu C', 400000, 0, 0),
('S07_03', N'Sân 7 - Số 3', 7, N'Khu C', 400000, 0, 0);


-- Thêm tài khoản
INSERT INTO TaiKhoan (MaNV, TenDangNhap, Sdt, MatKhau, LoaiTK) VALUES
('NV001', 'admin', '0901234567', '$2a$10$7eGAyPwCINMZ68cOWDLazeVGT9QpTo9YBSgTYff.mMkIhQS1zP.U.', N'Quản lý'),
('NV002', 'nv_nam', '0907778889', '$2a$10$iWzi30Z7btTNJfITGjL7P.B7Ov/jlcmji6ESsrMDV4gC6VeOQyBdS', N'Nhân viên');

-- Thêm nhân viên (Liên kết với tài khoản trên)
INSERT INTO NhanVien (MaNV, MaTK, Ho, Ten, SDT, GioiTinh, Email, Luong, NgayBatDau, ChucVu) VALUES
('NV001', 1, N'Nguyễn', N'An', '0901234567', N'Nam', 'an.nguyen@gmail.com', 15000000, '2023-01-10', N'Quản lý'),
('NV002', 2, N'Trần', N'Nam', '0907778889', N'Nam', 'nam.tran@gmail.com', 7000000, '2023-05-15', N'Nhân viên');

INSERT INTO PhieuDatSan (TenKhachHang, SDT_Khach, MaSan, NgayThue, GioBatDau, GioKetThuc, Duration, TrangThaiTT, MaHD) VALUES
(N'Lê Văn Tâm', '0988123123', 'S05_01', '2026-04-05', '17:00:00', '18:30:00', 1.5, 1, 1),
(N'Nguyễn Thị Bình', '0912334455', 'S07_01', '2026-04-06', '19:00:00', '21:00:00', 2.0, 1, 2),
-- Khách này đặt 2 sân cùng lúc
(N'Hoàng Long', '0909999000', 'S05_01', '2026-04-04', '08:00:00', '09:00:00', 1.0, 0, NULL);


INSERT INTO HoaDon (NgayLap, MaNV, SDT_Khach, TongTienSan, GiamGia, Thue, TongThanhToan, SoTienNhan, TienTraLai) VALUES
('2026-04-06 18:30:00', 'NV001', '0988123123', 300000, 0, 5, 315000, 320000, 5000),
('2026-04-05 21:00:00', 'NV002', '0977666555', 500000, 10, 5, 472500, 500000, 27500);


