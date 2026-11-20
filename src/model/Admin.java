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

    public void addStock(List<Stock> stocks, Stock stock) {
        stocks.add(stock);
    }

    /** Removes the stock with the given symbol. Returns true if it was found. */
    public boolean removeStock(List<Stock> stocks, String symbol) {
        Iterator<Stock> iterator = stocks.iterator();
        while (iterator.hasNext()) {
            Stock stock = iterator.next();
            if (stock.getSymbol().equals(symbol)) {
                iterator.remove();    // safe way to delete while looping
                return true;
            }
        }
        return false;
    }
}
