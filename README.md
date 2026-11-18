# 🐂 BullRun
### Virtual Stock Trading and Portfolio Simulator

Practice trading stocks with virtual money. Learn how the market works without risking a rupee.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![GUI](https://img.shields.io/badge/GUI-Swing-blue)
![Status](https://img.shields.io/badge/status-in%20progress-yellow)
![Course](https://img.shields.io/badge/CS%20F213-OOP-green)

**Team TLE:** Adwait Misra · Muhammad Owais Husain Puthuveetil · Kaviish Agrawal · Nabha Birjerane

---

## What is BullRun?

BullRun is a Java application that simulates a small stock market.

- Every trader starts with a **wallet** of virtual cash.
- Stock prices **change over time**.
- Traders **buy and sell** shares and track their profit or loss.
- An **admin** decides which stocks are listed on the market.

## How a trade works

```mermaid
flowchart LR
    A[Log in] --> B[View market prices]
    B --> C{Buy or sell?}
    C -->|Buy| D[Wallet decreases<br/>Shares increase]
    C -->|Sell| E[Wallet increases<br/>Shares decrease]
    D --> F[Portfolio and profit/loss update]
    E --> F
    C -->|Not enough cash or shares| G[Exception thrown<br/>Error message shown]
```

## Project design

```mermaid
classDiagram
    class User {
        <<abstract>>
        -id
        -name
        -password
        -walletBalance
        +deposit()
        +withdraw()
        +checkPassword()
        +getRole()*
    }
    class Trader {
        -holdings
        -history
        +buy()
        +sell()
        +getPortfolioValue()
        +getProfitLoss()
    }
    class Admin {
        +addStock()
        +removeStock()
    }
    class Tradable {
        <<interface>>
        +getSymbol()
        +getPrice()
    }
    class Stock {
        -symbol
        -price
        -priceHistory
        +setPrice()
    }
    class Market {
        -stocks
        -listeners
        +updatePrices()
        +addListener()
    }
    class MarketListener {
        <<interface>>
        +onPricesChanged()
    }
    class Holding {
        -symbol
        -quantity
        -averageBuyPrice
    }
    class Transaction {
        -type
        -symbol
        -quantity
        -price
        -timestamp
    }
    User <|-- Trader
    User <|-- Admin
    Tradable <|.. Stock
    Trader "1" o-- "*" Holding : owns
    Trader "1" o-- "*" Transaction : records
    Trader ..> Tradable : buys / sells
    Admin ..> Stock : manages
    Market "1" o-- "*" Stock : lists
    Market ..> MarketListener : notifies
```

| Class | Role |
|---|---|
| `User` | Abstract parent for anyone who logs in: name, password and wallet |
| `Trader` | Extends `User`. Buys and sells stocks and owns holdings |
| `Admin` | Extends `User`. Adds or removes stocks from the market |
| `Tradable` | Interface for anything with a symbol and a price |
| `Stock` | Implements `Tradable`. Keeps its price history |
| `Market` | Holds all stocks and moves their prices randomly on each update |
| `MarketListener` | Interface for anything that wants to hear about price changes |
| `Holding` | A position, such as "5 shares of TCS at an average of 100" |
| `Transaction` | One history entry, such as "BUY 10 TCS @ 100 at 12:30" |
| `InsufficientFundsException` | Thrown when a buy costs more than the wallet holds |
| `InsufficientSharesException` | Thrown when a sale exceeds the shares owned |

## OOP concepts used

| Concept | Where |
|---|---|
| Abstract class | `User`, with the abstract method `getRole()` |
| Inheritance | `Trader` and `Admin` extend `User` |
| Polymorphism | A `User` variable can hold a `Trader` or an `Admin` |
| Interface | `Tradable` (implemented by `Stock`) and `MarketListener` |
| Observer pattern | `Market` notifies its `MarketListener`s when prices change |
| Anonymous class | The listener created in `App.java` |
| Iterator | Removing a stock safely in `Market` and `Admin` |
| Encapsulation | Private fields with getters, such as `walletBalance` and the final fields of `Transaction` |
| Method overriding | `getRole()` and `toString()` |
| Custom exceptions | The `exceptions` package |
| Exception handling | `try` / `catch` in `App.java` |
| Collections | `ArrayList` for stocks, holdings and transactions |
| Packages | `model` and `exceptions` |
| Constructors, `this`, `super` | All model classes |

## Roadmap

```mermaid
flowchart LR
    A["✅ Core model"] --> B["✅ Transaction history"]
    B --> C["✅ Market with moving prices"]
    C --> D["⬜ Swing GUI"]
    D --> E["⬜ Live price chart"]
    E --> F["⬜ Save and load data"]
    F --> G["⬜ Report and demo"]
```

- [x] Core classes: `User`, `Trader`, `Admin`, `Stock`, `Holding`
- [x] Custom exceptions for invalid trades
- [x] `Transaction` class and per-trader trade history
- [x] `Market` class with random price movement and Observer notifications
- [ ] Swing `Timer` that calls `Market.updatePrices()` automatically
- [ ] Swing GUI: login, market table, buy/sell, portfolio, admin panel
- [ ] Live price chart
- [ ] Save and load users, holdings and transactions
- [ ] Input validation and login exceptions

## Getting started

Requires **JDK 21 or newer**.

```bash
git clone https://github.com/nabha3/tradingsimulation.git
cd tradingsimulation
javac -d bin src/App.java src/model/*.java src/exceptions/*.java
java -cp bin App
```

Expected output (timestamps will differ):

```text
Trader Nabha (wallet 10000.00)
After buying 10 TCS: Trader Nabha (wallet 9000.00)
After selling 5 TCS: Trader Nabha (wallet 9500.00)
[TCS: 5 shares, avg 100.00]
TCS now TCS @ 120.00, P/L = 100.0
Portfolio value = 600.0
Caught: Not enough money: need 120000.00 but wallet has 9500.00
Caught: Not enough shares of TCS: you own 5 but tried to sell 99
--- Market ---
Prices changed: [TCS @ 101.88, INFY @ 52.24]
(two more price updates, values are random)
--- History ---
18-11-2026 12:36:58  BUY 10 TCS @ 100.00 (total 1000.00)
18-11-2026 12:36:58  SELL 5 TCS @ 100.00 (total 500.00)
Trader -> Trader Nabha (wallet 9500.00)
Admin -> Admin Boss (wallet 0.00)
```

The project is currently a console demo. The GUI is planned next.

## Project structure

```text
tradingsimulation/
├── src/
│   ├── App.java                 # console demo (entry point)
│   ├── model/
│   │   ├── User.java
│   │   ├── Trader.java
│   │   ├── Admin.java
│   │   ├── Stock.java
│   │   ├── Holding.java
│   │   ├── Market.java
│   │   ├── MarketListener.java
│   │   ├── Transaction.java
│   │   └── Tradable.java
│   └── exceptions/
│       ├── InsufficientFundsException.java
│       └── InsufficientSharesException.java
└── README.md
```

*Educational project. Not financial advice, and no real money is involved.*
