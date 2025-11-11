let token = localStorage.getItem('token');
let username = localStorage.getItem('username');

if (token) {
    showAdminSection();
}

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: usernameInput, password })
    });

    if (response.ok) {
        const data = await response.json();
        console.log('Login response:', data);

        if (!data.role || !data.role.includes('ADMIN')) {
            document.getElementById('loginError').textContent = 'Admin access required';
            return;
        }
        token = data.token;
        username = data.username;
        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
        localStorage.setItem('role', data.role);
        showAdminSection();
    } else {
        document.getElementById('loginError').textContent = 'Invalid credentials';
    }
});

document.getElementById('createUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('createUsername').value;
    const password = document.getElementById('createPassword').value;
    const role = document.getElementById('createRole').value;

    const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ username, password, role })
    });

    if (response.ok) {
        document.getElementById('createUserForm').reset();
        loadUsers();
    } else {
        const error = await response.text();
        alert(error);
    }
});

document.getElementById('editUserForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('editUserId').value;
    const username = document.getElementById('editUsername').value;
    const password = document.getElementById('editPassword').value;
    const role = document.getElementById('editRole').value;

    const body = { username, role };
    if (password && password.trim() !== '') {
        body.password = password;
    }

    await fetch(`/api/users/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(body)
    });

    cancelEdit();
    loadUsers();
});

async function loadUsers() {
    const response = await fetch('/api/users', {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    if (!response.ok) {
        alert('Failed to load users');
        return;
    }

    const users = await response.json();
    document.getElementById('userList').innerHTML = users.map(user => `
            <div>
                <p><strong>${user.username}</strong> (${user.role})</p>
                <p>ID: ${user.id}</p>
                <button onclick="editUser(${user.id})">Edit</button>
                <button onclick="deleteUser(${user.id})">Delete</button>
            </div>
        `).join('');
}

async function editUser(id) {
    const response = await fetch(`/api/users/${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const user = await response.json();

    document.getElementById('editUserId').value = user.id;
    document.getElementById('editUsername').value = user.username;
    document.getElementById('editPassword').value = '';
    document.getElementById('editRole').value = user.role;
    document.getElementById('editSection').style.display = 'block';
}

function cancelEdit() {
    document.getElementById('editSection').style.display = 'none';
}

async function deleteUser(id) {
    if (!confirm('Delete this user?')) return;

    await fetch(`/api/users/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
    });

    loadUsers();
}

function showAdminSection() {
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('adminSection').style.display = 'block';
    document.getElementById('currentUsername').textContent = username;
    loadUsers();
}

function logout() {
    localStorage.clear();
    location.reload();
}

