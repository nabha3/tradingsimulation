package model;

import java.util.ArrayList;

public class Stock implements Tradable {
    private String symbol;
    private double price;
    private ArrayList<Double> priceHistory;

    public Stock(String symbol, double startPrice) {
        this.symbol = symbol;
        this.price = startPrice;
        this.priceHistory = new ArrayList<>();
        this.priceHistory.add(startPrice);
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public ArrayList<Double> getPriceHistory() {
        return priceHistory;
    }

    public void setPrice(double newPrice) {
        this.price = newPrice;
        this.priceHistory.add(newPrice);
    }

    @Override
    public String toString() {
        return symbol + " @ " + String.format("%.2f", price);
    }
}
