document.getElementById('registerForm').addEventListener('submit', function(event) {
    event.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const email = document.getElementById('email').value;

    fetch('/api/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password, email })
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/login';
        } else {
            return response.text();
        }
    })
    .then(error => {
        if (error) {
            document.getElementById('error').textContent = error;
        }
    });
});
