import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import model.Admin;
import model.Stock;
import model.Trader;
import model.User;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        ArrayList<Stock> market = new ArrayList<>();
        market.add(new Stock("TCS", 100));
        market.add(new Stock("INFY", 50));
        Stock tcs = market.get(0);

        Trader t = new Trader("T1", "Nabha", "pw", 10000);
        System.out.println(t);

        try {
            t.buy(tcs, 10);                          // pays 1000
            System.out.println("After buying 10 TCS: " + t);
            t.sell(tcs, 5);                          // gets 500 back
            System.out.println("After selling 5 TCS: " + t);
            System.out.println(t.getHoldings());

            tcs.setPrice(120);                       // price moves up
            System.out.println("TCS now " + tcs + ", P/L = " + t.getProfitLoss(tcs)); // (120-100)*5 = 100
            System.out.println("Portfolio value = " + t.getPortfolioValue(market));   // 5*120 = 600

            t.buy(tcs, 1000);                        // too expensive -> exception
            System.out.println("This line never prints.");
        } catch (InsufficientFundsException e) {
            System.out.println("Caught: " + e.getMessage());
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            t.sell(tcs, 99);                         // owns only 5 -> exception
        } catch (InsufficientSharesException e) {
            System.out.println("Caught: " + e.getMessage());
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
