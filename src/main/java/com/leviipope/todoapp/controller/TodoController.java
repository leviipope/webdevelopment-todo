package com.leviipope.todoapp.controller;

import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // Get todos for the currently logged-in user
    @GetMapping
    public ResponseEntity<List<Todo>> getMyTodos() {
        String username = getCurrentUsername();
        List<Todo> todos = todoService.getTodosByUsername(username);
        return ResponseEntity.ok(todos);
    }

    // Get all todos (admin only - we'll add security annotation later)
    @GetMapping("/all")
    public ResponseEntity<List<Todo>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        return ResponseEntity.ok(todos);
    }

    // Create a new todo for the current user
    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestBody Todo todo) {
        String username = getCurrentUsername();
        Todo createdTodo = todoService.createTodoForUsername(todo, username);
        return ResponseEntity.ok(createdTodo);
    }

    // Get single todo by id (only if it belongs to current user)
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        String username = getCurrentUsername();
        Todo todo = todoService.getTodoByIdAndUsername(id, username);
        return ResponseEntity.ok(todo);
    }

    // Update existing todo (only if it belongs to current user)
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        String username = getCurrentUsername();
        Todo updatedTodo = todoService.updateTodoForUsername(id, todo, username);
        return ResponseEntity.ok(updatedTodo);
    }

    // Delete todo (only if it belongs to current user)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        String username = getCurrentUsername();
        todoService.deleteTodoForUsername(id, username);
        return ResponseEntity.noContent().build();
    }

    // Helper method to get current logged-in username
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
