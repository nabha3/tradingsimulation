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
import java.awt.Font;
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
 * The Trade page, shown to traders only.
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

    // Creates the title, the stock table and the trading form.
    private void buildScreen() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        // Title
        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Trade");
        title.setFont(Theme.font(Font.BOLD, 26));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Pick a stock, enter a quantity, then buy or sell");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        add(titles, BorderLayout.NORTH);

        // Left: table, right: form
        JPanel body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(buildTable());
        body.add(buildForm());
        add(body, BorderLayout.CENTER);
    }

    // The table of stocks. Only one row can be selected at a time.
    private JScrollPane buildTable() {
        String[] columns = {"SYMBOL", "PRICE"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;               // read-only
            }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.SURFACE);
        return scroll;
    }

    // The form on the right: selected stock, quantity, info lines, buttons, message.
    private JPanel buildForm() {
        Theme.CardPanel card = new Theme.CardPanel();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createEmptyBorder(16, 26, 14, 26));

        // Top: which stock is selected, and its price
        JPanel top = new JPanel(new GridLayout(3, 1, 0, 0));
        top.setOpaque(false);
        top.add(Theme.createCaption("SELECTED STOCK"));
        stockLabel = new JLabel("Select a stock");
        stockLabel.setFont(Theme.font(Font.BOLD, 26));
        stockLabel.setForeground(Theme.TEXT);
        top.add(stockLabel);
        priceLabel = new JLabel("-");
        priceLabel.setFont(Theme.font(Font.PLAIN, 15));
        priceLabel.setForeground(Theme.GREEN);
        top.add(priceLabel);
        card.add(top, BorderLayout.NORTH);

        // Middle: quantity field and information lines
        JPanel middle = new JPanel(new BorderLayout(0, 6));
        middle.setOpaque(false);
        JPanel quantityBlock = new JPanel(new BorderLayout(0, 6));
        quantityBlock.setOpaque(false);
        quantityBlock.add(Theme.createCaption("QUANTITY"), BorderLayout.NORTH);
        quantityField = new JTextField();
        Theme.styleField(quantityField);
        quantityField.setPreferredSize(new Dimension(100, 38));
        quantityField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateInfo();               // show the new total while typing
            }
        });
        quantityBlock.add(quantityField, BorderLayout.CENTER);
        middle.add(quantityBlock, BorderLayout.NORTH);

        JPanel infoLines = new JPanel(new GridLayout(3, 1, 0, 0));
        infoLines.setOpaque(false);
        totalLabel = createInfoLabel();
        ownedLabel = createInfoLabel();
        walletLabel = createInfoLabel();
        infoLines.add(totalLabel);
        infoLines.add(ownedLabel);
        infoLines.add(walletLabel);
        middle.add(infoLines, BorderLayout.CENTER);
        card.add(middle, BorderLayout.CENTER);

        // Bottom: Buy and Sell buttons, and the message
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        JButton buyButton = Theme.createPrimaryButton("Buy");
        JButton sellButton = Theme.createButton("Sell", Theme.RED);
        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buy();
            }
        });
        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sell();
            }
        });
        buttons.add(buyButton);
        buttons.add(sellButton);
        bottom.add(buttons, BorderLayout.NORTH);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(Theme.font(Font.PLAIN, 13));
        messageLabel.setPreferredSize(new Dimension(100, 34));
        bottom.add(messageLabel, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(Theme.font(Font.PLAIN, 14));
        label.setForeground(Theme.MUTED);
        return label;
    }

    // ------------------------------------------------------------ trading

    // Called when the Buy button is pressed.
    private void buy() {
        Stock stock = getSelectedStock();
        if (stock == null) {
            showMessage("Pick a stock from the list first.", Theme.RED);
            return;
        }
        int quantity = readQuantity();
        if (quantity == Integer.MIN_VALUE) {
            return;                         // readQuantity already showed the error
        }

        try {
            trader.buy(stock, quantity);
            showMessage("Bought " + quantity + " " + stock.getSymbol()
                    + " at " + Theme.money(stock.getPrice()), Theme.GREEN);
            tradeDone();
        } catch (InsufficientFundsException e) {
            showMessage(e.getMessage(), Theme.RED);
        } catch (InvalidQuantityException e) {
            showMessage(e.getMessage(), Theme.RED);
        }
    }

    // Called when the Sell button is pressed.
    private void sell() {
        Stock stock = getSelectedStock();
        if (stock == null) {
            showMessage("Pick a stock from the list first.", Theme.RED);
            return;
        }
        int quantity = readQuantity();
        if (quantity == Integer.MIN_VALUE) {
            return;
        }

        try {
            trader.sell(stock, quantity);
            showMessage("Sold " + quantity + " " + stock.getSymbol()
                    + " at " + Theme.money(stock.getPrice()), Theme.GREEN);
            tradeDone();
        } catch (InsufficientSharesException e) {
            showMessage(e.getMessage(), Theme.RED);
        } catch (InvalidQuantityException e) {
            showMessage(e.getMessage(), Theme.RED);
        }
    }

    // Turns the text in the quantity field into a number.
    // Returns Integer.MIN_VALUE (and shows a message) if it is not a whole number.
    private int readQuantity() {
        try {
            return Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Quantity must be a whole number, for example 5.", Theme.RED);
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
            stockLabel.setText("Select a stock");
            priceLabel.setText("-");
            totalLabel.setText("Total:  -");
            ownedLabel.setText("You own:  -");
        } else {
            stockLabel.setText(stock.getSymbol());
            priceLabel.setText(Theme.money(stock.getPrice()) + " per share");

            // Show the total cost if the quantity field holds a valid number
            String total = "-";
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity > 0) {
                    total = Theme.money(stock.getPrice() * quantity);
                }
            } catch (NumberFormatException e) {
                // nothing typed yet, or not a number: just show "-"
            }
            totalLabel.setText("Total:  " + total);

            int owned = 0;
            for (Holding h : trader.getHoldings()) {
                if (h.getSymbol().equals(stock.getSymbol())) {
                    owned = h.getQuantity();
                }
            }
            ownedLabel.setText("You own:  " + owned + " shares");
        }
        walletLabel.setText("Wallet:  " + Theme.money(trader.getWalletBalance()));
    }

    // Rebuilds the stock table and keeps the selected row selected.
    private void refreshTable() {
        refreshing = true;
        tableModel.setRowCount(0);
        int rowToSelect = -1;
        for (Stock s : market.getStocks()) {
            if (s.getSymbol().equals(selectedSymbol)) {
                rowToSelect = tableModel.getRowCount();
            }
            tableModel.addRow(new Object[] {s.getSymbol(), Theme.money(s.getPrice())});
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

    /** Called by MainFrame (for example after a trade elsewhere) to redraw the labels. */
    public void refresh() {
        updateInfo();
    }
}
