window.onload = () => {
  const statusEl = document.getElementById('status');
  document.getElementById('registerBtn').onclick = () => {
    const u = document.getElementById('username').value.trim();
    const e = document.getElementById('email').value.trim();
    const p = document.getElementById('password').value;
    if (u && e && p) {
      alert(JSON.stringify({ action:'register', username:u, email:e, password:p }));
      statusEl.textContent = 'Registering…'; statusEl.style.color='black';
    }
  };
  document.getElementById('loginLink').onclick = () => {
    alert(JSON.stringify({ action:'openLogin' }));
  };
  window.setStatus = (msg, ok) => {
    statusEl.textContent = msg;
    statusEl.style.color = ok ? 'green' : 'red';
  };
};
