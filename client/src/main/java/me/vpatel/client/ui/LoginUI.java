package me.vpatel.ui;

import me.vpatel.client.AppContext;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class LoginUI extends JFrame {
    private JTextField usernameField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JLabel statusLabel = new JLabel("");

    public LoginUI() {
        super("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 200);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        addComponent(new JLabel("Username:"), gbc, 0, 0);
        addComponent(usernameField, gbc, 1, 0);
        addComponent(new JLabel("Password:"), gbc, 0, 1);
        addComponent(passwordField, gbc, 1, 1);

        JButton loginButton = new JButton("Login");
        addComponent(loginButton, gbc, 1, 2);
        addComponent(statusLabel, gbc, 1, 3);

        loginButton.addActionListener(e -> handleLogin());
    }

    private void addComponent(Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        add(comp, gbc);
    }

    private void handleLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Fill in all fields.");
            return;
        }

        statusLabel.setText("Connecting...");
        new Thread(() -> {
            try {
                AppContext.getClient().setUser(new ConvoUser(UUID.nameUUIDFromBytes(user.getBytes()), user));
                AppContext.getClient().connect("localhost", 8080);
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Failed: " + ex.getMessage()));
            }
        }).start();
    }
}
