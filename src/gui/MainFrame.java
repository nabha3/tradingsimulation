package gui;

import model.Market;
import model.MarketListener;
import model.Stock;
import model.Trader;
import model.User;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * The main window shown after login.
 *
 * Layout:
 *   - a header: logo, page buttons (with a sliding green underline), wallet and user name
 *   - the page area (a CardLayout shows one page at a time, with a fade when it changes)
 *   - a status bar with a live clock
 *
 * The Market page has four overview cards, a search box and a price table.
 * In the table you can click a column header to sort, rows light up when the
 * mouse is over them, a price that has just changed flashes green or red, and
 * every row has a small trend line.
 *
 * Other pages (trading, portfolio, chart, admin) can be added with addPage().
 *
 * MainFrame implements MarketListener, so the Market tells it whenever the
 * prices change (Observer pattern) and it simply redraws the table.
 */
public class MainFrame extends JFrame implements MarketListener {
    private static final int UPDATE_INTERVAL_MS = 2000;     // prices change every 2 seconds
    private static final String[] COLUMNS = {"SYMBOL", "PRICE", "CHANGE", "CHANGE %", "TREND"};
    private static final int TREND_COLUMN = 4;

    private User user;
    private Market market;

    // header and pages
    private NavBar navBar;
    private FadePanel pagePanel;
    private CardLayout cards;
    private HashMap<String, JButton> pageButtons;

    // wallet counter animation
    private JLabel walletLabel;
    private double shownWallet = 0;             // the number currently displayed
    private Timer walletTimer;

    // market page
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel gainerLabel;
    private JLabel loserLabel;
    private JLabel breadthLabel;
    private JLabel countLabel;
    private int sortColumn = 0;                 // which column the table is sorted by
    private boolean sortAscending = true;
    private int hoverRow = -1;                  // row under the mouse, -1 for none

    // the colour flash after a price change: +1 = just went up, -1 = just went down, fades to 0
    private HashMap<String, Double> flash;
    private Timer flashTimer;

    public MainFrame(User user, Market market) {
        this.user = user;
        this.market = market;
        this.pageButtons = new HashMap<>();
        this.flash = new HashMap<>();
        buildScreen();
        addPage("Market", buildMarketPage());
        refreshTable(false);            // show the starting prices straight away
        refreshWallet();                // counts up from 0 to the real balance
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
        pagePanel = new FadePanel();
        pagePanel.setLayout(cards);
        add(pagePanel, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);

        setSize(1000, 680);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
    }

