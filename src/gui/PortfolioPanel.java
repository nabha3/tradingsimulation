package gui;

import model.Holding;
import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;
import model.Transaction;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The Portfolio tab, shown to traders only.
 *
 * Top: three lines (wallet, value of all shares, total profit or loss).
 * Middle: a table of the shares the trader owns right now.
 * Bottom: a table of every trade made so far (newest first).
 *
 * It is a MarketListener, so profit/loss changes live as prices move.
 * MainFrame calls refresh() after a trade.
 */
public class PortfolioPanel extends JPanel implements MarketListener {
    private Trader trader;
    private Market market;

    private JLabel walletLabel;
    private JLabel valueLabel;
    private JLabel profitLabel;
    private DefaultTableModel holdingsModel;
    private DefaultTableModel historyModel;

    public PortfolioPanel(Trader trader, Market market) {
        this.trader = trader;
        this.market = market;
        buildScreen();
        refresh();
        market.addListener(this);
    }

    // Creates the summary lines and the two tables.
    private void buildScreen() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: three labels side by side
        walletLabel = new JLabel();
        valueLabel = new JLabel();
        profitLabel = new JLabel();
        Font bold = new Font("SansSerif", Font.BOLD, 14);
        walletLabel.setFont(bold);
        valueLabel.setFont(bold);
        profitLabel.setFont(bold);
        JPanel summary = new JPanel(new GridLayout(1, 3, 10, 0));
        summary.setBorder(BorderFactory.createTitledBorder("Summary"));
        summary.add(walletLabel);
        summary.add(valueLabel);
        summary.add(profitLabel);
        add(summary, BorderLayout.NORTH);

        // Centre: the two tables, one above the other
        String[] holdingColumns = {"Symbol", "Quantity", "Avg buy price ($)", "Price now ($)", "Value ($)", "Profit / Loss ($)"};
        holdingsModel = createModel(holdingColumns);
        String[] historyColumns = {"Time", "Type", "Symbol", "Quantity", "Price ($)", "Total ($)"};
        historyModel = createModel(historyColumns);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 10));
        tables.add(buildTableSection("Holdings", holdingsModel));
        tables.add(buildTableSection("Trade history", historyModel));
        add(tables, BorderLayout.CENTER);
    }

    // A read-only table model with the given column names.
    private DefaultTableModel createModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // A table inside a scroll pane, with a title around it.
    private JScrollPane buildTableSection(String heading, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(22);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(heading));
        return scroll;
    }

    /** Redraws the summary and both tables from the trader's current data. */
    public void refresh() {
        // ----- holdings table and totals
        holdingsModel.setRowCount(0);
        double totalValue = 0;
        double totalProfit = 0;
        for (Holding h : trader.getHoldings()) {
            Stock stock = market.findStock(h.getSymbol());
            if (stock == null) {
                // the admin removed this stock from the market, so there is no price any more
                holdingsModel.addRow(new Object[] {
                    h.getSymbol(), h.getQuantity(), String.format("%.2f", h.getAverageBuyPrice()), "-", "-", "-"
                });
            } else {
                double value = h.getQuantity() * stock.getPrice();
                double profit = h.getProfitLoss(stock.getPrice());
                totalValue += value;
                totalProfit += profit;
                holdingsModel.addRow(new Object[] {
                    h.getSymbol(), h.getQuantity(), String.format("%.2f", h.getAverageBuyPrice()),
                    String.format("%.2f", stock.getPrice()), String.format("%,.2f", value),
                    String.format("%+,.2f", profit)
                });
            }
        }

        // ----- trade history table (newest first, so we loop backwards)
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        historyModel.setRowCount(0);
        ArrayList<Transaction> history = trader.getHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            Transaction t = history.get(i);
            historyModel.addRow(new Object[] {
                t.getTimestamp().format(format), t.getType(), t.getSymbol(),
                t.getQuantity(), String.format("%.2f", t.getPrice()), String.format("%,.2f", t.getTotal())
            });
        }

        // ----- the summary labels
        walletLabel.setText("Wallet: $" + String.format("%,.2f", trader.getWalletBalance()));
        valueLabel.setText("Value of shares: $" + String.format("%,.2f", totalValue));
        profitLabel.setText("Profit / Loss: " + signedDollars(totalProfit));
    }

    // Formats an amount with the sign in front of the dollar sign: +$12.50 or -$12.50.
    private String signedDollars(double amount) {
        if (amount < 0) {
            return "-$" + String.format("%,.2f", -amount);
        }
        return "+$" + String.format("%,.2f", amount);
    }

    // Called by the Market after every price update.
    @Override
    public void onPricesChanged() {
        refresh();
    }
}
