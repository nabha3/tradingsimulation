package gui;

import exceptions.UserNotFoundException;
import exceptions.WrongPasswordException;
import model.Market;
import model.User;
import model.UserRegistry;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
 * The first window of BullRun.
 *
 * It shows two things one after the other on top of a moving chart background:
 *   1. the welcome slides (OnboardingPanel)
 *   2. the login card
 * When the slides finish they fade out and the login card fades in.
 * After a correct login the main window opens.
 */
public class LoginFrame extends JFrame {
    private UserRegistry registry;
    private Market market;

    private JPanel stage;                    // holds the slides and the login card (CardLayout)
    private CardLayout stageLayout;
    private OnboardingPanel onboarding;
    private Theme.CardPanel card;

    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginFrame(UserRegistry registry, Market market) {
        this.registry = registry;
        this.market = market;
        buildScreen();
    }

    // Creates the background, the slides and the login card.
    private void buildScreen() {
        setTitle("BullRun");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AnimatedBackground background = new AnimatedBackground();
        background.setLayout(new BorderLayout());
        setContentPane(background);

        stageLayout = new CardLayout();
        stage = new JPanel(stageLayout);
        stage.setOpaque(false);
        background.add(stage, BorderLayout.CENTER);

        // First thing the user sees: the welcome slides
        onboarding = new OnboardingPanel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLogin();
            }
        });
        stage.add(onboarding, "slides");

        // Second thing: the login card, centred on the screen
        JPanel loginPage = new JPanel(new GridBagLayout());
        loginPage.setOpaque(false);
        buildCard();
        loginPage.add(card);
        stage.add(loginPage, "login");

        stageLayout.show(stage, "slides");
        onboarding.fadeIn(700, 24);

        setSize(900, 640);
        setLocationRelativeTo(null);    // centre on screen
    }

    // Fades the slides out, then shows the login card and fades it in.
    private void showLogin() {
        onboarding.fadeOut(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stageLayout.show(stage, "login");
                card.fadeIn(550, 30);
                idField.requestFocusInWindow();
            }
        });
    }

    // Builds the login card: logo, two fields, button and message.
    private void buildCard() {
        card = new Theme.CardPanel();
        card.setLayout(new BorderLayout(0, 24));
        card.setBorder(BorderFactory.createEmptyBorder(36, 36, 32, 36));
        card.setPreferredSize(new Dimension(380, 470));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.setOpaque(false);
        top.add(Theme.createLogo(34));
        JLabel tagline = new JLabel("Log in to your account", SwingConstants.CENTER);
        tagline.setFont(Theme.font(Font.PLAIN, 13));
        tagline.setForeground(Theme.MUTED);
        top.add(tagline);
        card.add(top, BorderLayout.NORTH);

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

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        JButton loginButton = Theme.createPrimaryButton("Log in");
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(Theme.font(Font.PLAIN, 13));
        messageLabel.setForeground(Theme.RED);
        bottom.add(loginButton, BorderLayout.CENTER);
        bottom.add(messageLabel, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);

        // Event handling: both the button and the Enter key run the same code.
        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptLogin();
            }
        };
        loginButton.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
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
