import gui.LoginFrame;
import model.Admin;
import model.Market;
import model.Stock;
import model.Trader;
import model.UserRegistry;

import javax.swing.SwingUtilities;

/**
 * Entry point of BullRun. It creates the demo accounts and the market,
 * then opens the login window.
 * (The old console demo is kept in ConsoleDemo.java.)
 */
public class App {
    public static void main(String[] args) {
        final UserRegistry registry = new UserRegistry();
        registry.addUser(new Trader("trader1", "Nabha", "pass123", 10000));
        registry.addUser(new Admin("admin", "Boss", "admin123"));

        // Global stocks, with prices in US dollars
        final Market market = new Market();
        market.addStock(new Stock("AAPL", 190));
        market.addStock(new Stock("MSFT", 410));
        market.addStock(new Stock("GOOGL", 175));
        market.addStock(new Stock("AMZN", 185));
        market.addStock(new Stock("TSLA", 240));
        market.addStock(new Stock("NVDA", 130));

        // Swing windows should be created on the event dispatch thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame(registry, market).setVisible(true);
            }
        });
    }
}
