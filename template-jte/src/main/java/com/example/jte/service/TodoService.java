package com.example.jte.service;

import com.example.jte.model.Todo;
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
        seed("Explore JTE templates", "Kotlin-like syntax with Java type safety", false, "HIGH");
        seed("Create type-safe template params", "Compile-time errors for missing properties", false, "MEDIUM");
        seed("Configure hot reload", "Development mode auto-compiles on change", true, "LOW");
        seed("Measure performance", "JTE compiles to bytecode, fastest template engine", false, "URGENT");
    }

    private void seed(String title, String desc, boolean completed, String priority) {
        long id = idSeq.getAndIncrement();
        LocalDateTime now = LocalDateTime.now();
        store.put(id, new Todo(id, title, desc, completed, priority, now, now));
    }

    public List<Todo> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public void toggleCompleted(Long id) {
        // For JTE demo with immutable model, we replace the entry
        Todo todo = store.get(id);
        if (todo != null) {
            store.put(id, new Todo(
                    todo.getId(), todo.getTitle(), todo.getDescription(),
                    !todo.isCompleted(), todo.getPriority(),
                    todo.getCreatedAt(), LocalDateTime.now()));
        }
    }

    public long countCompleted() {
        return store.values().stream().filter(Todo::isCompleted).count();
    }

    public long countPending() {
        return store.values().stream().filter(t -> !t.isCompleted()).count();
    }
}
