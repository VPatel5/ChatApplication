window.onload = () => {
  const statusEl = document.getElementById('status');
  const loginBtn = document.getElementById('loginBtn');
  const registerLink = document.getElementById('registerLink');

  loginBtn.onclick = () => {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const payload = JSON.stringify({ action: 'login', username, password });
    alert(payload);  // Handled by WebUI via `engine.setOnAlert`
  };

  registerLink.onclick = () => {
    const payload = JSON.stringify({ action: 'openRegister' });
    alert(payload);
  };
};

// Called from Java when login/register succeeds/fails:
function setStatus(message, success) {
  const statusEl = document.getElementById('status');
  statusEl.textContent = message;
  statusEl.style.color = success ? 'green' : 'red';
}
