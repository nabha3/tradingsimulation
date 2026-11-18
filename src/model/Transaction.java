package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A single entry in a trader's history, for example
 * "BUY 10 TCS at 100.00". Once created it is never modified,
 * which is why every field is final.
 */
public class Transaction {
    private final String type;          // "BUY" or "SELL"
    private final String symbol;
    private final int quantity;
    private final double price;         // price of one share at the time of the trade
    private final LocalDateTime timestamp;

    public Transaction(String type, String symbol, int quantity, double price) {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /** Total amount of money involved in this trade. */
    public double getTotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return timestamp.format(format) + "  " + type + " " + quantity + " " + symbol
                + " @ " + String.format("%.2f", price)
                + " (total " + String.format("%.2f", getTotal()) + ")";
    }
}
