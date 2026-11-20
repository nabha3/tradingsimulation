package model;

import exceptions.InvalidQuantityException;

public class Holding {
    private String symbol;
    private int quantity;
    private double averageBuyPrice;

    public Holding(String symbol, int quantity, double buyPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageBuyPrice = buyPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    
    public void addShares(int extraQuantity, double buyPrice) {
        double totalCost = averageBuyPrice * quantity + buyPrice * extraQuantity;
        quantity += extraQuantity;
        averageBuyPrice = totalCost / quantity;
    }

    
    // Safety check: we cannot remove zero, a negative number, or more shares than we own.
    public void removeShares(int soldQuantity) throws InvalidQuantityException {
        if (soldQuantity <= 0 || soldQuantity > quantity) {
            throw new InvalidQuantityException("Cannot remove " + soldQuantity + " shares from " + quantity);
        }
        quantity -= soldQuantity;
    }

    public double getProfitLoss(double currentPrice) {
        return (currentPrice - averageBuyPrice) * quantity;
    }

    @Override
    public String toString() {
        return symbol + ": " + quantity + " shares, avg " + String.format("%.2f", averageBuyPrice);
    }
}
