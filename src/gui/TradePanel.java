package gui;

import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import exceptions.InvalidQuantityException;
import model.Holding;
import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * The Trade tab, shown to traders only.
 *
 * Left: a table of all stocks. Click a row to choose a stock.
 * Right: a form where the trader types a quantity and presses Buy or Sell.
 *
 * The actual buying and selling is done by the Trader class (buy and sell).
 * This panel only reads the input, calls those methods, and shows the result.
 * If the Trader throws an exception (not enough money, bad quantity, ...),
 * we catch it and show its message in red.
 *
 * It is also a MarketListener, so the prices in the table stay up to date.
 */
public class TradePanel extends JPanel implements MarketListener {
    private Trader trader;
    private Market market;
    private ActionListener onTradeDone;     // called after a successful trade

    private JTable table;
    private DefaultTableModel tableModel;
    private boolean refreshing = false;     // true while we are rebuilding the table
    private String selectedSymbol = null;

    private JLabel stockLabel;
    private JLabel priceLabel;
    private JTextField quantityField;
    private JLabel totalLabel;
    private JLabel ownedLabel;
    private JLabel walletLabel;
    private JLabel messageLabel;

    public TradePanel(Trader trader, Market market, ActionListener onTradeDone) {
        this.trader = trader;
        this.market = market;
        this.onTradeDone = onTradeDone;
        buildScreen();
        refreshTable();
        updateInfo();
        market.addListener(this);
    }

    // Creates the stock table (left) and the trading form (right).
    private void buildScreen() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: the table of stocks. Only one row can be selected at a time.
        String[] columns = {"Symbol", "Price ($)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;               // read-only
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // When the user clicks a row, remember which stock it is and update the form.
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (refreshing || e.getValueIsAdjusting()) {
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedSymbol = String.valueOf(tableModel.getValueAt(row, 0));
                    messageLabel.setText(" ");
                    updateInfo();
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Right: the form
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.setBorder(BorderFactory.createTitledBorder("Place an order"));

        stockLabel = new JLabel();
        priceLabel = new JLabel();
        quantityField = new JTextField();
        totalLabel = new JLabel();
        ownedLabel = new JLabel();
        walletLabel = new JLabel();
        messageLabel = new JLabel(" ");

        // show the new total while the user types
        quantityField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateInfo();
            }
        });

        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buy();
            }
        });
        JButton sellButton = new JButton("Sell");
        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sell();
            }
        });
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.add(buyButton);
        buttons.add(sellButton);

        form.add(stockLabel);
        form.add(priceLabel);
        form.add(new JLabel("Quantity:"));
        form.add(quantityField);
        form.add(totalLabel);
        form.add(ownedLabel);
        form.add(walletLabel);
        form.add(buttons);
        form.add(messageLabel);

        JPanel right = new JPanel(new BorderLayout());
        right.add(form, BorderLayout.NORTH);
        right.setPreferredSize(new Dimension(320, 100));
        add(right, BorderLayout.EAST);
    }

    // ------------------------------------------------------------ trading

    // Called when the Buy button is pressed.
    private void buy() {
        Stock stock = getSelectedStock();
        if (stock == null) {
            showMessage("Pick a stock from the list first.", Color.RED);
            return;
        }
        int quantity = readQuantity();
        if (quantity == Integer.MIN_VALUE) {
            return;                         // readQuantity already showed the error
        }

        try {
            trader.buy(stock, quantity);
            showMessage("Bought " + quantity + " " + stock.getSymbol()
                    + " at $" + String.format("%.2f", stock.getPrice()), new Color(0, 130, 0));
            tradeDone();
        } catch (InsufficientFundsException e) {
            showMessage(e.getMessage(), Color.RED);
        } catch (InvalidQuantityException e) {
            showMessage(e.getMessage(), Color.RED);
        }
    }

    // Called when the Sell button is pressed.
    private void sell() {
        Stock stock = getSelectedStock();
        if (stock == null) {
            showMessage("Pick a stock from the list first.", Color.RED);
            return;
        }
        int quantity = readQuantity();
        if (quantity == Integer.MIN_VALUE) {
            return;
        }

        try {
            trader.sell(stock, quantity);
            showMessage("Sold " + quantity + " " + stock.getSymbol()
                    + " at $" + String.format("%.2f", stock.getPrice()), new Color(0, 130, 0));
            tradeDone();
        } catch (InsufficientSharesException e) {
            showMessage(e.getMessage(), Color.RED);
        } catch (InvalidQuantityException e) {
            showMessage(e.getMessage(), Color.RED);
        }
    }

    // Turns the text in the quantity field into a number.
    // Returns Integer.MIN_VALUE (and shows a message) if it is not a whole number.
    private int readQuantity() {
        try {
            return Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Quantity must be a whole number, for example 5.", Color.RED);
            return Integer.MIN_VALUE;
        }
    }

    // After a successful trade: clear the field, refresh our labels, tell MainFrame.
    private void tradeDone() {
        quantityField.setText("");
        updateInfo();
        onTradeDone.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "trade done"));
    }

    private void showMessage(String text, Color color) {
        messageLabel.setForeground(color);
        messageLabel.setText("<html>" + text + "</html>");      // html lets long text wrap
    }

    // ------------------------------------------------------------ display

    // Returns the Stock the user clicked, or null if nothing is selected.
    private Stock getSelectedStock() {
        if (selectedSymbol == null) {
            return null;
        }
        return market.findStock(selectedSymbol);
    }

    // Refreshes the labels on the form.
    private void updateInfo() {
        Stock stock = getSelectedStock();
        if (stock == null) {
            stockLabel.setText("Stock: (select one from the list)");
            priceLabel.setText("Price: -");
            totalLabel.setText("Total: -");
            ownedLabel.setText("You own: -");
        } else {
            stockLabel.setText("Stock: " + stock.getSymbol());
            priceLabel.setText("Price: $" + String.format("%.2f", stock.getPrice()) + " per share");

            // Show the total cost if the quantity field holds a valid number
            String total = "-";
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity > 0) {
                    total = "$" + String.format("%,.2f", stock.getPrice() * quantity);
                }
            } catch (NumberFormatException e) {
                // nothing typed yet, or not a number: just show "-"
            }
            totalLabel.setText("Total: " + total);

            int owned = 0;
            for (Holding holding : trader.getHoldings()) {
                if (holding.getSymbol().equals(stock.getSymbol())) {
                    owned = holding.getQuantity();
                }
            }
            ownedLabel.setText("You own: " + owned + " shares");
        }
        walletLabel.setText("Wallet: $" + String.format("%,.2f", trader.getWalletBalance()));
    }

    // Rebuilds the stock table and keeps the selected row selected.
    private void refreshTable() {
        refreshing = true;
        tableModel.setRowCount(0);
        int rowToSelect = -1;
        for (Stock stock : market.getStocks()) {
            if (stock.getSymbol().equals(selectedSymbol)) {
                rowToSelect = tableModel.getRowCount();
            }
            tableModel.addRow(new Object[] {stock.getSymbol(), String.format("%,.2f", stock.getPrice())});
        }
        if (rowToSelect >= 0) {
            table.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
        refreshing = false;
    }

    // Called by the Market after every price update.
    @Override
    public void onPricesChanged() {
        refreshTable();
        updateInfo();
    }
}
