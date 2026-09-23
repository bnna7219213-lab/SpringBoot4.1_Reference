package com.example.thyme.service;

import com.example.thyme.model.Todo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TodoService {

    private final ConcurrentHashMap<Long, Todo> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public TodoService() {
        // Seed demo data
        seed("Learn Thymeleaf basics", "Study fragments, expressions, and form binding", false, "HIGH");
        seed("Build REST API", "Create Spring Boot CRUD endpoints", false, "MEDIUM");
        seed("Write unit tests", "Cover service and controller layers", true, "LOW");
        seed("Deploy to production", "Setup CI/CD pipeline", false, "URGENT");
    }

    private void seed(String title, String desc, boolean completed, String priority) {
        long id = idSeq.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        Todo todo = new Todo(id, title, desc, completed, priority, now, now);
        store.put(id, todo);
    }

    public List<Todo> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void toggleCompleted(Long id) {
        Todo todo = store.get(id);
        if (todo != null) {
            todo.setCompleted(!todo.isCompleted());
            todo.setUpdatedAt(LocalDateTime.now());
        }
    }

    public long countCompleted() {
        return store.values().stream().filter(Todo::isCompleted).count();
    }

    public long countPending() {
        return store.values().stream().filter(t -> !t.isCompleted()).count();
    }

    public long countByPriority(String priority) {
        return store.values().stream().filter(t -> priority.equals(t.getPriority())).count();
    }
}
