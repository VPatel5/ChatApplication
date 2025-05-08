package me.vpatel.client.ui;

import me.vpatel.client.ConvoClient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class MainUI extends JFrame {
    private final ConvoClient client;

    private JTextField searchField;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;
    private JButton addFriendButton;

    private JList<String> friendsList;
    private DefaultListModel<String> friendsModel;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private JButton newGroupButton;

    public MainUI(ConvoClient client) {
        super("Convo Chat");
        this.client = client;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Left panel: Search & Friends
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // Search section
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        addFriendButton = new JButton("Add Friend");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        addFriendButton.addActionListener(e -> onAddFriend());

        searchPanel.add(new JLabel("Search Users:"), BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(new JScrollPane(suggestionList), BorderLayout.SOUTH);
        searchPanel.add(addFriendButton, BorderLayout.PAGE_END);

        // Friends section
        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newGroupButton = new JButton("New Group");
        newGroupButton.addActionListener(e -> onCreateGroup());

        friendsPanel.add(new JLabel("Friends:"), BorderLayout.NORTH);
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        friendsPanel.add(newGroupButton, BorderLayout.SOUTH);

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(friendsPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);

        // Right panel: Chat area
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> onSendMessage());

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void onSearchChanged() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            suggestionModel.clear();
            return;
        }
        // send search request
        //client.send(new ClientSearchUsersPacket(query));
    }

    private void onAddFriend() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            //client.send(new ClientAddFriendPacket(selected));
        }
    }

    private void onSendMessage() {
        String to = friendsList.getSelectedValue();
        String msg = messageField.getText().trim();
        if (to != null && !msg.isEmpty()) {
            //client.send(new ClientMessagePacket(to, msg));
            messageField.setText("");
        }
    }

    private void onCreateGroup() {
        // dialog to select multiple friends and name group
        JList<String> list = new JList<>(friendsModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JTextField groupName = new JTextField();
        int res = JOptionPane.showConfirmDialog(this, new Object[]{
                new JLabel("Group Name:"), groupName,
                new JLabel("Select Friends:"), new JScrollPane(list)
        }, "Create Group", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            List<String> members = list.getSelectedValuesList();
            String name = groupName.getText().trim();
            if (!name.isEmpty() && !members.isEmpty()) {
                //client.send(new ClientCreateGroupPacket(name, members));
            }
        }
    }

}
