// ─── In-memory stores ───────────────────────────────────────────────────────
let users = [];
let friends = [];
let groups = [];
let incomingFriendInvites = [];
let directMessages = [];
let groupMessages = {};
let currentConversation = null;
let currentType = null;

// ─── DOM refs ───────────────────────────────────────────────────────────────
const elements = {
    tabs: {
        friends: document.getElementById('friends-tab'),
        groups: document.getElementById('groups-tab')
    },
    lists: {
        friends: document.getElementById('friends-list'),
        groups: document.getElementById('groups-list')
    },
    search: document.getElementById('search-input'),
    chat: {
        header: document.getElementById('chat-header'),
        messages: document.getElementById('chat-messages'),
        input: document.getElementById('chat-input'),
        sendBtn: document.getElementById('chat-send-btn')
    },
    friendsPanel: {
        container: document.getElementById('friends-panel'),
        input: document.getElementById('friend-request-input'),
        sendBtn: document.getElementById('send-request-btn'),
        viewBtn: document.getElementById('view-requests-btn'),
        requests: document.getElementById('friend-requests')
    },
    groupActions: {
        container: document.getElementById('group-actions'),
        input: document.getElementById('new-group-name'),
        viewInvitesBtn: document.getElementById('view-group-invites-btn'),
        invitesContainer: document.getElementById('group-invites'),
        createBtn: document.getElementById('create-group-btn'),
        inviteBtn: document.getElementById('invite-friend-btn')
    },
    invite: {
        inviteModal: document.getElementById('invite-modal'),
        friendSelect: document.getElementById('friend-select'),
        confirmInviteBtn: document.getElementById('confirm-invite-btn'),
        closeModal: document.querySelector('.close-modal')
    }
};

// ─── Initialize Feedback System ────────────────────────────────────────────
const feedbackContainer = document.createElement('div');
feedbackContainer.id = 'feedback-container';
document.body.appendChild(feedbackContainer);

