package model;

import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import java.util.HashMap;

public class Trader extends User {
    // symbol -> holding
    private HashMap<String, Holding> holdings;

    public Trader(String id, String name, String password, double walletBalance) {
        super(id, name, password, walletBalance);
        this.holdings = new HashMap<>();
    }

    @Override
    public String getRole() {
        return "Trader";
    }

    public HashMap<String, Holding> getHoldings() {
        return holdings;
    }

    public void buy(Tradable stock, int quantity) throws InsufficientFundsException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        double totalCost = stock.getPrice() * quantity;
        if (totalCost > getWalletBalance()) {
            throw new InsufficientFundsException(
                "Not enough money: need " + String.format("%.2f", totalCost)
                + " but wallet has " + String.format("%.2f", getWalletBalance()));
        }
        withdraw(totalCost);
        Holding existing = holdings.get(stock.getSymbol());
        if (existing == null) {
            holdings.put(stock.getSymbol(), new Holding(stock.getSymbol(), quantity, stock.getPrice()));
        } else {
            existing.addShares(quantity, stock.getPrice());
        }
    }

    public void sell(Tradable stock, int quantity) throws InsufficientSharesException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        Holding existing = holdings.get(stock.getSymbol());
        int owned = (existing == null) ? 0 : existing.getQuantity();
        if (quantity > owned) {
            throw new InsufficientSharesException(
                "Not enough shares of " + stock.getSymbol() + ": you own " + owned
                + " but tried to sell " + quantity);
        }
        deposit(stock.getPrice() * quantity);
        existing.removeShares(quantity);
        if (existing.getQuantity() == 0) {
            holdings.remove(stock.getSymbol());
        }
    }

    // Total value of all shares owned, at the given stock prices.
    public double getPortfolioValue(java.util.List<Stock> market) {
        double total = 0;
        for (Stock s : market) {
            Holding h = holdings.get(s.getSymbol());
            if (h != null) {
                total += h.getQuantity() * s.getPrice();
            }
        }
        return total;
    }

    public double getProfitLoss(Stock stock) {
        Holding h = holdings.get(stock.getSymbol());
        return (h == null) ? 0 : h.getProfitLoss(stock.getPrice());
    }
}
