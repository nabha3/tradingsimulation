package model;

/**
 * Observer interface. Any class that wants to be told when the market
 * prices change (for example a GUI table) implements this interface
 * and registers itself with the Market.
 */
public interface MarketListener {
    void onPricesChanged();
}
