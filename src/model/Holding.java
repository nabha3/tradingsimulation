package model;

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

    // Buying more: recalculate the average price.
    // Example: 10 shares at 100 + 10 shares at 200 -> average 150.
    public void addShares(int extraQuantity, double buyPrice) {
        double totalCost = averageBuyPrice * quantity + buyPrice * extraQuantity;
        quantity += extraQuantity;
        averageBuyPrice = totalCost / quantity;
    }

    // Selling: the average buy price does not change.
    public void removeShares(int soldQuantity) {
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
