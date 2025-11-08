package com.leviipope.todoapp.service;

import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.model.User;
import com.leviipope.todoapp.repository.TodoRepository;
import com.leviipope.todoapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TodoService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    public TodoService(UserRepository userRepository, TodoRepository todoRepository) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found: " + username
                ));
    }

    private void verifyTodoOwnership(Todo todo, User user) {
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to access this todo"
            );
        }
    }

    // Get todos for the currently logged-in user
    public List<Todo> getTodosByUsername(String username) {
        User user = findUserByUsername(username);
        return todoRepository.findByUser(user);
    }

    // Get all todos (admin)
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    // Create a new todo for the current user
    public Todo createTodoForUsername(Todo todo, String username) {
        User user = findUserByUsername(username);
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    // Get single todo by id (only if it belongs to current user)
    public Todo getTodoByIdAndUsername(Long id, String username) {
        User user = findUserByUsername(username);
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        verifyTodoOwnership(todo, user);

        return todo;
    }

    // Update existing todo (only if it belongs to current user)
    public Todo updateTodoForUsername(Long id, Todo updatedTodo, String username) {
        User user = findUserByUsername(username);
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        verifyTodoOwnership(existingTodo, user);

        existingTodo.setTitle(updatedTodo.getTitle());
        existingTodo.setDescription(updatedTodo.getDescription());
        existingTodo.setCompleted(updatedTodo.isCompleted());

        return todoRepository.save(existingTodo);
    }

    // Delete todo (only if it belongs to current user)
    public void deleteTodoForUsername(Long id, String username) {
        User user = findUserByUsername(username);
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        verifyTodoOwnership(todo, user);

        todoRepository.delete(todo);
    }
}
