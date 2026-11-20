import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import exceptions.InvalidQuantityException;
import model.Admin;
import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;
import model.User;

import java.util.ArrayList;

public class ConsoleDemo {
    public static void main(String[] args) {
        ArrayList<Stock> market = new ArrayList<>();
        market.add(new Stock("AAPL", 100));
        market.add(new Stock("MSFT", 50));
        Stock aapl = market.get(0);

        Trader t = new Trader("T1", "Nabha", "pw", 10000);
        System.out.println(t);

        try {
            t.buy(aapl, 10);                          // pays 1000
            System.out.println("After buying 10 AAPL: " + t);
            t.sell(aapl, 5);                          // gets 500 back
            System.out.println("After selling 5 AAPL: " + t);
            System.out.println(t.getHoldings());

            aapl.setPrice(120);                       // price moves up
            System.out.println("AAPL now " + aapl + ", P/L = " + t.getProfitLoss(aapl)); // (120-100)*5 = 100
            System.out.println("Portfolio value = " + t.getPortfolioValue(market));   // 5*120 = 600

            t.buy(aapl, 1000);                        // too expensive -> exception
            System.out.println("This line never prints.");
        } catch (InsufficientFundsException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            t.sell(aapl, 99);                         // owns only 5 -> exception
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            t.sell(aapl, 0);                          // zero shares makes no sense -> exception
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Market demo: prices move randomly and a listener is told each time.
        Market liveMarket = new Market();
        liveMarket.addStock(new Stock("AAPL", 100));
        liveMarket.addStock(new Stock("MSFT", 50));
        liveMarket.addListener(new MarketListener() {   // anonymous class
            @Override
            public void onPricesChanged() {
                System.out.println("Prices changed: " + liveMarket.getStocks());
            }
        });
        System.out.println("--- Market ---");
        for (int i = 0; i < 3; i++) {
            liveMarket.updatePrices();
        }

        // Trade history (only successful trades are recorded).
        System.out.println("--- History ---");
        for (model.Transaction tx : t.getHistory()) {
            System.out.println(tx);
        }

        // Polymorphism: a User variable can hold a Trader or an Admin.
        User[] users = { t, new Admin("A1", "Boss", "admin") };
        for (User u : users) {
            System.out.println(u.getRole() + " -> " + u);
        }
    }
}
