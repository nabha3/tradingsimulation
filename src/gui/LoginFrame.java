package gui;

import exceptions.UserNotFoundException;
import exceptions.WrongPasswordException;
import model.Market;
import model.User;
import model.UserRegistry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
 * message is shown under the form.
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
        setTitle("BullRun - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top: the title
        JLabel title = new JLabel("BullRun", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        add(title, BorderLayout.NORTH);

        // Middle: labels and text fields in a 2-column grid
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        idField = new JTextField();
        passwordField = new JPasswordField();
        form.add(new JLabel("User ID:"));
        form.add(idField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        // Bottom: login button and the message label
        JPanel bottom = new JPanel(new GridLayout(2, 1, 5, 5));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 30, 15, 30));
        JButton loginButton = new JButton("Login");
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        bottom.add(loginButton);
        bottom.add(messageLabel);
        add(bottom, BorderLayout.SOUTH);

        // Event handling: both the button and the Enter key run the same code.
        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        };
        loginButton.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        setSize(360, 270);
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
