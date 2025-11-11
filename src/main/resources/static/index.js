let token = localStorage.getItem('token');
let username = localStorage.getItem('username');

if (token) {
    showTodoSection();
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
        token = data.token;
        username = data.username;
        localStorage.setItem('token', token);
        localStorage.setItem('username', username);
        showTodoSection();
    } else {
        document.getElementById('loginError').textContent = 'Invalid credentials';
    }
});

document.getElementById('addTodoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('todoTitle').value.trim();
    const description = document.getElementById('todoDescription').value.trim();

    if (!title) {
        alert('Title is required');
        return;
    }

    try {
        const response = await fetch('/api/todos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ title, description, completed: false })
        });

        if (response.status === 401) {
            alert('Session expired. Please login again.');
            logout();
            return;
        }

        if (!response.ok) {
            throw new Error('Failed to create todo');
        }

        document.getElementById('addTodoForm').reset();
        loadTodos();
    } catch (error) {
        console.error('Error creating todo:', error);
        alert('Failed to create todo');
    }
});

document.getElementById('editTodoForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('editTodoId').value;
    const title = document.getElementById('editTodoTitle').value;
    const description = document.getElementById('editTodoDescription').value;
    const completed = document.getElementById('editTodoCompleted').checked;

    await fetch(`/api/todos/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ title, description, completed })
    });

    cancelEdit();
    loadTodos();
});

async function loadTodos() {
    const response = await fetch('/api/todos', {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    const todos = await response.json();
    document.getElementById('todoList').innerHTML = todos.map(todo => `
            <div>
                <p><strong>${todo.title}</strong> ${todo.completed ? '(Completed)' : ''}</p>
                <p>${todo.description || ''}</p>
                <button onclick="editTodo(${todo.id})">Edit</button>
                <button onclick="deleteTodo(${todo.id})">Delete</button>
            </div>
        `).join('');
}

async function editTodo(id) {
    const response = await fetch(`/api/todos/${id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const todo = await response.json();

    document.getElementById('editTodoId').value = todo.id;
    document.getElementById('editTodoTitle').value = todo.title;
    document.getElementById('editTodoDescription').value = todo.description || '';
    document.getElementById('editTodoCompleted').checked = todo.completed;
    document.getElementById('editSection').style.display = 'block';
}

function cancelEdit() {
    document.getElementById('editSection').style.display = 'none';
}

async function deleteTodo(id) {
    if (!confirm('Delete this todo?')) return;

    await fetch(`/api/todos/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
    });

    loadTodos();
}

function showTodoSection() {
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('todoSection').style.display = 'block';
    document.getElementById('currentUsername').textContent = username;
    loadTodos();
}

function logout() {
    localStorage.clear();
    location.reload();
}

