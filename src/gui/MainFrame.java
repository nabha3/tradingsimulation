package gui;

import model.User;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * The main window shown after a successful login.
 * For now it only greets the user. The market table, buy/sell panel
 * and portfolio view will be added here in the next steps.
 */
public class MainFrame extends JFrame {

    public MainFrame(User user) {
        setTitle("BullRun - " + user.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel welcome = new JLabel(
                "Welcome, " + user.getName() + " (" + user.getRole() + ")", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(welcome);

        setSize(700, 450);
        setLocationRelativeTo(null);
    }
}
