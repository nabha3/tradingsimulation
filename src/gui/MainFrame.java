package gui;

import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;
import model.User;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * The main window shown after login.
 *
 * Layout:
 *   - a header with the logo, the page buttons (Market, ...) and the user info
 *   - the page area in the middle (a CardLayout shows one page at a time)
 *   - a small status bar at the bottom
 *
 * Other pages (trading, portfolio, chart, admin) can be added with addPage().
 *
 * MainFrame implements MarketListener, so the Market tells it whenever the
 * prices change (Observer pattern) and it simply redraws the table.
 */
public class MainFrame extends JFrame implements MarketListener {
    private static final int UPDATE_INTERVAL_MS = 2000;   // prices change every 2 seconds

    private User user;
    private Market market;

    private JPanel navPanel;                 // holds the page buttons in the header
    private JPanel pagePanel;                // holds the pages (CardLayout)
    private CardLayout cards;
    private ArrayList<NavButton> navButtons;

    private JLabel walletLabel;
    private DefaultTableModel tableModel;
    private Timer timer;

    public MainFrame(User user, Market market) {
        this.user = user;
        this.market = market;
        this.navButtons = new ArrayList<>();
        buildScreen();
        addPage("Market", buildMarketPage());
        refreshTable();                 // show the starting prices straight away
        market.addListener(this);       // register as an observer
        startTimer();
    }

    // Creates the header, the page area and the status bar.
    private void buildScreen() {
        setTitle("BullRun - " + user.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        cards = new CardLayout();
        pagePanel = new JPanel(cards);
        pagePanel.setBackground(Theme.BACKGROUND);
        add(pagePanel, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);

        setSize(960, 620);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
    }

    // Header: logo and page buttons on the left, user info on the right.
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 14));
        left.setOpaque(false);
        left.add(Theme.createLogo(22));
        navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navPanel.setOpaque(false);
        left.add(navPanel);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 20));
        right.setOpaque(false);
        walletLabel = new JLabel();
        walletLabel.setFont(Theme.font(Font.BOLD, 15));
        walletLabel.setForeground(Theme.GREEN);
        JLabel userLabel = new JLabel(user.getName() + "  |  " + user.getRole());
        userLabel.setFont(Theme.font(Font.PLAIN, 13));
        userLabel.setForeground(Theme.MUTED);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));   // a little breathing room
        right.add(walletLabel);
        right.add(userLabel);
        header.add(right, BorderLayout.EAST);

        refreshWallet();
        return header;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 24, 6, 24)));
        JLabel status = new JLabel("● Live   |   prices update every "
                + (UPDATE_INTERVAL_MS / 1000) + " seconds");
        status.setFont(Theme.font(Font.PLAIN, 12));
        status.setForeground(Theme.MUTED);
        bar.add(status, BorderLayout.WEST);
        return bar;
    }

    /**
     * Adds a page to the window and a button for it in the header.
     * The first page that is added is shown straight away.
     */
    public void addPage(String name, JPanel page) {
        pagePanel.add(page, name);

        final String pageName = name;
        NavButton button = new NavButton(name);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(pageName);
            }
        });
        navButtons.add(button);
        navPanel.add(button);
        navPanel.revalidate();

        if (navButtons.size() == 1) {
            showPage(name);
        }
    }

    // Shows one page and highlights its button.
    private void showPage(String name) {
        cards.show(pagePanel, name);
        for (NavButton b : navButtons) {
            b.setActive(b.getText().equals(name));
        }
    }

    // Shows the trader's wallet balance in the header (admins have no wallet).
    // Other panels can call this after a trade.
    public void refreshWallet() {
        if (user instanceof Trader) {
            walletLabel.setText("Wallet  " + Theme.RUPEE + " " + String.format("%,.2f", user.getWalletBalance()));
        } else {
            walletLabel.setText("");
        }
    }

    // Builds the Market page: a title and the price table.
    private JPanel buildMarketPage() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(Theme.BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Market");
        title.setFont(Theme.font(Font.BOLD, 24));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Live prices of all listed stocks");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        page.add(titles, BorderLayout.NORTH);

        String[] columns = {"SYMBOL", "PRICE", "CHANGE", "CHANGE %"};

        // The table should be read-only, so we override isCellEditable.
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Theme.SURFACE);
        table.setRowSelectionAllowed(false);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(100, 38));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.SURFACE);
        page.add(scroll, BorderLayout.CENTER);

        return page;
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
            double percent = 0;
            if (history.size() >= 2) {
                double previous = history.get(history.size() - 2);
                change = s.getPrice() - previous;
                percent = change / previous * 100;
            }

            String changeText;
            String percentText;
            if (Math.abs(change) < 0.005) {
                changeText = "0.00";
                percentText = "0.00%";
            } else if (change > 0) {
                changeText = Theme.ARROW_UP + " " + String.format("%.2f", change);
                percentText = String.format("+%.2f%%", percent);
            } else {
                changeText = Theme.ARROW_DOWN + " " + String.format("%.2f", -change);
                percentText = String.format("%.2f%%", percent);
            }

            tableModel.addRow(new Object[] {
                s.getSymbol(), Theme.RUPEE + " " + String.format("%,.2f", s.getPrice()),
                changeText, percentText
            });
        }
    }

    /**
     * Inner class: a flat header button with a green underline when it is
     * the page that is currently shown.
     */
    private static class NavButton extends JButton {
        private boolean active = false;

        NavButton(String text) {
            super(text);
            setFont(Theme.font(Font.BOLD, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(22, 12, 22, 12));
            setActive(false);
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Theme.TEXT : Theme.MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                g.setColor(Theme.GREEN);
                g.fillRect(8, getHeight() - 3, getWidth() - 16, 3);
            }
        }
    }

    /**
     * Inner class: draws the table cells. Rows alternate in colour,
     * the symbol is bold, numbers are right-aligned, and rises are green
     * while falls are red.
     */
    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            String text = String.valueOf(value);

            setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

            if (column == 0) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(Theme.font(Font.BOLD, 14));
                setForeground(Theme.TEXT);
            } else {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(Theme.font(Font.PLAIN, 14));
                setForeground(Theme.TEXT);
                if (column >= 2) {
                    if (text.startsWith(Theme.ARROW_UP) || text.startsWith("+")) {
                        setForeground(Theme.GREEN);
                    } else if (text.startsWith(Theme.ARROW_DOWN) || text.startsWith("-")) {
                        setForeground(Theme.RED);
                    } else {
                        setForeground(Theme.MUTED);
                    }
                }
            }
            return this;
        }
    }

    // Inner class: draws the table header in small muted capital letters.
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setBackground(Theme.BACKGROUND);
            setForeground(Theme.MUTED);
            setFont(Theme.font(Font.BOLD, 11));
            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 16, 0, 16)));
            return this;
        }
    }
}
