package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The moving backdrop of the welcome screens: two smooth price lines that
 * draw themselves from left to right and then keep scrolling slowly, like
 * a live market chart.
 *
 * Each line is just a list of numbers between 0 and 1 (a random walk).
 * Every tick the whole line slides a little to the left, and when it has
 * moved one full step we drop the first number and add a new one at the end.
 */
public class AnimatedBackground extends JPanel {
    private static final int POINTS = 90;           // how many points fit across the screen
    private static final Color BLUE = new Color(0x4C, 0x8D, 0xFF);

    private ArrayList<Double> mainLine;
    private ArrayList<Double> backLine;
    private double scroll = 0;                      // 0 to 1: progress towards the next point
    private double reveal = 0;                      // 0 to 1: how much of the line is drawn so far
    private long startTime;
    private Random random = new Random();

    public AnimatedBackground() {
        setBackground(Theme.BACKGROUND);
        mainLine = makeWalk(0.55);
        backLine = makeWalk(0.40);
        startTime = System.currentTimeMillis();

        Timer timer = new Timer(33, new ActionListener() {      // about 30 frames per second
            @Override
            public void actionPerformed(ActionEvent e) {
                scroll += 0.04;
                if (scroll >= 1) {
                    scroll -= 1;
                    shift(mainLine);
                    shift(backLine);
                }
                double t = (System.currentTimeMillis() - startTime) / 2400.0;
                reveal = Theme.easeOut(Math.min(1.0, t));
                repaint();
            }
        });
        timer.start();
    }

    // Makes a list of random values that drift up and down gently.
    private ArrayList<Double> makeWalk(double start) {
        ArrayList<Double> list = new ArrayList<>();
        double value = start;
        for (int i = 0; i < POINTS + 2; i++) {
            list.add(value);
            value = nextValue(value);
        }
        return list;
    }

    // One small random step, pulled gently back towards the middle.
    private double nextValue(double previous) {
        double v = previous + (random.nextDouble() - 0.5) * 0.12 + (0.5 - previous) * 0.04;
        if (v < 0.08) {
            v = 0.08;
        }
        if (v > 0.92) {
            v = 0.92;
        }
        return v;
    }

    private void shift(ArrayList<Double> list) {
        double last = list.get(list.size() - 1);
        list.remove(0);
        list.add(nextValue(last));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Soft green glow near the top
        float radius = Math.max(w, h) * 0.75f;
        g2.setPaint(new RadialGradientPaint(w / 2f, h * 0.15f, radius,
                new float[] {0f, 1f},
                new Color[] {Theme.alpha(Theme.GREEN, 34), Theme.alpha(Theme.GREEN, 0)}));
        g2.fillRect(0, 0, w, h);

        // The chart lives in the lower part of the window
        int top = (int) (h * 0.70);
        int bottom = (int) (h * 0.97);

        // Faint horizontal grid lines
        g2.setColor(Theme.alpha(Theme.BORDER, 70));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= 4; i++) {
            int y = top + (bottom - top) * i / 4;
            g2.drawLine(0, y, w, y);
        }

        // Only draw the part of the lines that has been "revealed" so far
        g2.setClip(0, 0, (int) (w * reveal), h);

        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Theme.alpha(BLUE, 90));
        g2.draw(buildPath(backLine, w, top, bottom));

        Path2D.Double line = buildPath(mainLine, w, top, bottom);
        Path2D.Double area = new Path2D.Double(line);
        double step = w / (double) (POINTS - 1);
        area.lineTo(w + step, h);
        area.lineTo(-step, h);
        area.closePath();
        g2.setPaint(new GradientPaint(0, top, Theme.alpha(Theme.GREEN, 60),
                0, h, Theme.alpha(Theme.GREEN, 0)));
        g2.fill(area);

        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Theme.alpha(Theme.GREEN, 210));
        g2.draw(line);

        g2.dispose();
    }

    // Turns a list of values into a smooth curve across the window.
    private Path2D.Double buildPath(ArrayList<Double> points, int w, int top, int bottom) {
        Path2D.Double path = new Path2D.Double();
        double step = w / (double) (POINTS - 1);
        double previousX = 0;
        double previousY = 0;
        for (int i = 0; i < points.size(); i++) {
            double x = (i - scroll) * step;
            double y = bottom - points.get(i) * (bottom - top);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                // curve towards the midpoint between the previous and current point
                path.quadTo(previousX, previousY, (previousX + x) / 2, (previousY + y) / 2);
            }
            previousX = x;
            previousY = y;
        }
        path.lineTo(previousX, previousY);
        return path;
    }
}
