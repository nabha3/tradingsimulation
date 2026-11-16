package model;

import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import java.util.ArrayList;
import java.util.List;

public class Trader extends User {
    private ArrayList<Holding> holdings;

    public Trader(String id, String name, String password, double walletBalance) {
        super(id, name, password, walletBalance);
        this.holdings = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Trader";
    }

    public ArrayList<Holding> getHoldings() {
        return holdings;
    }

    private Holding findHolding(String symbol) {
        for (Holding h : holdings) {
            if (h.getSymbol().equals(symbol)) {
                return h;
            }
        }
        return null;
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
        Holding existing = findHolding(stock.getSymbol());
        if (existing == null) {
            holdings.add(new Holding(stock.getSymbol(), quantity, stock.getPrice()));
        } else {
            existing.addShares(quantity, stock.getPrice());
        }
    }

    public void sell(Tradable stock, int quantity) throws InsufficientSharesException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        Holding existing = findHolding(stock.getSymbol());
        int owned = (existing == null) ? 0 : existing.getQuantity();
        if (quantity > owned) {
            throw new InsufficientSharesException(
                "Not enough shares of " + stock.getSymbol() + ": you own " + owned
                + " but tried to sell " + quantity);
        }
        deposit(stock.getPrice() * quantity);
        existing.removeShares(quantity);
        if (existing.getQuantity() == 0) {
            holdings.remove(existing);
        }
    }

    public double getPortfolioValue(List<Stock> market) {
        double total = 0;
        for (Stock s : market) {
            Holding h = findHolding(s.getSymbol());
            if (h != null) {
                total += h.getQuantity() * s.getPrice();
            }
        }
        return total;
    }

    public double getProfitLoss(Stock stock) {
        Holding h = findHolding(stock.getSymbol());
        return (h == null) ? 0 : h.getProfitLoss(stock.getPrice());
    }
}