let currentTarget = null;
window.onload = () => {
  const statusEl = document.getElementById('status');
  document.getElementById('loginBtn').onclick = () => {
    const u = document.getElementById('username').value.trim();
    const p = document.getElementById('password').value;
    if (u && p) {
      alert(JSON.stringify({ action:'login', username:u, password:p }));
      statusEl.textContent = 'Logging in…'; statusEl.style.color='black';
    }
  };
  document.getElementById('registerLink').onclick = () => {
    alert(JSON.stringify({ action:'openRegister' }));
  };
  window.setStatus = (msg, ok) => {
    statusEl.textContent = msg;
    statusEl.style.color = ok ? 'green' : 'red';
  };
};
