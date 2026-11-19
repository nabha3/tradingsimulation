package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The welcome slides shown before login, like the intro screens of a trading app.
 * There are three slides. Each has a small animated picture, a title and two
 * lines of text. "Next" slides to the following one; "Get started" (or "Skip")
 * tells the login window to move on.
 *
 * Everything here is drawn by hand with Graphics2D. One Timer ticks about 60
 * times a second, updates the clock "time", and repaints.
 */
public class OnboardingPanel extends FadePanel {
    private static final String[] TITLES = {
        "Trade without the risk",
        "Prices that never sleep",
        "Know where you stand"
    };
    private static final String[][] LINES = {
        {"Start with " + Theme.RUPEE + "10,000 of virtual cash.", "Buy and sell stocks, lose nothing."},
        {"Watch the market move live and spot", "every rise and fall at a glance."},
        {"Your portfolio, profit and loss and every", "trade in one clean place."}
    };

    private static final Color BLUE = new Color(0x4C, 0x8D, 0xFF);
    private static final Color AMBER = new Color(0xF5, 0xB8, 0x3D);
    private static final Color PURPLE = new Color(0x9B, 0x7B, 0xFF);

    private int current = 0;                // index of the slide on screen
    private boolean sliding = false;        // true while moving to the next slide
    private long slideStart;
    private double time = 0;                // seconds since the panel was created, drives the pictures
    private long createdAt = System.currentTimeMillis();

    private JButton nextButton;
    private ActionListener onFinish;

