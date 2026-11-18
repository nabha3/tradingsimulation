package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * The stock market. It owns the list of stocks, moves their prices by a
 * small random amount each time updatePrices() is called, and then tells
 * every registered listener (Observer pattern).
 *
 * The Market does not know anything about the GUI. Later, a Swing Timer
 * will simply call updatePrices() every second or so.
 */
public class Market {
    private static final double MAX_CHANGE = 0.05;   // a price moves at most 5% per update
    private static final double MIN_PRICE = 1.00;    // a price never drops below this

    private ArrayList<Stock> stocks;
    private ArrayList<MarketListener> listeners;
    private Random random;

    public Market() {
        this.stocks = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.random = new Random();
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public void addStock(Stock stock) {
        stocks.add(stock);
    }

    /** Removes the stock with the given symbol. Returns true if it was found. */
    public boolean removeStock(String symbol) {
        Iterator<Stock> it = stocks.iterator();
        while (it.hasNext()) {
            if (it.next().getSymbol().equals(symbol)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /** Returns the stock with the given symbol, or null if there is none. */
    public Stock findStock(String symbol) {
        for (Stock s : stocks) {
            if (s.getSymbol().equals(symbol)) {
                return s;
            }
        }
        return null;
    }

    public void addListener(MarketListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MarketListener listener) {
        listeners.remove(listener);
    }

    /** Moves every stock price up or down by a random percentage, then notifies listeners. */
    public void updatePrices() {
        for (Stock s : stocks) {
            // random.nextDouble() is between 0 and 1, so this gives a value between -5% and +5%
            double change = (random.nextDouble() * 2 - 1) * MAX_CHANGE;
            double newPrice = s.getPrice() * (1 + change);
            newPrice = Math.round(newPrice * 100) / 100.0;   // keep two decimal places
            if (newPrice < MIN_PRICE) {
                newPrice = MIN_PRICE;
            }
            s.setPrice(newPrice);
        }
        notifyListeners();
    }

    private void notifyListeners() {
        for (MarketListener l : listeners) {
            l.onPricesChanged();
        }
    }
}
