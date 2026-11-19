package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * One place for the look of the whole application: colours, fonts and a few
 * small styled components. Keeping this in one class means every window looks
 * consistent, and changing a colour here changes it everywhere.
 */
public class Theme {
    // Colours (a dark "trading terminal" palette)
    public static final Color BACKGROUND = new Color(0x0B, 0x0E, 0x14);
    public static final Color SURFACE    = new Color(0x12, 0x16, 0x1C);
    public static final Color ROW_ALT    = new Color(0x16, 0x1B, 0x22);
    public static final Color FIELD      = new Color(0x0D, 0x11, 0x17);
    public static final Color BORDER     = new Color(0x23, 0x2A, 0x33);
    public static final Color TEXT       = new Color(0xE6, 0xED, 0xF3);
    public static final Color MUTED      = new Color(0x8B, 0x94, 0x9E);
    public static final Color GREEN      = new Color(0x2E, 0xBD, 0x85);
    public static final Color GREEN_DARK = new Color(0x24, 0x9A, 0x6B);
    public static final Color RED        = new Color(0xF6, 0x46, 0x5D);

    // Characters used in the price table
    public static final String ARROW_UP = "▲";
    public static final String ARROW_DOWN = "▼";
    public static final String RUPEE = "₹";

    // Nobody needs an object of this class; everything in it is static.
    private Theme() {
    }

    public static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    /** Mixes two colours. t = 0 gives a, t = 1 gives b. */
    public static Color blend(Color a, Color b, double t) {
        if (t < 0) {
            t = 0;
        }
        if (t > 1) {
            t = 1;
        }
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    /** Same colour with a different transparency (0 to 255). */
    public static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Easing: starts fast and slows down at the end, which feels natural. t goes 0 to 1. */
    public static double easeOut(double t) {
        double inv = 1 - t;
        return 1 - inv * inv * inv;
    }

    /** A plain text button without a background (e.g. "Skip"). Brightens on hover. */
    public static JButton createTextButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                setForeground(getModel().isRollover() ? TEXT : MUTED);
                super.paintComponent(g);
            }
        };
        b.setFont(font(Font.PLAIN, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** The "BullRun" logo: "Bull" in white and "Run" in green. */
    public static JPanel createLogo(int size) {
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logo.setOpaque(false);
        JLabel bull = new JLabel("Bull");
        bull.setFont(font(Font.BOLD, size));
        bull.setForeground(TEXT);
        JLabel run = new JLabel("Run");
        run.setFont(font(Font.BOLD, size));
        run.setForeground(GREEN);
        logo.add(bull);
        logo.add(run);
        return logo;
    }

    /** Gives a text field the dark style (works for password fields too). */
    public static void styleField(JTextField field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(font(Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    /** A small muted label, used above input fields. */
    public static JLabel createCaption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    /** A green button with rounded corners. */
    public static JButton createPrimaryButton(String text) {
        return new RoundedButton(text);
    }

    /**
     * Inner class: a button that paints its own rounded background.
     * It gets a bit darker when the mouse is over it.
     */
    private static class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setFont(font(Font.BOLD, 15));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);    // we paint the background ourselves
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? GREEN_DARK : GREEN);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);        // draws the text on top
        }
    }

    /** A panel with rounded corners and a thin border, used as a "card". */
    public static class CardPanel extends FadePanel {
        public CardPanel() {
            super();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