    public OnboardingPanel(ActionListener onFinish) {
        this.onFinish = onFinish;
        setLayout(new BorderLayout());

        // Top right: the Skip button
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        top.setOpaque(false);
        JButton skip = Theme.createTextButton("Skip");
        skip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        top.add(skip);
        add(top, BorderLayout.NORTH);

        // Bottom: the Next button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 44, 0));
        nextButton = Theme.createPrimaryButton("Next");
        nextButton.setPreferredSize(new Dimension(260, 48));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goNext();
            }
        });
        bottom.add(nextButton);
        add(bottom, BorderLayout.SOUTH);

        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                time = (System.currentTimeMillis() - createdAt) / 1000.0;
                if (sliding && System.currentTimeMillis() - slideStart >= 450) {
                    sliding = false;
                    current++;
                    updateButtonText();
                }
                repaint();
            }
        });
        timer.start();
    }

    private void goNext() {
        if (sliding) {
            return;
        }
        if (current < TITLES.length - 1) {
            sliding = true;
            slideStart = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private void finish() {
        onFinish.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "finished"));
    }

    private void updateButtonText() {
        nextButton.setText(current == TITLES.length - 1 ? "Get started" : "Next");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        if (sliding) {
            double t = Math.min(1.0, (System.currentTimeMillis() - slideStart) / 450.0);
            double eased = Theme.easeOut(t);
            paintSlide(g2, current, -eased * w);            // old slide moves out to the left
            paintSlide(g2, current + 1, w - eased * w);     // new slide comes in from the right
        } else {
            paintSlide(g2, current, 0);
        }
        paintDots(g2);
        g2.dispose();
    }

    // Draws one complete slide (picture + title + text), shifted sideways by dx pixels.
    private void paintSlide(Graphics2D g2, int index, double dx) {
        Graphics2D g = (Graphics2D) g2.create();
        g.translate(dx, 0);
        int w = getWidth();
        int h = getHeight();
        int pictureY = (int) (h * 0.27);

        if (index == 0) {
            drawCoin(g, w / 2, pictureY);
        } else if (index == 1) {
            drawChart(g, w / 2, pictureY);
        } else {
            drawDonut(g, w / 2, pictureY);
        }

        g.setFont(Theme.font(Font.BOLD, 32));
        g.setColor(Theme.TEXT);
        drawCentered(g, TITLES[index], w / 2, pictureY + 160);

        g.setFont(Theme.font(Font.PLAIN, 16));
        g.setColor(Theme.MUTED);
        drawCentered(g, LINES[index][0], w / 2, pictureY + 200);
        drawCentered(g, LINES[index][1], w / 2, pictureY + 226);
        g.dispose();
    }

    // Three small dots under the text; the current one is a longer green pill.
    private void paintDots(Graphics2D g) {
        int w = getWidth();
        int y = (int) (getHeight() * 0.27) + 262;
        int total = TITLES.length;
        int x = w / 2 - 30;
        for (int i = 0; i < total; i++) {
            boolean active = (i == current);
            int width = active ? 24 : 8;
            g.setColor(active ? Theme.GREEN : Theme.BORDER);
            g.fillRoundRect(x, y, width, 8, 8, 8);
            x += width + 10;
        }
    }

    private void drawCentered(Graphics2D g, String text, int centreX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, centreX - fm.stringWidth(text) / 2, baselineY);
    }

    // Slide 1: a floating rupee coin with small dots orbiting it.
    private void drawCoin(Graphics2D g, int cx, int cy) {
        int y = (int) (cy + Math.sin(time * 2.0) * 8);      // gentle up-and-down bobbing

        for (int i = 4; i >= 1; i--) {                      // soft glow made of big faint circles
            int r = 66 + i * 16;
            g.setColor(Theme.alpha(Theme.GREEN, 12));
            g.fillOval(cx - r, y - r, r * 2, r * 2);
        }

        g.setPaint(new GradientPaint(cx - 70, y - 70, new Color(0x3D, 0xDB, 0x9E),
                cx + 70, y + 70, Theme.GREEN_DARK));
        g.fillOval(cx - 70, y - 70, 140, 140);
        g.setColor(Theme.alpha(Color.WHITE, 70));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(cx - 56, y - 56, 112, 112);

        g.setFont(Theme.font(Font.BOLD, 74));
        g.setColor(Color.WHITE);
        drawCentered(g, Theme.RUPEE, cx, y + 26);

        for (int i = 0; i < 3; i++) {                       // three dots going round the coin
            double angle = time * 1.2 + i * 2.094;
            int ox = (int) (cx + Math.cos(angle) * 120);
            int oy = (int) (y + Math.sin(angle) * 44);
            g.setColor(Theme.alpha(Theme.GREEN, 210));
            g.fillOval(ox - 6, oy - 6, 12, 12);
        }
    }

    // Slide 2: a price line that draws itself over and over, with a pulsing dot at its tip.
    private void drawChart(Graphics2D g, int cx, int cy) {
        int boxW = 380;
        int boxH = 170;
        int left = cx - boxW / 2;
        int top = cy - boxH / 2;

        g.setColor(Theme.alpha(Theme.BORDER, 160));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            int y = top + boxH * i / 4;
            g.drawLine(left, y, left + boxW, y);
        }

        double progress = Math.min(1.0, (time % 3.2) / 2.0);    // draws for 2s, then rests
        progress = Theme.easeOut(progress);
        int count = 70;
        int visible = Math.max(2, (int) (count * progress));

        Path2D.Double line = new Path2D.Double();
        double lastX = left;
        double lastY = top;
        for (int i = 0; i < visible; i++) {
            double x01 = i / (double) (count - 1);
            // a made-up price curve built from a few sine waves, trending upwards
            double v = 0.5 + 0.16 * Math.sin(x01 * 7) + 0.10 * Math.sin(x01 * 15 + 1) + 0.28 * (x01 - 0.5);
            double x = left + x01 * boxW;
            double y = top + boxH - v * boxH;
            if (i == 0) {
                line.moveTo(x, y);
            } else {
                line.lineTo(x, y);
            }
            lastX = x;
            lastY = y;
        }

        Path2D.Double area = new Path2D.Double(line);
        area.lineTo(lastX, top + boxH);
        area.lineTo(left, top + boxH);
        area.closePath();
        g.setPaint(new GradientPaint(0, top, Theme.alpha(Theme.GREEN, 80),
                0, top + boxH, Theme.alpha(Theme.GREEN, 0)));
        g.fill(area);

        g.setColor(Theme.GREEN);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(line);

        double pulse = (time * 1.5) % 1.0;                       // 0 to 1, repeats
        int ring = (int) (6 + pulse * 16);
        g.setColor(Theme.alpha(Theme.GREEN, (int) (140 * (1 - pulse))));
        g.fillOval((int) lastX - ring, (int) lastY - ring, ring * 2, ring * 2);
        g.setColor(Color.WHITE);
        g.fillOval((int) lastX - 5, (int) lastY - 5, 10, 10);
    }

    // Slide 3: a donut chart that sweeps open, like a portfolio breakdown.
    private void drawDonut(Graphics2D g, int cx, int cy) {
        double[] share = {0.45, 0.25, 0.20, 0.10};
        Color[] colors = {Theme.GREEN, BLUE, AMBER, PURPLE};
        double progress = Theme.easeOut(Math.min(1.0, (time % 5.0) / 1.6));

        int r = 78;
        g.setStroke(new BasicStroke(22f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(Theme.alpha(Theme.BORDER, 120));
        g.drawOval(cx - r, cy - r, r * 2, r * 2);                // the empty ring behind

        double start = 90;                                       // start at the top
        for (int i = 0; i < share.length; i++) {
            double extent = -360 * share[i] * progress;          // negative = clockwise
            g.setColor(colors[i]);
            g.draw(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, start, extent, Arc2D.OPEN));
            start += extent;
        }

        g.setFont(Theme.font(Font.BOLD, 28));
        g.setColor(Theme.GREEN);
        drawCentered(g, "+12.4%", cx, cy + 6);
        g.setFont(Theme.font(Font.PLAIN, 12));
        g.setColor(Theme.MUTED);
        drawCentered(g, "this month", cx, cy + 28);
    }
}
