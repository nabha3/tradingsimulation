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
 * The Portfolio page, shown to traders only.
 *
 * Top: three cards (wallet, value of all shares, total profit or loss).
 * Middle: a table of the shares the trader owns right now.
 * Bottom: a table of every trade made so far (newest first).
 *
 * It is a MarketListener, so profit/loss changes live as prices move.
 * TradePanel/MainFrame call refresh() after a trade.
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

    // Creates the title, the cards and the two tables.
    private void buildScreen() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        // Top: title and the three cards
        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Portfolio");
        title.setFont(Theme.font(Font.BOLD, 26));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("What you own and how it is doing");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        top.add(titles, BorderLayout.NORTH);

        walletLabel = new JLabel("-");
        valueLabel = new JLabel("-");
        profitLabel = new JLabel("-");
        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.add(Theme.createStatCard("WALLET", walletLabel));
        cards.add(Theme.createStatCard("VALUE OF SHARES", valueLabel));
        cards.add(Theme.createStatCard("PROFIT / LOSS", profitLabel));
        top.add(cards, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // Centre: the two tables, one above the other
        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);

        String[] holdingColumns = {"SYMBOL", "QUANTITY", "AVG BUY PRICE", "PRICE NOW", "VALUE", "PROFIT / LOSS"};
        holdingsModel = createModel(holdingColumns);
        tables.add(buildTableSection("Holdings", holdingsModel));

        String[] historyColumns = {"TIME", "TYPE", "SYMBOL", "QUANTITY", "PRICE", "TOTAL"};
        historyModel = createModel(historyColumns);
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

    // A small heading with a table underneath.
    private JPanel buildTableSection(String heading, DefaultTableModel model) {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);

        JLabel label = new JLabel(heading);
        label.setFont(Theme.font(Font.BOLD, 15));
        label.setForeground(Theme.TEXT);
        section.add(label, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(190);     // wide enough for the time
        Theme.styleTable(table);
        table.setRowHeight(34);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.SURFACE);
        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    /** Redraws the cards and both tables from the trader's current data. */
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
                    h.getSymbol(), h.getQuantity(), Theme.money(h.getAverageBuyPrice()), "-", "-", "-"
                });
            } else {
                double value = h.getQuantity() * stock.getPrice();
                double profit = h.getProfitLoss(stock.getPrice());
                totalValue += value;
                totalProfit += profit;
                holdingsModel.addRow(new Object[] {
                    h.getSymbol(), h.getQuantity(), Theme.money(h.getAverageBuyPrice()),
                    Theme.money(stock.getPrice()), Theme.money(value), signedMoney(profit)
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
                t.getQuantity(), Theme.money(t.getPrice()), Theme.money(t.getTotal())
            });
        }

        // ----- the three cards
        walletLabel.setText(Theme.money(trader.getWalletBalance()));
        valueLabel.setText(Theme.money(totalValue));
        profitLabel.setText(signedMoney(totalProfit));
        if (totalProfit > 0.005) {
            profitLabel.setForeground(Theme.GREEN);
        } else if (totalProfit < -0.005) {
            profitLabel.setForeground(Theme.RED);
        } else {
            profitLabel.setForeground(Theme.TEXT);
        }
    }

    // Money with a + or - in front, for example "+ ₹ 120.00" or "- ₹ 45.50".
    private String signedMoney(double amount) {
        if (amount > 0.005) {
            return "+" + Theme.money(amount);
        } else if (amount < -0.005) {
            return "-" + Theme.money(-amount);
        }
        return Theme.money(0);
    }

    // Called by the Market after every price update.
    @Override
    public void onPricesChanged() {
        refresh();
    }
}
