package model;

// Abstract: you can create a Trader or an Admin, but never a plain User.
public abstract class User {
    private String id;
    private String name;
    private String password;
    private double walletBalance;

    public User(String id, String name, String password, double walletBalance) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.walletBalance = walletBalance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    public void deposit(double amount) {
        walletBalance += amount;
    }

    public void withdraw(double amount) {
        walletBalance -= amount;
    }

    // Each subclass says what kind of user it is (method overriding).
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " " + name + " (wallet " + String.format("%.2f", walletBalance) + ")";
    }
}
