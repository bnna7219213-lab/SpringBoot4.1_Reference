package com.example.freemarker.model;

import java.time.LocalDateTime;

public class Todo {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;
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

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * FreeMarker helper — FreeMarker can call methods, not just getters.
     */
    public String getPriorityCssClass() {
        if ("URGENT".equals(priority)) return "urgent";
        if ("HIGH".equals(priority)) return "high";
        if ("MEDIUM".equals(priority)) return "medium";
        return "low";
    }

    public String getStatusText() {
        return completed ? "Done" : "Pending";
    }
}
