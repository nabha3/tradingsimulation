package gui;

import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;
import model.User;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * The main window shown after login.
 *
 * It has a header (who is logged in and the wallet), a tabbed pane in the
 * middle and a small status line at the bottom.
 *   - Market tab:    a table of all stocks, refreshed every 2 seconds
 *   - Trade tab:     buy and sell (traders only)
 *   - Portfolio tab: holdings, profit/loss and history (traders only)
 *
 * A Swing Timer asks the Market to change the prices. The Market then tells
 * every listener (Observer pattern), and MainFrame is one of them, so it
 * simply redraws the market table.
 */
public class MainFrame extends JFrame implements MarketListener {
    private static final int UPDATE_INTERVAL_MS = 2000;     // prices change every 2 seconds

    private User user;
    private Market market;

    private JLabel headerLabel;
    private JTabbedPane tabs;
    private DefaultTableModel tableModel;

    public MainFrame(User user, Market market) {
        this.user = user;
        this.market = market;
        buildScreen();

        tabs.addTab("Market", buildMarketTab());
        if (user instanceof Trader) {
            addTraderTabs((Trader) user);
        }

        refreshTable();
        refreshWallet();
        market.addListener(this);       // register as an observer
        startTimer();
    }

    // Creates the header, the tabbed pane and the status line.
    private void buildScreen() {
        setTitle("BullRun - " + user.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        headerLabel = new JLabel(" ");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(headerLabel, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel("Prices update every " + (UPDATE_INTERVAL_MS / 1000) + " seconds.");
        status.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(status, BorderLayout.SOUTH);

        setSize(800, 520);
        setLocationRelativeTo(null);
    }

    // Traders get two more tabs: Trade and Portfolio.
    private void addTraderTabs(Trader trader) {
        final PortfolioPanel portfolio = new PortfolioPanel(trader, market);
        TradePanel trade = new TradePanel(trader, market, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // a trade just happened: update the header and the portfolio tab
                refreshWallet();
                portfolio.refresh();
            }
        });
        tabs.addTab("Trade", trade);
        tabs.addTab("Portfolio", portfolio);
    }

    /** Adds one more tab to the window (used for any extra page, e.g. the admin page). */
    public void addPage(String name, JPanel page) {
        tabs.addTab(name, page);
    }

    /** Updates the header text. Called after a trade so the wallet is up to date. */
    public void refreshWallet() {
        String text = "Logged in as " + user.getName() + " (" + user.getRole() + ")";
        if (user instanceof Trader) {
            text = text + "      Wallet: $" + String.format("%,.2f", user.getWalletBalance());
        }
        headerLabel.setText(text);
    }

    // Builds the Market tab: one table with all the stocks.
    private JPanel buildMarketTab() {
        String[] columns = {"Symbol", "Price ($)", "Change", "Change %"};

        // The table should be read-only, so we override isCellEditable.
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Starts a Swing Timer that asks the Market to update prices every 2 seconds.
    private void startTimer() {
        Timer timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                market.updatePrices();
            }
        });
        timer.start();
    }

    // Called by the Market after every price update.
    @Override
    public void onPricesChanged() {
        refreshTable();
    }

    // Clears the table and fills it again from the current stock list.
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Stock s : market.getStocks()) {
            ArrayList<Double> history = s.getPriceHistory();
            double change = 0;
            double percent = 0;
            if (history.size() >= 2) {
                double previous = history.get(history.size() - 2);
                change = s.getPrice() - previous;
                percent = change / previous * 100;
            }
            tableModel.addRow(new Object[] {
                s.getSymbol(),
                String.format("%,.2f", s.getPrice()),
                String.format("%+.2f", change),         // %+ always shows the sign
                String.format("%+.2f%%", percent)
            });
        }
    }
}
