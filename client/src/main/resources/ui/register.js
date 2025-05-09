window.onload = () => {
  const statusEl = document.getElementById('status');
  const registerBtn = document.getElementById('registerBtn');
  const loginLink = document.getElementById('loginLink');

  registerBtn.onclick = () => {
    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const payload = JSON.stringify({
      action: 'register',
      username,
      email,
      password
    });
    alert(payload);
  };

  loginLink.onclick = () => {
    const payload = JSON.stringify({ action: 'openLogin' });
    alert(payload);
  };
};

// Called from Java:
function setStatus(message, success) {
  const statusEl = document.getElementById('status');
  statusEl.textContent = message;
  statusEl.style.color = success ? 'green' : 'red';
}
