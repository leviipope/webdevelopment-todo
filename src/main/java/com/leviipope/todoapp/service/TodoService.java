package com.leviipope.todoapp.service;

import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found for id :: " + id));
    }

    public Todo createTodo(Todo todo){
        return todoRepository.save(todo);
    }

    public Todo toggleTodoStatus(Long id) {
        Todo todo = getTodoById(id);
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    public void deleteTodoById(Long id) {
        Todo todo = getTodoById(id);
        todoRepository.delete(todo);
    }
}
