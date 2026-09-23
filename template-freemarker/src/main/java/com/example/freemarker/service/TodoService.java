package com.example.freemarker.service;

import com.example.freemarker.model.Todo;
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
        seed("Master FreeMarker templates", "Learn FTL directives and built-ins", false, "HIGH");
        seed("Implement custom directives", "Create @layout and user-defined macros", false, "MEDIUM");
        seed("Configure auto-escaping", "Set up .ftlh for HTML escaping", true, "LOW");
        seed("Set up CI/CD pipeline", "Automate build and deploy", false, "URGENT");
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

    public long countByStatus(boolean completed) {
        return store.values().stream().filter(t -> t.isCompleted() == completed).count();
    }
}
