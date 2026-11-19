package gui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A panel that can fade in (and slide up a little) or fade out.
 *
 * How the animation works, in simple words:
 *   1. A Swing Timer fires every 15 milliseconds.
 *   2. Each time, we work out how far through the animation we are (0 to 1)
 *      and change two numbers: alpha (transparency) and offsetY (vertical shift).
 *   3. Then we call repaint(), and paint() draws everything with those numbers.
 * Because we override paint() (not paintComponent), all the buttons and text
 * inside the panel fade together.
 */
public class FadePanel extends JPanel {
    private float alpha = 1f;       // 0 = invisible, 1 = fully visible
    private int offsetY = 0;        // how many pixels the content is pushed down
    private Timer timer;

    public FadePanel() {
        setOpaque(false);
    }

    /** Fades from invisible to visible while sliding up by slidePixels. */
    public void fadeIn(int durationMs, int slidePixels) {
        animate(0f, 1f, durationMs, slidePixels, null);
    }

    /** Fades from visible to invisible, then runs whenDone. */
    public void fadeOut(int durationMs, ActionListener whenDone) {
        animate(1f, 0f, durationMs, 0, whenDone);
    }

    private void animate(final float from, final float to, final int durationMs,
                         final int slidePixels, final ActionListener whenDone) {
        if (timer != null) {
            timer.stop();
        }
        alpha = from;
        offsetY = (to > from) ? slidePixels : 0;
        final long start = System.currentTimeMillis();

        timer = new Timer(15, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double t = (System.currentTimeMillis() - start) / (double) durationMs;
                if (t > 1) {
                    t = 1;
                }
                double eased = Theme.easeOut(t);
                alpha = (float) (from + (to - from) * eased);
                offsetY = (to > from) ? (int) (slidePixels * (1 - eased)) : 0;
                repaint();
                if (t >= 1) {
                    timer.stop();
                    if (whenDone != null) {
                        whenDone.actionPerformed(e);
                    }
                }
            }
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        if (alpha >= 0.999f && offsetY == 0) {
            super.paint(g);             // normal painting when no animation is running
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        float a = Math.max(0f, Math.min(1f, alpha));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        g2.translate(0, offsetY);
        super.paint(g2);
        g2.dispose();
    }
}
