package gui;

import model.Market;
import model.MarketListener;
import model.Stock;
import model.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * The main window shown after login.
 *
 * It has a header (who is logged in) and a tabbed area. The first tab shows
 * the market table, which refreshes automatically every few seconds.
 * More tabs (trading, portfolio, chart, admin) will be added to the tabbed
 * pane later.
 *
 * MainFrame implements MarketListener, so the Market tells it whenever the
 * prices change (Observer pattern) and it simply redraws the table.
 */
public class MainFrame extends JFrame implements MarketListener {
    private static final int UPDATE_INTERVAL_MS = 2000;   // prices change every 2 seconds

    private User user;
    private Market market;

    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private Timer timer;

    public MainFrame(User user, Market market) {
        this.user = user;
        this.market = market;
        buildScreen();
        refreshTable();                 // show the starting prices straight away
        market.addListener(this);       // register as an observer
        startTimer();
    }

    // Creates the components and adds them to the window.
    private void buildScreen() {
        setTitle("BullRun - " + user.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top: who is logged in
        JLabel header = new JLabel(
                "Logged in as " + user.getName() + " (" + user.getRole() + ")", SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        add(header, BorderLayout.NORTH);

        // Centre: tabbed pane. For now it only has the Market tab.
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Market", buildMarketTab());
        add(tabs, BorderLayout.CENTER);

        // Bottom: small status line
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(statusLabel, BorderLayout.SOUTH);

        setSize(700, 450);
        setLocationRelativeTo(null);
    }

    // Builds the panel that holds the market table.
    private JPanel buildMarketTab() {
        String[] columns = {"Symbol", "Price", "Change"};

        // The table should be read-only, so we override isCellEditable.
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getColumnModel().getColumn(2).setCellRenderer(new ChangeRenderer());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // Starts a Swing Timer that asks the Market to update prices every 2 seconds.
    private void startTimer() {
        timer = new Timer(UPDATE_INTERVAL_MS, new ActionListener() {
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
            if (history.size() >= 2) {
                change = s.getPrice() - history.get(history.size() - 2);
            }
            String changeText = String.format("%+.2f", change);   // %+ always shows the sign
            tableModel.addRow(new Object[] {
                s.getSymbol(), String.format("%.2f", s.getPrice()), changeText
            });
        }
        statusLabel.setText("Prices update every " + (UPDATE_INTERVAL_MS / 1000) + " seconds.");
    }

    /**
     * Inner class that colours the Change column: green for a rise,
     * red for a fall, and the normal colour if nothing changed.
     */
    private static class ChangeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            String text = String.valueOf(value);
            if (text.startsWith("+") && !text.equals("+0.00")) {
                c.setForeground(new Color(0, 140, 0));
            } else if (text.startsWith("-") && !text.equals("-0.00")) {
                c.setForeground(Color.RED);
            } else {
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}
