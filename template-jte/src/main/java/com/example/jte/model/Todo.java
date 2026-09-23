package com.example.jte.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JTE domain model.
 *
 * JTE generates Java bytecode from templates — template parameters become
 * method parameters at compile time. This means:
 * - Compile-time type safety: referencing a non-existent property causes a COMPILE ERROR
 * - Excellent performance: templates are pre-compiled to Java classes
 * - Hot code reload in development mode
 */
public class Todo {

    private final Long id;
    private final String title;
    private final String description;
    private final boolean completed;
    private final String priority;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Todo(Long id, String title, String description, boolean completed, String priority,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public String getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null) return "N/A";
        return updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getPriorityCssClass() {
        return switch (priority != null ? priority : "LOW") {
            case "URGENT" -> "urgent";
            case "HIGH" -> "high";
            case "MEDIUM" -> "medium";
            default -> "low";
        };
    }

    public String getStatusText() {
        return completed ? "Done" : "Pending";
    }

    public String getToggleAction() {
        return completed ? "Undo" : "Complete";
    }

    public String getPageDescription() {
        return description != null ? description : "No description provided";
    }
}
