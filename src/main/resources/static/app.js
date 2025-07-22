document.addEventListener('DOMContentLoaded', function() {
    // --- Form and Link Elements ---
    const loginContainer = document.getElementById('login-form-container');
    const registerContainer = document.getElementById('register-form-container');
    const showRegisterLink = document.getElementById('show-register');
    const showLoginLink = document.getElementById('show-login');
    const messageElement = document.getElementById('message');
    const registerForm = document.getElementById('registerForm');
    const loginForm = document.getElementById('loginForm');
    const registerButton = registerForm.querySelector('button');
    const loginButton = loginForm.querySelector('button');

    // --- Toggle Links Logic ---
    showRegisterLink.addEventListener('click', function(event) {
        event.preventDefault();
        loginContainer.style.display = 'none';
        registerContainer.style.display = 'block';
    });

    showLoginLink.addEventListener('click', function(event) {
        event.preventDefault();
        loginContainer.style.display = 'block';
        registerContainer.style.display = 'none';
    });

    // --- Registration Form Logic ---
    registerForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const name = document.getElementById('registerName').value;
        const email = document.getElementById('registerEmail').value;
        const password = document.getElementById('registerPassword').value;

        registerButton.classList.add('loading'); // Start loading
        messageElement.textContent = ''; // Clear old messages

        fetch('/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        })
        .then(response => {
            if (response.ok) {
                messageElement.textContent = "Registration successful! You can now log in.";
                messageElement.style.color = 'green';
                registerContainer.style.display = 'none';
                loginContainer.style.display = 'block';
            } else {
                messageElement.textContent = "Registration failed. Please try again.";
                messageElement.style.color = 'red';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            messageElement.textContent = "An error occurred during registration.";
            messageElement.style.color = 'red';
        })
        .finally(() => {
            registerButton.classList.remove('loading'); // Stop loading
        });
    });

    // --- Login Form Logic ---
    loginForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        loginButton.classList.add('loading'); // Start loading
        messageElement.textContent = ''; // Clear old messages

        fetch('/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        })
        .then(response => {
            if (!response.ok) throw new Error('Login failed');
            return response.json();
        })
        .then(data => {
            localStorage.setItem('jwtToken', data.token);
            window.location.href = 'dashboard.html';
        })
        .catch(error => {
            console.error('Error:', error);
            messageElement.textContent = "Login failed. Please check your credentials.";
            messageElement.style.color = 'red';
            loginButton.classList.remove('loading'); // Stop loading on error
        });
    });
});