// ─── UI Functions ──────────────────────────────────────────────────────────
function showFeedback(msg) {
    const toast = document.createElement('div');
    toast.className = 'feedback-toast';
    toast.innerHTML = `
        <div class="feedback-content">
            <span class="feedback-icon">!</span>
            <span class="feedback-message">${msg}</span>
        </div>
    `;
    feedbackContainer.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function addMessage(line) {
    const div = document.createElement('div');
    div.textContent = line;
    elements.chat.messages.appendChild(div);
    elements.chat.messages.scrollTop = elements.chat.messages.scrollHeight;
}

function clearAndRenderChat(lines) {
    elements.chat.messages.innerHTML = '';
    if (Array.isArray(lines)) lines.forEach(addMessage);
}

// ─── Data Population Functions ─────────────────────────────────────────────
window.populateFriends = list => {
    friends = list;
    elements.lists.friends.innerHTML = '';
    friends.forEach(name => {
        const li = document.createElement('li');
        const span = document.createElement('span');
        span.textContent = name;
        span.onclick = () => selectConversation(name, 'friend');
        const rem = document.createElement('button');
        rem.textContent = 'Remove';
        rem.className = 'remove-friend-btn';
        rem.onclick = e => {
            e.stopPropagation();
            handleRemoveFriend(name);
        };
        li.append(span, rem);
        elements.lists.friends.appendChild(li);
    });
};

window.populateGroups = list => {
    groups = list;
    elements.lists.groups.innerHTML = '';
    groups.forEach(name => {
        const li = document.createElement('li');
        li.textContent = name;
        li.onclick = () => selectConversation(name, 'group');
        elements.lists.groups.appendChild(li);
    });
};

window.populateGroupInvites = invites => {
    elements.groupActions.invitesContainer.innerHTML = '';
    Object.entries(invites).forEach(([group, groupInvites]) => {
        groupInvites.forEach(invite => {
            const row = document.createElement('div');
            row.textContent = `${invite.inviter} invited you to ${group}`;

            const acceptBtn = document.createElement('button');
            acceptBtn.textContent = 'Accept';
            acceptBtn.onclick = () => handleGroupInviteResponse(group, true);

            const declineBtn = document.createElement('button');
            declineBtn.textContent = 'Decline';
            declineBtn.onclick = () => handleGroupInviteResponse(group, false);

            row.append(acceptBtn, declineBtn);
            elements.groupActions.invitesContainer.appendChild(row);
        });
    });
};

window.populateFriendRequests = list => {
    incomingFriendInvites = list;
    elements.friendsPanel.requests.innerHTML = '';
    incomingFriendInvites.forEach(inviter => {
        const row = document.createElement('div');
        row.textContent = inviter + ' ';
        const acc = document.createElement('button');
        acc.textContent = 'Accept';
        acc.onclick = () => handleFriendRequestResponse(inviter, true);
        const dec = document.createElement('button');
        dec.textContent = 'Decline';
        dec.onclick = () => handleFriendRequestResponse(inviter, false);
        row.append(acc, dec);
        elements.friendsPanel.requests.appendChild(row);
    });
};

window.populateDirectMessages = lines => {
    directMessages = lines;
    clearAndRenderChat(lines);
};

window.populateGroupMessages = ({ group, messages }) => {
    groupMessages[group] = messages;
    if (currentConversation === group && currentType === 'group') {
        clearAndRenderChat(messages);
    }
};


// ─── Conversation Management ───────────────────────────────────────────────
function selectConversation(name, type) {
    currentConversation = name;
    currentType = type;

    if (type === 'group') {
        elements.chat.header.textContent = "Group: " + name;
        elements.groupActions.inviteBtn.classList.remove('hidden');
    } else {
        elements.chat.header.textContent = name;
        elements.groupActions.inviteBtn.classList.add('hidden');
    }

    window.alert(JSON.stringify({
        action: type === 'friend' ? 'selectFriend' : 'selectGroup',
        target: name
    }));
}

// ─── Action Handlers ───────────────────────────────────────────────────────
function handleRemoveFriend(name) {
    window.alert(JSON.stringify({
        action: 'removeFriend',
        target: name
    }));
}

function handleFriendRequestResponse(inviter, accept) {
    window.alert(JSON.stringify({
        action: 'respondFriendRequest',
        target: inviter,
        accept: accept
    }));
}

function handleGroupInviteResponse(group, accept) {
    window.alert(JSON.stringify({
        action: 'respondGroupInvite',
        group: group,
        accept: accept
    }));
}

// ─── Tab Switching ─────────────────────────────────────────────────────────
elements.tabs.friends.addEventListener('click', () => {
    elements.tabs.friends.classList.add('active');
    elements.tabs.groups.classList.remove('active');
    elements.lists.friends.classList.remove('hidden');
    elements.lists.groups.classList.add('hidden');
    elements.groupActions.container.classList.add('hidden');
    elements.friendsPanel.container.classList.remove('hidden');
    elements.search.placeholder = 'Search friends...';

    window.alert(JSON.stringify({
        action: 'switchToFriendsTab'
    }));
    // Reset conversation
    currentConversation = null;
    currentType = null;
    elements.chat.header.textContent = 'Select a conversation';
    elements.chat.messages.innerHTML = '';
    clearAndRenderChat([]);
    elements.groupActions.inviteBtn.classList.add('hidden');

});

elements.tabs.groups.addEventListener('click', () => {
    elements.tabs.groups.classList.add('active');
    elements.tabs.friends.classList.remove('active');
    elements.lists.groups.classList.remove('hidden');
    elements.lists.friends.classList.add('hidden');
    elements.groupActions.container.classList.remove('hidden');
    elements.friendsPanel.container.classList.add('hidden');
    elements.search.placeholder = 'Search groups...';

    window.alert(JSON.stringify({
        action: 'switchToGroupsTab'
    }));

    currentConversation = null;
    currentType = null;
    elements.chat.header.textContent = 'Select a conversation';
    elements.chat.messages.innerHTML = '';
    clearAndRenderChat([]);
    elements.groupActions.inviteBtn.classList.add('hidden');

});

// ─── Event Listeners ───────────────────────────────────────────────────────

elements.groupActions.inviteBtn.addEventListener('click', () => {
    elements.invite.friendSelect.innerHTML = '';
    friends.forEach(friend => {
        const option = document.createElement('option');
        option.value = friend;
        option.textContent = friend;
        elements.invite.friendSelect.appendChild(option);
    });
    elements.invite.inviteModal.classList.remove('hidden');
});

elements.invite.confirmInviteBtn.addEventListener('click', () => {
    const friend = elements.invite.friendSelect.value;
    if (friend && currentConversation) {
        window.alert(JSON.stringify({
            action: 'inviteToGroup',
            friend: friend
        }));
        elements.invite.inviteModal.classList.add('hidden');
    }
});

elements.invite.closeModal.addEventListener('click', () => {
    elements.invite.inviteModal.classList.add('hidden');
});

elements.search.addEventListener('input', () => {
    const query = elements.search.value.toLowerCase();
    const listEl = elements.tabs.friends.classList.contains('active')
        ? elements.lists.friends
        : elements.lists.groups;
    Array.from(listEl.children).forEach(li => {
        li.style.display = li.textContent.toLowerCase().includes(query) ? '' : 'none';
    });
});

elements.groupActions.createBtn.addEventListener('click', () => {
    const name = elements.groupActions.input.value.trim();
    if (!name) return;
    window.alert(JSON.stringify({
        action: 'createGroup',
        groupName: name
    }));
    elements.groupActions.input.value = '';
});

elements.groupActions.viewInvitesBtn.addEventListener('click', () => {
    elements.groupActions.invitesContainer.classList.toggle('hidden');
    window.alert(JSON.stringify({
        action: 'getGroupInvites'
    }));
});

elements.chat.sendBtn.addEventListener('click', sendMessage);
elements.chat.input.addEventListener('keypress', e => {
    if (e.key === 'Enter') sendMessage();
});

elements.friendsPanel.sendBtn.addEventListener('click', () => {
    const username = elements.friendsPanel.input.value.trim();
    if (!username) return;
    window.alert(JSON.stringify({
        action: 'sendFriendRequest',
        target: username
    }));
});

elements.friendsPanel.viewBtn.addEventListener('click', () => {
    window.alert(JSON.stringify({
        action: 'getFriendRequests'
    }));
});

// ─── Helper Functions ──────────────────────────────────────────────────────
function sendMessage() {
    if (!currentConversation) return;
    const msg = elements.chat.input.value.trim();
    if (!msg) return;

    const action = currentType === 'friend' ? 'sendFriendMessage' : 'sendGroupMessage';
    window.alert(JSON.stringify({
        action,
        target: currentConversation,
        message: msg
    }));
    elements.chat.input.value = '';
}

// Initialize
elements.tabs.friends.click();

window.showFeedback = showFeedback;
window.populateFriends = populateFriends;
window.populateGroups = populateGroups;
window.populateGroupInvites = populateGroupInvites;
window.populateFriendRequests = populateFriendRequests;
window.populateDirectMessages = populateDirectMessages;
window.populateGroupMessages = populateGroupMessages;