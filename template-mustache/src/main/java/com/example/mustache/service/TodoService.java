package com.example.mustache.service;

import com.example.mustache.model.Todo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mustache service demonstrating logic-less template philosophy:
 * All computation must happen in Java — the template only renders what it's given.
 */
@Service
public class TodoService {

    private final ConcurrentHashMap<Long, Todo> store = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public TodoService() {
        seed("Explore Mustache logic-less nature", "No if/else/loop logic in templates", false, "HIGH");
        seed("Compute derived values in Java", "All pre-processing happens in service layer", false, "MEDIUM");
        seed("Use partials for composition", "{{> header}} / {{> footer}}", true, "LOW");
        seed("Handle missing values gracefully", "{{title}}{{!comment}} with optional display", false, "URGENT");
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
}
