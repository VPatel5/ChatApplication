let currentTarget = null;
window.onload = () => {
  // init lists
  alert(JSON.stringify({action:'listFriends'}));
  alert(JSON.stringify({action:'listGroups'}));

  document.getElementById('sendBtn').onclick = () => {
    const msg = document.getElementById('msgField').value.trim();
    if (currentTarget && msg) {
      alert(JSON.stringify({ action:'sendMessage', target:currentTarget, message:msg }));
      document.getElementById('msgField').value = '';
    }
  };
};

function populateFriends(arr) {
  const ul = document.getElementById('friendsList');
  ul.innerHTML = '';
  arr.forEach(u => {
    const li = document.createElement('li');
    li.textContent = u;
    li.onclick = () => { currentTarget = u; alert(JSON.stringify({action:'selectTarget',target:u})); };
    ul.appendChild(li);
  });
}

function populateGroups(arr) {
  const ul = document.getElementById('groupsList');
  ul.innerHTML = '';
  arr.forEach(g => {
    const li = document.createElement('li');
    li.textContent = g;
    li.onclick = () => { currentTarget = g; alert(JSON.stringify({action:'selectTarget',target:g})); };
    ul.appendChild(li);
  });
}

function populateMessages(arr) {
  const md = document.getElementById('messages');
  md.innerHTML = '';
  arr.forEach(line => {
    const p = document.createElement('p');
    p.textContent = line;
    md.appendChild(p);
  });
}
