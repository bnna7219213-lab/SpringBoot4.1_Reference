package com.example.thyme.model;

import java.time.LocalDateTime;

/**
 * Domain model for Todo items — shared across all template engine demos.
 */
public class Todo {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;  // LOW, MEDIUM, HIGH, URGENT
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Todo() {}

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
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isUrgent() { return "URGENT".equals(priority); }
    public boolean isHighPriority() { return "HIGH".equals(priority) || "URGENT".equals(priority); }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Todo{id=%d, title='%s', completed=%s, priority='%s'}".formatted(id, title, completed, priority);
    }
}