    // Header: logo and page buttons on the left, wallet and user on the right.
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 14));
        left.setOpaque(false);
        left.add(Theme.createLogo(22));
        navBar = new NavBar();
        left.add(navBar);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 20));
        right.setOpaque(false);
        walletLabel = new JLabel(" ");
        walletLabel.setFont(Theme.font(Font.BOLD, 15));
        walletLabel.setForeground(Theme.GREEN);
        walletLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        JLabel userLabel = new JLabel(user.getName() + "  |  " + user.getRole());
        userLabel.setFont(Theme.font(Font.PLAIN, 13));
        userLabel.setForeground(Theme.MUTED);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        right.add(walletLabel);
        right.add(userLabel);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // Status bar: "Live" on the left, a clock on the right.
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

        final JLabel clock = new JLabel(" ");
        clock.setFont(Theme.font(Font.PLAIN, 12));
        clock.setForeground(Theme.MUTED);
        bar.add(clock, BorderLayout.EAST);

        final DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss");
        clock.setText(LocalTime.now().format(format));
        Timer clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clock.setText(LocalTime.now().format(format));
            }
        });
        clockTimer.start();
        return bar;
    }

    /**
     * Adds a page to the window and a button for it in the header.
     * The first page that is added is shown straight away.
     */
    public void addPage(String name, JPanel page) {
        pagePanel.add(page, name);

        final String pageName = name;
        JButton button = navBar.addButton(name);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(pageName);
            }
        });
        pageButtons.put(name, button);

        if (pageButtons.size() == 1) {
            showPage(name);
        }
    }

    // Shows one page (with a short fade) and moves the underline to its button.
    private void showPage(String name) {
        cards.show(pagePanel, name);
        navBar.setActive(pageButtons.get(name));
        pagePanel.fadeIn(300, 14);
    }

    // ---------------------------------------------------------------- wallet

    // Shows the trader's wallet in the header. The number counts up smoothly.
    // Other panels can call this after a trade.
    public void refreshWallet() {
        if (!(user instanceof Trader)) {
            walletLabel.setText("");        // admins have no wallet
            return;
        }
        final double target = user.getWalletBalance();
        if (walletTimer != null) {
            walletTimer.stop();
        }
        walletTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // move 12% of the remaining distance each tick, so it slows down near the end
                shownWallet += (target - shownWallet) * 0.12;
                if (Math.abs(target - shownWallet) < 0.01) {
                    shownWallet = target;
                    walletTimer.stop();
                }
                walletLabel.setText("Wallet  " + Theme.RUPEE + " " + String.format("%,.2f", shownWallet));
            }
        });
        walletTimer.start();
    }

    // ----------------------------------------------------------- market page

    // Builds the Market page: title and search, overview cards, price table.
    private JPanel buildMarketPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(Theme.BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));

        // Title on the left, search box on the right
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Market");
        title.setFont(Theme.font(Font.BOLD, 26));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Live prices of all listed stocks");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);
        titleRow.add(titles, BorderLayout.WEST);

        searchField = buildSearchField();
        JPanel searchHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        searchHolder.setOpaque(false);
        searchHolder.add(searchField);
        titleRow.add(searchHolder, BorderLayout.EAST);
        page.add(titleRow, BorderLayout.NORTH);

        // Cards on top, table below
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.add(buildOverviewCards(), BorderLayout.NORTH);
        body.add(buildTable(), BorderLayout.CENTER);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    // A text field that shows grey "Search symbol" text while it is empty.
    private JTextField buildSearchField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(Theme.MUTED);
                    g2.setFont(getFont());
                    g2.drawString("Search symbol", getInsets().left, (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2);
                    g2.dispose();
                }
            }
        };
        Theme.styleField(field);
        field.setPreferredSize(new Dimension(220, 40));
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refreshTable(false);        // filter the table as the user types
            }
        });
        return field;
    }

    // Four small cards: top gainer, top loser, how many are up/down, how many stocks.
    private JPanel buildOverviewCards() {
        gainerLabel = new JLabel("-");
        loserLabel = new JLabel("-");
        breadthLabel = new JLabel("-");
        countLabel = new JLabel("-");

        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.add(Theme.createStatCard("TOP GAINER", gainerLabel));
        row.add(Theme.createStatCard("TOP LOSER", loserLabel));
        row.add(Theme.createStatCard("UP / DOWN", breadthLabel));
        row.add(Theme.createStatCard("STOCKS LISTED", countLabel));
        return row;
    }


    // Builds the price table inside a scroll pane.
    private JScrollPane buildTable() {
        // The table should be read-only, so we override isCellEditable.
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Theme.SURFACE);
        table.setRowSelectionAllowed(false);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getColumnModel().getColumn(TREND_COLUMN).setCellRenderer(new SparkRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(100, 40));
        table.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Clicking a header sorts by that column; clicking it again reverses the order.
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                if (column < 0 || column == TREND_COLUMN) {
                    return;
                }
                if (column == sortColumn) {
                    sortAscending = !sortAscending;
                } else {
                    sortColumn = column;
                    sortAscending = (column == 0);      // symbols A-Z first, numbers biggest first
                }
                refreshTable(false);
                table.getTableHeader().repaint();
            }
        });

        // Remember which row the mouse is over, so it can be highlighted.
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverRow = -1;
                table.repaint();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.SURFACE);
        return scroll;
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
        refreshTable(true);
    }

    // --------------------------------------------------- filling the table

    // Change in price since the previous update.
    private double changeOf(Stock s) {
        ArrayList<Double> history = s.getPriceHistory();
        if (history.size() < 2) {
            return 0;
        }
        return s.getPrice() - history.get(history.size() - 2);
    }

    // Change as a percentage of the previous price.
    private double percentOf(Stock s) {
        ArrayList<Double> history = s.getPriceHistory();
        if (history.size() < 2) {
            return 0;
        }
        return changeOf(s) / history.get(history.size() - 2) * 100;
    }

    // Returns the Comparator used to sort the table by the given column.
    private Comparator<Stock> comparatorFor(int column) {
        if (column == 1) {
            return new Comparator<Stock>() {
                @Override
                public int compare(Stock a, Stock b) {
                    return Double.compare(a.getPrice(), b.getPrice());
                }
            };
        } else if (column == 2) {
            return new Comparator<Stock>() {
                @Override
                public int compare(Stock a, Stock b) {
                    return Double.compare(changeOf(a), changeOf(b));
                }
            };
        } else if (column == 3) {
            return new Comparator<Stock>() {
                @Override
                public int compare(Stock a, Stock b) {
                    return Double.compare(percentOf(a), percentOf(b));
                }
            };
        }
        return new Comparator<Stock>() {            // column 0: by symbol
            @Override
            public int compare(Stock a, Stock b) {
                return a.getSymbol().compareTo(b.getSymbol());
            }
        };
    }

    // Rebuilds the table from the market. animate = true makes changed prices flash.
    private void refreshTable(boolean animate) {
        // 1. Pick the stocks that match the search text
        String query = searchField.getText().trim().toUpperCase();
        ArrayList<Stock> shown = new ArrayList<>();
        for (Stock s : market.getStocks()) {
            if (query.isEmpty() || s.getSymbol().contains(query)) {
                shown.add(s);
            }
        }

        // 2. Sort them
        Collections.sort(shown, comparatorFor(sortColumn));
        if (!sortAscending) {
            Collections.reverse(shown);         // descending order: biggest (or Z) first
        }

        // 3. Fill the table rows
        tableModel.setRowCount(0);
        for (Stock s : shown) {
            double change = changeOf(s);
            double percent = percentOf(s);
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
                changeText, percentText, s
            });

            if (animate) {
                flash.put(s.getSymbol(), change > 0.005 ? 1.0 : (change < -0.005 ? -1.0 : 0.0));
            }
        }
        if (animate) {
            startFlashTimer();
        }
        refreshOverview();
    }

    // Fades the green/red flash away over about half a second.
    private void startFlashTimer() {
        if (flashTimer != null && flashTimer.isRunning()) {
            return;
        }
        flashTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean stillFlashing = false;
                for (String symbol : flash.keySet()) {
                    double value = flash.get(symbol) * 0.85;
                    if (Math.abs(value) < 0.03) {
                        value = 0;
                    } else {
                        stillFlashing = true;
                    }
                    flash.put(symbol, value);
                }
                table.repaint();
                if (!stillFlashing) {
                    flashTimer.stop();
                }
            }
        });
        flashTimer.start();
    }

    // Updates the four cards above the table.
    private void refreshOverview() {
        Stock best = null;
        Stock worst = null;
        int up = 0;
        int down = 0;
        for (Stock s : market.getStocks()) {
            double p = percentOf(s);
            if (best == null || p > percentOf(best)) {
                best = s;
            }
            if (worst == null || p < percentOf(worst)) {
                worst = s;
            }
            if (p > 0) {
                up++;
            } else if (p < 0) {
                down++;
            }
        }

        if (best != null && percentOf(best) > 0) {
            gainerLabel.setText(best.getSymbol() + "  " + String.format("+%.2f%%", percentOf(best)));
            gainerLabel.setForeground(Theme.GREEN);
        } else {
            gainerLabel.setText("-");
            gainerLabel.setForeground(Theme.MUTED);
        }
        if (worst != null && percentOf(worst) < 0) {
            loserLabel.setText(worst.getSymbol() + "  " + String.format("%.2f%%", percentOf(worst)));
            loserLabel.setForeground(Theme.RED);
        } else {
            loserLabel.setText("-");
            loserLabel.setForeground(Theme.MUTED);
        }
        breadthLabel.setText("<html><span style='color:#2EBD85'>" + up + " " + Theme.ARROW_UP
                + "</span> &nbsp;&nbsp; <span style='color:#F6465D'>" + down + " " + Theme.ARROW_DOWN + "</span></html>");
        countLabel.setText(String.valueOf(market.getStocks().size()));
    }

    // Background colour of a table row: stripes, hover highlight and the price flash.
    private Color rowBackground(JTable table, int row) {
        Color base = (row % 2 == 0) ? Theme.SURFACE : Theme.ROW_ALT;
        if (row == hoverRow) {
            base = Theme.blend(base, Theme.TEXT, 0.07);
        }
        Double f = flash.get(String.valueOf(table.getValueAt(row, 0)));
        if (f != null && Math.abs(f) > 0.01) {
            base = Theme.blend(base, f > 0 ? Theme.GREEN : Theme.RED, 0.24 * Math.abs(f));
        }
        return base;
    }

    // ------------------------------------------------------ inner classes

    /**
     * The row of page buttons in the header. A green underline slides from the
     * old button to the new one: every 15 ms it moves 25% of the remaining
     * distance, so it starts quickly and eases to a stop.
     */
    private static class NavBar extends JPanel {
        private JButton active;
        private ArrayList<JButton> buttons = new ArrayList<>();
        private double lineX = 0;
        private double lineWidth = 0;
        private boolean placed = false;
        private Timer timer;

        NavBar() {
            super(new FlowLayout(FlowLayout.LEFT, 6, 0));
            setOpaque(false);
            timer = new Timer(15, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    slideStep();
                }
            });
        }

        JButton addButton(String text) {
            JButton b = new JButton(text);
            b.setFont(Theme.font(Font.BOLD, 14));
            b.setForeground(Theme.MUTED);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createEmptyBorder(22, 12, 22, 12));
            buttons.add(b);
            add(b);
            revalidate();
            return b;
        }

        void setActive(JButton button) {
            active = button;
            for (JButton b : buttons) {
                b.setForeground(b == active ? Theme.TEXT : Theme.MUTED);
            }
            timer.start();
        }

        private void slideStep() {
            if (active == null || active.getWidth() <= 0) {
                return;                         // layout has not happened yet, try again next tick
            }
            double targetX = active.getX() + 8;
            double targetWidth = active.getWidth() - 16;
            if (!placed) {
                lineX = targetX;                // first time: no sliding, just appear
                lineWidth = targetWidth;
                placed = true;
            } else {
                lineX += (targetX - lineX) * 0.25;
                lineWidth += (targetWidth - lineWidth) * 0.25;
                if (Math.abs(targetX - lineX) < 0.5 && Math.abs(targetWidth - lineWidth) < 0.5) {
                    lineX = targetX;
                    lineWidth = targetWidth;
                    timer.stop();
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);                     // draw the buttons first
            if (placed) {
                g.setColor(Theme.GREEN);
                g.fillRect((int) lineX, getHeight() - 3, (int) lineWidth, 3);
            }
        }
    }

    // Draws the normal table cells: bold symbol, right-aligned numbers, green/red changes.
    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            String text = String.valueOf(value);

            setBackground(rowBackground(table, row));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

            if (column == 0) {
                setHorizontalAlignment(SwingConstants.LEFT);
                setFont(Theme.font(Font.BOLD, 14));
                setForeground(Theme.TEXT);
            } else {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setFont(Theme.font(Font.PLAIN, 14));
                setForeground(Theme.TEXT);
                if (column == 2 || column == 3) {
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

    // Draws the TREND column: a tiny line chart of the stock's recent prices.
    private class SparkRenderer extends JComponent implements TableCellRenderer {
        private Stock stock;
        private Color background;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            stock = (Stock) value;
            background = rowBackground(table, row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(background);
            g2.fillRect(0, 0, getWidth(), getHeight());

            ArrayList<Double> history = stock.getPriceHistory();
            int count = Math.min(history.size(), 30);       // show the last 30 prices at most
            if (count >= 2) {
                int first = history.size() - count;
                double min = history.get(first);
                double max = history.get(first);
                for (int i = first; i < history.size(); i++) {
                    min = Math.min(min, history.get(i));
                    max = Math.max(max, history.get(i));
                }
                double range = Math.max(max - min, 0.0001);

                int left = 16;
                int width = getWidth() - 32;
                int top = 10;
                int height = getHeight() - 20;

                Path2D.Double line = new Path2D.Double();
                double lastX = left;
                double lastY = top;
                for (int i = 0; i < count; i++) {
                    double x = left + width * i / (double) (count - 1);
                    double y = top + (max - history.get(first + i)) / range * height;
                    if (i == 0) {
                        line.moveTo(x, y);
                    } else {
                        line.lineTo(x, y);
                    }
                    lastX = x;
                    lastY = y;
                }

                boolean rising = history.get(history.size() - 1) >= history.get(first);
                Color color = rising ? Theme.GREEN : Theme.RED;
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(line);
                g2.fillOval((int) lastX - 3, (int) lastY - 3, 6, 6);     // dot at the latest price
            }
            g2.dispose();
        }
    }

    // Draws the table header: small muted capitals, with an arrow on the sorted column.
    private class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String text = String.valueOf(value);
            if (column == sortColumn) {
                text = text + " " + (sortAscending ? Theme.ARROW_UP : Theme.ARROW_DOWN);
            }
            super.getTableCellRendererComponent(table, text, false, false, row, column);
            setBackground(Theme.BACKGROUND);
            setForeground(column == sortColumn ? Theme.TEXT : Theme.MUTED);
            setFont(Theme.font(Font.BOLD, 11));
            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.RIGHT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 16, 0, 16)));
            return this;
        }
    }
}
