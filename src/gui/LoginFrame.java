package gui;

import exceptions.UserNotFoundException;
import exceptions.WrongPasswordException;
import model.Market;
import model.User;
import model.UserRegistry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The first screen of BullRun. The user types an id and a password and
 * presses Login. On success the main window opens; on failure an error
 * message is shown under the button.
 */
public class LoginFrame extends JFrame {
    private UserRegistry registry;
    private Market market;

    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginFrame(UserRegistry registry, Market market) {
        this.registry = registry;
        this.market = market;
        buildScreen();
    }

    // Creates all the components and adds them to the window.
    private void buildScreen() {
        setTitle("BullRun");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // The window background is dark; the login card sits in the middle of it.
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(Theme.BACKGROUND);
        setContentPane(background);

        Theme.CardPanel card = new Theme.CardPanel();
        card.setLayout(new BorderLayout(0, 24));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 32, 36));
        card.setPreferredSize(new Dimension(380, 470));

        // Top: logo and a short tagline
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.setOpaque(false);
        top.add(Theme.createLogo(34));
        JLabel tagline = new JLabel("Virtual stock trading, zero risk", SwingConstants.CENTER);
        tagline.setFont(Theme.font(Font.PLAIN, 13));
        tagline.setForeground(Theme.MUTED);
        top.add(tagline);
        card.add(top, BorderLayout.NORTH);

        // Middle: captions and fields, one under the other
        JPanel form = new JPanel(new GridLayout(4, 1, 0, 6));
        form.setOpaque(false);
        idField = new JTextField();
        passwordField = new JPasswordField();
        Theme.styleField(idField);
        Theme.styleField(passwordField);
        form.add(Theme.createCaption("USER ID"));
        form.add(idField);
        form.add(Theme.createCaption("PASSWORD"));
        form.add(passwordField);
        card.add(form, BorderLayout.CENTER);

        // Bottom: login button and the message line
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JButton loginButton = Theme.createPrimaryButton("Log in");
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(Theme.font(Font.PLAIN, 13));
        messageLabel.setForeground(Theme.RED);
        bottom.add(loginButton, BorderLayout.CENTER);
        bottom.add(messageLabel, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        background.add(card);

        // Event handling: both the button and the Enter key run the same code.
        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        };
        loginButton.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        setSize(520, 600);
        setLocationRelativeTo(null);    // centre on screen
    }

    // Reads the fields, asks the registry to check them, and reacts to the result.
    private void attemptLogin() {
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (id.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both ID and password.");
            return;
        }

        try {
            User user = registry.login(id, password);
            new MainFrame(user, market).setVisible(true);
            dispose();                                  // close the login window
        } catch (UserNotFoundException e) {
            messageLabel.setText(e.getMessage());
        } catch (WrongPasswordException e) {
            messageLabel.setText(e.getMessage());
            passwordField.setText("");
        }
    }
}
