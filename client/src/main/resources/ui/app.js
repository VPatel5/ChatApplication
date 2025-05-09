//
// app.js
//

// ─── In-memory stores ───────────────────────────────────────────────────────
let users                    = [];
let friends                  = [];
let groups                   = [];
let incomingFriendInvites    = [];
let outgoingFriendInvites    = [];
let incomingGroupInvites     = {};
let outgoingGroupInvites     = {};
let directMessages           = [];
let groupMessages            = {};

// ─── DOM refs ───────────────────────────────────────────────────────────────
const friendsTab        = document.getElementById('friends-tab');
const groupsTab         = document.getElementById('groups-tab');
const searchInput       = document.getElementById('search-input');
const friendsList       = document.getElementById('friends-list');
const groupsList        = document.getElementById('groups-list');
const addGroupBtn       = document.getElementById('add-group-btn');
const chatHeader        = document.getElementById('chat-header');
const chatMessages      = document.getElementById('chat-messages');
const chatInput         = document.getElementById('chat-input');
const chatSendBtn       = document.getElementById('chat-send-btn');
const sendRequestBtn    = document.getElementById('send-request-btn');
const viewRequestsBtn   = document.getElementById('view-requests-btn');
const requestsContainer = document.getElementById('friend-requests');

// ─── Feedback (toasts) ─────────────────────────────────────────────────────
const feedbackContainer = document.createElement('div');
feedbackContainer.id = 'feedback-container';
document.body.appendChild(feedbackContainer);
function showFeedback(msg) {
  const d = document.createElement('div');
  d.className = 'feedback-toast';
  d.textContent = msg;
  feedbackContainer.appendChild(d);
  setTimeout(() => d.remove(), 3000);
}
window.showFeedback = showFeedback;

// ─── UI helpers ────────────────────────────────────────────────────────────
function addMessage(line) {
  const div = document.createElement('div');
  div.textContent = line;
  chatMessages.appendChild(div);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function clearAndRenderChat(lines) {
  chatMessages.innerHTML = '';
  if (Array.isArray(lines)) {
    lines.forEach(addMessage);
  }
}

// ─── Populate functions (called by Java) ─────────────────────────────────
function populateUsers(list) {
  users = list;
}
window.populateUsers = populateUsers;

function populateFriends(list) {
  friends = list;
  friendsList.innerHTML = '';
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
    friendsList.appendChild(li);
  });
}
window.populateFriends = populateFriends;

function populateGroups(list) {
  groups = list;
  groupsList.innerHTML = '';
  groups.forEach(name => {
    const li = document.createElement('li');
    li.textContent = name;
    li.onclick = () => selectConversation(name, 'group');
    groupsList.appendChild(li);
  });
}
window.populateGroups = populateGroups;

function populateIncomingFriendInvites(list) {
  incomingFriendInvites = list;
  requestsContainer.innerHTML = '';
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
    requestsContainer.appendChild(row);
  });
}
window.populateFriendRequests = populateIncomingFriendInvites;

function populateOutgoingFriendRequests(list) {
  outgoingFriendInvites = list;
}
window.populateOutgoingFriendRequests = populateOutgoingFriendRequests;

function populateDirectMessages(lines) {
  directMessages = lines;
  clearAndRenderChat(lines);
}
window.populateDirectMessages = populateDirectMessages;

function populateGroupMessages({ group, messages }) {
  groupMessages[group] = messages;
  if (currentConversation === group && currentType === 'group') {
    clearAndRenderChat(messages);
  }
}
window.populateGroupMessages = populateGroupMessages;

// ─── Conversation selection ────────────────────────────────────────────────
let currentConversation, currentType;
function selectConversation(name, type) {
  currentConversation = name;
  currentType = type;
  chatHeader.textContent = name;

  if (type === 'friend') {
    // Request messages for this friend
    window.alert(JSON.stringify({
      action: 'selectFriend',
      target: name
    }));
  } else {
    // Request messages for this group
    window.alert(JSON.stringify({
      action: 'selectGroup',
      target: name
    }));
  }
}

// ─── Action handlers ──────────────────────────────────────────────────────
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

// ─── Tabs ─────────────────────────────────────────────────────────────────
friendsTab.addEventListener('click', () => {
  friendsTab.classList.add('active');
  groupsTab.classList.remove('active');
  friendsList.classList.remove('hidden');
  groupsList.classList.add('hidden');
  addGroupBtn.classList.add('hidden');
  searchInput.placeholder = 'Search friends...';
});

groupsTab.addEventListener('click', () => {
  groupsTab.classList.add('active');
  friendsTab.classList.remove('active');
  groupsList.classList.remove('hidden');
  friendsList.classList.add('hidden');
  addGroupBtn.classList.remove('hidden');
  searchInput.placeholder = 'Search groups...';
});

// ─── Search ────────────────────────────────────────────────────────────────
searchInput.addEventListener('input', () => {
  const q = searchInput.value.toLowerCase();
  const listEl = friendsTab.classList.contains('active') ? friendsList : groupsList;
  Array.from(listEl.children).forEach(li => {
    li.style.display = li.textContent.toLowerCase().includes(q) ? '' : 'none';
  });
});

// ─── Create Group ─────────────────────────────────────────────────────────
addGroupBtn.addEventListener('click', () => {
  const name = prompt('Group name:');
  if (!name) return;
  window.alert(JSON.stringify({
    action: 'createGroup',
    groupName: name
  }));
});

// ─── Send Message ─────────────────────────────────────────────────────────
chatSendBtn.addEventListener('click', () => {
  if (!currentConversation) return;
  const msg = chatInput.value.trim();
  if (!msg) return;

  const action = currentType === 'friend' ? 'sendFriendMessage' : 'sendGroupMessage';
  window.alert(JSON.stringify({
    action,
    target: currentConversation,
    message: msg
  }));
  chatInput.value = '';
});

// Allow sending with Enter key
chatInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') {
    chatSendBtn.click();
  }
});

// ─── Friend Requests ──────────────────────────────────────────────────────
sendRequestBtn.addEventListener('click', () => {
  const u = document.getElementById('friend-request-input').value.trim();
  if (!u) return;
  window.alert(JSON.stringify({
    action: 'sendFriendRequest',
    target: u
  }));
});

viewRequestsBtn.addEventListener('click', () => {
  window.alert(JSON.stringify({
    action: 'getFriendRequests'
  }));
});