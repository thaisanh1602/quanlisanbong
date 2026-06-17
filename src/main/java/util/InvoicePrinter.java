package util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.PdfEncodings;

import model.HoaDon;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InvoicePrinter {

    public static void printInvoice(HoaDon hd, List<Object[]> listToPay) {
        String dest = "HoaDon_" + hd.getMaHD() + ".pdf";
        try {
            // 1. Khởi tạo Writer và Document
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A5);
            document.setMargins(20, 20, 20, 20);

            String fontPath = "C:/Windows/Fonts/arial.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
            PdfFont fontBold = PdfFontFactory.createFont("C:/Windows/Fonts/arialbd.ttf", PdfEncodings.IDENTITY_H);

            // 3. Header
            document.add(new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setFont(fontBold).setFontSize(18).setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
            document.add(new Paragraph("SÂN BÓNG ĐÁ CHUYÊN NGHIỆP")
                    .setFont(fontBold).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

            // 4. Thông tin chung
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("Mã hóa đơn: HD-" + hd.getMaHD()).setFont(font).setFontSize(10));
            document.add(new Paragraph("Ngày lập: " + sdf.format(hd.getNgayLap())).setFont(font).setFontSize(10));
            document.add(new Paragraph("SĐT Khách hàng: " + hd.getSdtKhach()).setFont(font).setFontSize(10).setMarginBottom(10));

            // 5. Bảng chi tiết
            float[] columnWidths = {2, 4, 2, 2, 3};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            // Header bảng
            String[] headers = {"Mã Sân", "Ngày Thuê", "Giờ", "Số giờ", "Thành tiền"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(fontBold).setFontSize(9))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }

            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

            for (Object[] obj : listToPay) {
                String maSan = (String) obj[3];
                java.sql.Date ngay = (java.sql.Date) obj[5];
                java.sql.Time gb = (java.sql.Time) obj[6];
                java.sql.Time gk = (java.sql.Time) obj[7];
                float dur = (float) obj[8];
                double giaTien = (double) obj[9];
                double thanhTien = dur * giaTien;

                table.addCell(new Cell().add(new Paragraph(maSan).setFont(font).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(new SimpleDateFormat("dd/MM").format(ngay)).setFont(font).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(gb.toString().substring(0, 5) + "-" + gk.toString().substring(0, 5)).setFont(font).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(dur)).setFont(font).setFontSize(9)));
                table.addCell(new Cell().add(new Paragraph(nf.format(thanhTien)).setFont(font).setFontSize(9).setTextAlignment(TextAlignment.RIGHT)));
            }
            document.add(table);

            // 6. Tổng cộng
            document.add(new Paragraph("\nTổng tiền sân: " + nf.format(hd.getTongTienSan()) + " VNĐ")
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Giảm giá: " + nf.format(hd.getGiamGia()) + " VNĐ")
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Thuế (5%): " + nf.format(hd.getTongTienSan() * 0.05) + " VNĐ")
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("TỔNG THANH TOÁN: " + nf.format(hd.getTongThanhToan()) + " VNĐ")
                    .setFont(fontBold).setFontSize(12).setFontColor(ColorConstants.RED).setTextAlignment(TextAlignment.RIGHT));
            
            document.add(new Paragraph("\nTiền nhận: " + nf.format(hd.getSoTienNhan()) + " VNĐ")
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Tiền trả lại: " + nf.format(hd.getTienTraLai()) + " VNĐ")
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            // 7. Footer
            document.add(new Paragraph("\nCảm ơn Quý khách! Hẹn gặp lại.")
                    .setFont(font).setFontSize(10).setItalic().setTextAlignment(TextAlignment.CENTER));

            document.close();

            // Mở file PDF ngay sau khi tạo
            openFile(dest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void openFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
