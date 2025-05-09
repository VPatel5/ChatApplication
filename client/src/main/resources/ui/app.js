// Elements
const friendsTab   = document.getElementById('friends-tab');
const groupsTab    = document.getElementById('groups-tab');
const searchInput  = document.getElementById('search-input');
const friendsList  = document.getElementById('friends-list');
const groupsList   = document.getElementById('groups-list');
const addGroupBtn  = document.getElementById('add-group-btn');
const chatHeader   = document.getElementById('chat-header');
const chatMessages = document.getElementById('chat-messages');
const chatInput    = document.getElementById('chat-input');
const chatSendBtn  = document.getElementById('chat-send-btn');

// State
let selectedType = null; // 'friend' or 'group'
let selectedName = null;

// === Tab Switching ===
friendsTab.addEventListener('click', () => switchTab('friends'));
groupsTab .addEventListener('click', () => switchTab('groups'));
function switchTab(tab) {
  selectedType = null; selectedName = null;
  friendsTab.classList.toggle('active', tab==='friends');
  groupsTab .classList.toggle('active', tab==='groups');

  searchInput.placeholder = tab==='friends'
    ? 'Search friends...' : 'Search groups...';
  friendsList.classList.toggle('hidden', tab!=='friends');
  groupsList .classList.toggle('hidden', tab!=='groups');
  chatHeader.textContent = 'Select a conversation';
  chatMessages.innerHTML = '';
}

// === Search on Enter ===
searchInput.addEventListener('keyup', e => {
  if (e.key !== 'Enter') return;
  const query = searchInput.value.trim();
  if (friendsTab.classList.contains('active')) {
    alert(JSON.stringify({ action:'searchFriends', query }));
  } else {
    alert(JSON.stringify({ action:'searchGroups', query }));
  }
});

// === Create Group ===
addGroupBtn.addEventListener('click', () => {
  const name = prompt('Enter new group name:');
  if (!name) return;
  alert(JSON.stringify({ action:'createGroup', groupName:name }));
});

// === Populate Lists (called from Java) ===
function populateFriends(names) {
  friendsList.innerHTML = '';
  names.forEach(name => {
    const li = document.createElement('li');
    li.textContent = name;
    li.onclick = () => selectConversation('friend', name);
    friendsList.appendChild(li);
  });
}

function populateGroups(names) {
  groupsList.innerHTML = '';
  names.forEach(name => {
    const li = document.createElement('li');
    li.textContent = name;
    li.onclick = () => selectConversation('group', name);
    groupsList.appendChild(li);
  });
}

// === Select Conversation ===
function selectConversation(type, name) {
  selectedType = type;
  selectedName = name;
  chatHeader.textContent = name;
  chatMessages.innerHTML = '';
  alert(JSON.stringify({ action: type==='friend' ? 'selectFriend' : 'selectGroup', target: name }));
}

// === Populate Messages (called from Java) ===
function populateMessages(lines) {
  chatMessages.innerHTML = '';
  lines.forEach(line => {
    const div = document.createElement('div');
    div.textContent = line;
    chatMessages.appendChild(div);
  });
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

// === Send Message ===
chatSendBtn.addEventListener('click', () => {
  if (!selectedName) return;
  const msg = chatInput.value.trim();
  if (!msg) return;
  const action = selectedType==='friend' ? 'sendFriendMessage' : 'sendGroupMessage';
  alert(JSON.stringify({ action, target:selectedName, message:msg }));
  chatInput.value = '';
});

// === Initial Load ===
window.onload = () => {
  switchTab('friends');
  alert(JSON.stringify({ action:'listFriends' }));
  alert(JSON.stringify({ action:'listGroups' }));
};
