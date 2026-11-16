package model;

// Anything that can be bought or sold must provide a symbol and a price.
public interface Tradable {
    String getSymbol();
    double getPrice();
}
