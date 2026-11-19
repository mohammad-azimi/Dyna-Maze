package dynamaze;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class Ui {
    static final Color BACKGROUND = new Color(0xEEF2F5);
    static final Color INK = new Color(0x15273A);
    static final Color MUTED = new Color(0x486176);
    static final Color BORDER = new Color(0xCFD9E3);
    static final Color WHITE = Color.WHITE;
    static final Color TEAL = new Color(0x006D63);
    static final Color ORANGE = new Color(0xA34713);
    static final Color BLUE = new Color(0x3C4F9B);
    static final Color[] SERIES = {BLUE, ORANGE, TEAL};

    private Ui() {}

    static Font font(int size, boolean bold) {
        return new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, size);
    }

    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font(size, bold));
        return label;
    }

    static JPanel row(int gap) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        panel.setOpaque(false);
        return panel;
    }

    static JPanel card(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        return panel;
    }

    static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(font(14, true));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        colorButton(button, primary);
        return button;
    }

    static void colorButton(JButton button, boolean primary) {
        button.setBackground(primary ? TEAL : WHITE);
        button.setForeground(primary ? WHITE : INK);
    }
}
