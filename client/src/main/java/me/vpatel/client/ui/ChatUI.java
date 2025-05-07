package me.vpatel.client.ui;

import me.vpatel.client.AppContext;

import javax.swing.*;
import java.awt.*;

public class ChatUI extends JFrame {
    private JTextArea chatArea = new JTextArea();
    private JTextField inputField = new JTextField();

    public ChatUI() {
        super("Chat");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String msg = inputField.getText().trim();
            if (!msg.isEmpty()) {
                //AppContext.getClient().sendChatMessage(msg);
                chatArea.append("Me: " + msg + "\\n");
                inputField.setText("");
            }
        });
    }

    public void receiveMessage(String msg) {
        chatArea.append(msg + "\\n");
    }
}
