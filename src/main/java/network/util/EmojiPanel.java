package network.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Floating emoji picker panel – click an emoji to insert it into the chat input.
 */
public class EmojiPanel extends JWindow {

    private static final String[] EMOJIS = {
        "😀","😁","😂","🤣","😊","😇","🙂","🙃",
        "😉","😌","😍","🥰","😘","😗","😙","😚",
        "😋","😛","😜","🤪","😝","🤑","🤗","🤭",
        "🤫","🤔","🤐","😐","😑","😶","😏","😒",
        "🙄","😬","😮","😯","😲","😳","🥺","😦",
        "😧","😨","😰","😥","😢","😭","😱","😤",
        "😠","😡","🤬","💀","👻","😺","🎉","🔥",
        "❤️","💔","👍","👎","👏","🙌","🤝","✌️",
        "🤞","👋","😎","🤓","🥳","💯","🎶","⭐"
    };

    private final Consumer<String> onSelect;

    public EmojiPanel(Component owner, Consumer<String> onSelect) {
        super(SwingUtilities.getWindowAncestor(owner));
        this.onSelect = onSelect;
        buildUI();
    }

    private void buildUI() {
        Color bg = new Color(0x2D2D40);
        Color hover = new Color(0x45475A);

        JPanel grid = new JPanel(new GridLayout(0, 8, 2, 2));
        grid.setBackground(bg);
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (String emoji : EMOJIS) {
            JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            lbl.setOpaque(true);
            lbl.setBackground(bg);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.setPreferredSize(new Dimension(36, 36));

            lbl.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { lbl.setBackground(hover); }
                @Override public void mouseExited(MouseEvent e)  { lbl.setBackground(bg); }
                @Override public void mouseClicked(MouseEvent e) {
                    onSelect.accept(emoji);
                    setVisible(false);
                }
            });
            grid.add(lbl);
        }

        // Border
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(0x585B70), 1));
        wrapper.setBackground(bg);
        wrapper.add(grid);
        add(wrapper);
        pack();

        // Click outside to close
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
            @Override public void windowLostFocus(java.awt.event.WindowEvent e) { setVisible(false); }
        });
    }

    /** Show the panel above/below the given anchor component. */
    public void showNear(Component anchor) {
        Point loc = anchor.getLocationOnScreen();
        int x = loc.x;
        int y = loc.y - getPreferredSize().height - 4;
        if (y < 0) y = loc.y + anchor.getHeight() + 4;
        setLocation(x, y);
        setVisible(true);
        toFront();
    }
}
