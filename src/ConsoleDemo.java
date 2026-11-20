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
        ArrayList<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("AAPL", 100));
        stocks.add(new Stock("MSFT", 50));
        Stock aapl = stocks.get(0);

        Trader trader = new Trader("T1", "Nabha", "pw", 10000);
        System.out.println(trader);

        try {
            trader.buy(aapl, 10);                          // pays 1000
            System.out.println("After buying 10 AAPL: " + trader);
            trader.sell(aapl, 5);                          // gets 500 back
            System.out.println("After selling 5 AAPL: " + trader);
            System.out.println(trader.getHoldings());

            aapl.setPrice(120);                       // price moves up
            System.out.println("AAPL now " + aapl + ", P/L = " + trader.getProfitLoss(aapl)); // (120-100)*5 = 100
            System.out.println("Portfolio value = " + trader.getPortfolioValue(stocks));   // 5*120 = 600

            trader.buy(aapl, 1000);                        // too expensive -> exception
            System.out.println("This line never prints.");
        } catch (InsufficientFundsException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            trader.sell(aapl, 99);                         // owns only 5 -> exception
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            trader.sell(aapl, 0);                          // zero shares makes no sense -> exception
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InvalidQuantityException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Market demo: prices move randomly and a listener is told each time.
        Market market = new Market();
        market.addStock(new Stock("AAPL", 100));
        market.addStock(new Stock("MSFT", 50));
        market.addListener(new MarketListener() {   // anonymous class
            @Override
            public void onPricesChanged() {
                System.out.println("Prices changed: " + market.getStocks());
            }
        });
        System.out.println("--- Market ---");
        for (int i = 0; i < 3; i++) {
            market.updatePrices();
        }

        // Trade history (only successful trades are recorded).
        System.out.println("--- History ---");
        for (model.Transaction transaction : trader.getHistory()) {
            System.out.println(transaction);
        }

        // Polymorphism: a User variable can hold a Trader or an Admin.
        User[] users = { trader, new Admin("A1", "Boss", "admin") };
        for (User user : users) {
            System.out.println(user.getRole() + " -> " + user);
        }
    }
}
