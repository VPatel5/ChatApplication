package me.vpatel.client.ui;

import me.vpatel.client.AppContext;

import javax.swing.*;
import java.awt.*;

public class RegisterUI extends JFrame {
    private JTextField usernameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JTextField emailField = new JTextField(15);
    public JLabel statusLabel = new JLabel("");

    public RegisterUI() {
        super("Register");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 250);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addComponent(new JLabel("Username:"), gbc, 0, 0);
        addComponent(usernameField, gbc, 1, 0);

        addComponent(new JLabel("Password:"), gbc, 0, 1);
        addComponent(passwordField, gbc, 1, 1);

        addComponent(new JLabel("Email:"), gbc, 0, 2);
        addComponent(emailField, gbc, 1, 2);

        JButton registerButton = new JButton("Register");
        addComponent(registerButton, gbc, 1, 3);
        addComponent(statusLabel, gbc, 1, 4);

        registerButton.addActionListener(e -> handleRegister());
    }

    private void addComponent(Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        add(comp, gbc);
    }

    private void handleRegister() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        statusLabel.setText("Registering...");
        AppContext.getClient().getAuthHandler().registerUser(user, pass, email);
    }
}
