package com.leviipope.todoapp.service;

import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.model.User;
import com.leviipope.todoapp.repository.TodoRepository;
import com.leviipope.todoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all todos for a specific user by username
    public List<Todo> getTodosByUsername(String username) {
        User user = findUserByUsername(username);
        return todoRepository.findByUser(user);
    }

    // Get all todos (for admin purposes)
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    // Create a new todo for a specific user
    public Todo createTodoForUsername(Todo todo, String username) {
        User user = findUserByUsername(username);
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    // Get a single todo by ID, ensuring it belongs to the user
    public Todo getTodoByIdAndUsername(Long id, String username) {
        User user = findUserByUsername(username);
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        // Verify the todo belongs to this user
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to access this todo"
            );
        }

        return todo;
    }

    // Update a todo, ensuring it belongs to the user
    public Todo updateTodoForUsername(Long id, Todo updatedTodo, String username) {
        User user = findUserByUsername(username);
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        // Verify the todo belongs to this user
        if (!existingTodo.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to modify this todo"
            );
        }

        // Update fields
        existingTodo.setTitle(updatedTodo.getTitle());
        existingTodo.setDescription(updatedTodo.getDescription());
        existingTodo.setCompleted(updatedTodo.isCompleted());

        return todoRepository.save(existingTodo);
    }

    // Delete a todo, ensuring it belongs to the user
    public void deleteTodoForUsername(Long id, String username) {
        User user = findUserByUsername(username);
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo not found with id: " + id
                ));

        // Verify the todo belongs to this user
        if (!todo.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this todo"
            );
        }

        todoRepository.delete(todo);
    }

    // Helper method to find user by username with proper error handling
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found: " + username
                ));
    }
}
