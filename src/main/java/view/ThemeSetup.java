package view;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeSetup {
    
    public static void applyTheme() {
        try {
            // Cấu hình các thuộc tính hệ thống của FlatLaf trước khi khởi tạo LookAndFeel
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            
            // Thiết lập LookAndFeel FlatMacLightLaf (giao diện macOS sáng cực kỳ hiện đại)
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            
            // --- THIẾT LẬP THẨM MỸ TOÀN CỤC (GLOBAL STYLES) ---
            
            // 1. Bo góc (Corners & Arcs)
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("CheckBox.arc", 6);
            
            // 2. Margins & Padding cho các ô nhập liệu
            UIManager.put("TextComponent.margin", new Insets(6, 12, 6, 12));
            
            // 3. Tông màu Accent chính (Xanh dương hoàng gia sang trọng)
            // FlatLaf tự động ánh xạ màu này cho tiêu điểm chọn, đường viền kích hoạt, v.v.
            System.setProperty("flatlaf.accentColor", "#0066FF");
            
            // 4. Định dạng ScrollBar siêu mỏng thanh lịch
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.showButtons", false);
            
            // 5. Định dạng Bảng (JTable) chuyên nghiệp
            UIManager.put("Table.rowHeight", 35); // Dòng cao thoáng đãng
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false); // Bỏ đường kẻ đứng cho đúng phong cách hiện đại
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.selectionBackground", new Color(230, 242, 255));
            UIManager.put("Table.selectionForeground", new Color(0, 50, 150));
            
            // 6. Định dạng Tiêu đề bảng (JTableHeader)
            UIManager.put("TableHeader.background", new Color(245, 247, 250));
            UIManager.put("TableHeader.foreground", new Color(60, 64, 67));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("TableHeader.height", 38);
            UIManager.put("TableHeader.cellBorder", BorderFactory.createEmptyBorder(6, 12, 6, 12));
            
            // 7. Định dạng JTabbedPane (Ribbon Tabs)
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabType", "card");
            UIManager.put("TabbedPane.selectedBackground", new Color(255, 255, 255));
            UIManager.put("TabbedPane.hoverColor", new Color(240, 244, 250));
            
            // 8. Định dạng Menu Bar
            UIManager.put("MenuBar.background", new Color(248, 249, 250));
            
        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo FlatMacLightLaf: " + ex.getMessage());
        }
    }
}
