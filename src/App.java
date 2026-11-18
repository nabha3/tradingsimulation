import gui.LoginFrame;
import model.Admin;
import model.Trader;
import model.UserRegistry;

import javax.swing.SwingUtilities;

/**
 * Entry point of BullRun. It creates a few demo accounts and opens the login window.
 * (The old console demo is kept in ConsoleDemo.java.)
 */
public class App {
    public static void main(String[] args) {
        final UserRegistry registry = new UserRegistry();
        registry.addUser(new Trader("trader1", "Nabha", "pass123", 10000));
        registry.addUser(new Admin("admin", "Boss", "admin123"));

        // Swing windows should be created on the event dispatch thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame(registry).setVisible(true);
            }
        });
    }
}
