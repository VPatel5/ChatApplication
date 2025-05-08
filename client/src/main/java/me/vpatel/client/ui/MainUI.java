package me.vpatel.client.ui;

import me.vpatel.client.AppContext;
import me.vpatel.client.ConvoClient;
import me.vpatel.client.api.ClientApi;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.client.ClientListRequestPacket.ListType;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class MainUI extends JFrame {
    private final ClientApi clientApi;

    private JTextField searchField;
    private JList<ConvoUser> suggestionList;
    private DefaultListModel<ConvoUser> suggestionModel;
    private JButton addFriendButton;

    private JList<ConvoUser> friendsList;
    private DefaultListModel<ConvoUser> friendsModel;

    private JList<Invite> incomingInvitesList;
    private DefaultListModel<Invite> invitesModel;
    private JButton acceptInviteButton, declineInviteButton;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private JList<ConvoUser> groupMembersList;
    private DefaultListModel<ConvoUser> groupMembersModel;
    private JTextField groupNameField;
    private JButton createGroupButton;

    public MainUI(ConvoClient client) {
        super("Convo Chat");
        this.clientApi = client.getClientApi();
        initComponents();
        registerListeners();
        refreshFriends();
        refreshInvites();
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Left panel for user management
        JPanel leftPanel = new JPanel(new GridLayout(4,1));

        // Search & Add Friend
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        addFriendButton = new JButton("Add Friend");
        addFriendButton.setEnabled(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Users"));
        searchPanel.add(searchField, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(suggestionList), BorderLayout.CENTER);
        searchPanel.add(addFriendButton, BorderLayout.SOUTH);
        leftPanel.add(searchPanel);

        // Friends list
        JPanel friendsPanel = new JPanel(new BorderLayout());
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsPanel.setBorder(BorderFactory.createTitledBorder("Friends"));
        friendsPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        leftPanel.add(friendsPanel);

        // Incoming Invites
        JPanel invitesPanel = new JPanel(new BorderLayout());
        invitesModel = new DefaultListModel<>();
        incomingInvitesList = new JList<>(invitesModel);
        acceptInviteButton = new JButton("Accept");
        declineInviteButton = new JButton("Decline");
        acceptInviteButton.setEnabled(false);
        declineInviteButton.setEnabled(false);
        JPanel inviteButtons = new JPanel(new FlowLayout());
        inviteButtons.add(acceptInviteButton);
        inviteButtons.add(declineInviteButton);
        invitesPanel.setBorder(BorderFactory.createTitledBorder("Friend Invites"));
        invitesPanel.add(new JScrollPane(incomingInvitesList), BorderLayout.CENTER);
        invitesPanel.add(inviteButtons, BorderLayout.SOUTH);
        leftPanel.add(invitesPanel);

        // Create Group
        JPanel groupPanel = new JPanel(new BorderLayout());
        groupPanel.setBorder(BorderFactory.createTitledBorder("Create Group"));
        groupNameField = new JTextField();
        groupMembersModel = new DefaultListModel<>();
        groupMembersList = new JList<>(groupMembersModel);
        groupMembersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        createGroupButton = new JButton("Create");
        groupPanel.add(new JLabel("Group Name:"), BorderLayout.NORTH);
        groupPanel.add(groupNameField, BorderLayout.CENTER);
        groupPanel.add(new JScrollPane(groupMembersList), BorderLayout.EAST);
        groupPanel.add(createGroupButton, BorderLayout.SOUTH);
        leftPanel.add(groupPanel);

        add(leftPanel, BorderLayout.WEST);

        // Right panel for chat
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea(); chatArea.setEditable(false);
        messageField = new JTextField(); sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.add(messageField, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(sendPanel, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void registerListeners() {
        // Search auto-complete
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchUsers(); }
            public void removeUpdate(DocumentEvent e) { searchUsers(); }
            public void changedUpdate(DocumentEvent e) { searchUsers(); }
        });
        suggestionList.addListSelectionListener(e -> addFriendButton.setEnabled(!suggestionList.isSelectionEmpty()));
        addFriendButton.addActionListener(e -> {
            ConvoUser user = suggestionList.getSelectedValue();
            clientApi.sendFriendInvite(user.getName());
            refreshInvites();
        });

        friendsList.addListSelectionListener(e -> {
            boolean sel = !friendsList.isSelectionEmpty();
            sendButton.setEnabled(sel);
            if (sel) loadChat();
        });
        sendButton.addActionListener(e -> {
            ConvoUser to = friendsList.getSelectedValue();
            clientApi.chat(to.getName(), messageField.getText());
            messageField.setText("");
        });

        incomingInvitesList.addListSelectionListener(e -> {
            boolean sel = !incomingInvitesList.isSelectionEmpty();
            acceptInviteButton.setEnabled(sel);
            declineInviteButton.setEnabled(sel);
        });
        acceptInviteButton.addActionListener(e -> {
            Invite invite = incomingInvitesList.getSelectedValue();
            clientApi.acceptFriendInvite(invite.getInviter().getName());
            refreshFriends(); refreshInvites();
        });
        declineInviteButton.addActionListener(e -> {
            Invite invite = incomingInvitesList.getSelectedValue();
            clientApi.declineFriendInvite(invite.getInviter().getName());
            refreshInvites();
        });

        createGroupButton.addActionListener(e -> {
            String name = groupNameField.getText().trim();
            List<ConvoUser> selected = groupMembersList.getSelectedValuesList();
            clientApi.createGroup(name);
            refreshFriends();
        });
    }

    private void searchUsers() {
        String q = searchField.getText().trim();
        suggestionModel.clear();
        if (!q.isEmpty()) {
            clientApi.list(ListType.CONVO_USERS);
            List<ConvoUser> users = clientApi.getUsers();
            for (ConvoUser u : users) suggestionModel.addElement(u);
        }
    }

    private void refreshFriends() {
        clientApi.list(ListType.FRIENDS);
        friendsModel.clear(); groupMembersModel.clear();
        for (ConvoUser u : clientApi.getFriends()) {
            friendsModel.addElement(u);
            groupMembersModel.addElement(u);
        }
    }

    private void refreshInvites() {
        clientApi.list(ListType.INCOMING_FRIEND_INVITES);
        invitesModel.clear();
        for (Invite i : clientApi.getIncomingFriendInvites()) invitesModel.addElement(i);
    }

    private void loadChat() {
        chatArea.setText("");
        ConvoUser to = friendsList.getSelectedValue();
        clientApi.list(ListType.MESSAGES, to.getName());
        List<Message> msgs = clientApi.getMessages().get(to.getName());
        if (msgs != null) {
            for (Message m : msgs) chatArea.append(m.getSender().getName() + ": " + m.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI(AppContext.getClient()));
    }
}
