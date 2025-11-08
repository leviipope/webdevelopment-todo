package com.leviipope.todoapp.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leviipope.todoapp.model.Todo;
import com.leviipope.todoapp.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DataLoader(TodoRepository todoRepository, ObjectMapper objectMapper, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadData() {
        if (userRepository.count() == 0) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/data/users.json");
                List<User> users = objectMapper.readValue(inputStream, new TypeReference<>() {});
                userRepository.saveAll(users);
                System.out.println("Loaded " + users.size() + " users from JSON");
            } catch (Exception e) {
                System.out.println("Failed to load USERS: " + e.getMessage());
            }
        } else {
                System.out.println("Database already populated with users");
        }

        if (todoRepository.count() == 0) {
            try {
                InputStream inputStream = getClass().getResourceAsStream("/data/todos.json");
                List<Todo> todos = objectMapper.readValue(inputStream, new TypeReference<>() {
                });
                todoRepository.saveAll(todos);
                System.out.println("Loaded " + todos.size() + " todos from JSON");
            } catch (Exception e) {
                System.out.println("Failed to load TODOS: " + e.getMessage());
            }
        } else {
            System.out.println("Database already populated with todos");
        }
    }
}
