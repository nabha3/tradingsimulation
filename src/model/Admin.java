package model;

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

    public boolean removeStock(List<Stock> market, String symbol) {
        return market.removeIf(s -> s.getSymbol().equals(symbol));
    }
}
