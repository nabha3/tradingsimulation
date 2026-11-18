package model;

import java.util.Iterator;
import java.util.List;

public class Admin extends User {

    public Admin(String id, String name, String password) {
        super(id, name, password, 0);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    public void addStock(List<Stock> market, Stock stock) {
        market.add(stock);
    }

    /** Removes the stock with the given symbol. Returns true if it was found. */
    public boolean removeStock(List<Stock> market, String symbol) {
        Iterator<Stock> it = market.iterator();
        while (it.hasNext()) {
            Stock s = it.next();
            if (s.getSymbol().equals(symbol)) {
                it.remove();    // safe way to delete while looping
                return true;
            }
        }
        return false;
    }
}
