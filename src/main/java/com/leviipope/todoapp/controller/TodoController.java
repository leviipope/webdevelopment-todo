package com.leviipope.todoapp.controller;

import com.leviipope.todoapp.dto.TodoResponse;
import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // Get todos for the currently logged-in user
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getMyTodos() {
        String username = getCurrentUsername();
        List<Todo> todos = todoService.getTodosByUsername(username);
        List<TodoResponse> response = todos.stream()
                .map(todo -> new TodoResponse(
                        todo.getId(),
                        todo.getTitle(),
                        todo.getDescription(),
                        todo.isCompleted(),
                        todo.getUser().getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);

    }

    // Get all todos (admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TodoResponse>> getAllTodos() {
        List<Todo> todos = todoService.getAllTodos();
        List<TodoResponse> response = todos.stream()
                .map(todo -> new TodoResponse(
                        todo.getId(),
                        todo.getTitle(),
                        todo.getDescription(),
                        todo.isCompleted(),
                        todo.getUser().getId()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Create a new todo for the current user
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@RequestBody Todo todo) {
        String username = getCurrentUsername();
        Todo createdTodo = todoService.createTodoForUsername(todo, username);
        TodoResponse response = new TodoResponse(
                createdTodo.getId(),
                createdTodo.getTitle(),
                createdTodo.getDescription(),
                createdTodo.isCompleted(),
                createdTodo.getUser().getId()
        );
        return ResponseEntity.ok(response);
    }

    // Get single todo by id (only if it belongs to current user)
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable Long id) {
        String username = getCurrentUsername();
        Todo todo = todoService.getTodoByIdAndUsername(id, username);
        TodoResponse response = new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getUser().getId()
        );
        return ResponseEntity.ok(response);
    }

    // Update existing todo (only if it belongs to current user)
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        String username = getCurrentUsername();
        Todo updatedTodo = todoService.updateTodoForUsername(id, todo, username);
        TodoResponse response = new TodoResponse(
                updatedTodo.getId(),
                updatedTodo.getTitle(),
                updatedTodo.getDescription(),
                updatedTodo.isCompleted(),
                updatedTodo.getUser().getId()
        );
        return ResponseEntity.ok(response);
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
