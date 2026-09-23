package com.example.mustache.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * MUSTACHE LOGIC-LESS DESIGN:
     * Because Mustache cannot perform any logic in templates, all derived values
     * must be computed in Java (controller or service) before passing to the template.
     * This is a KEY DIFFERENCE from Thymeleaf/FreeMarker: computation happens in Java.
     */
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getPriorityCssClass() {
        if ("URGENT".equals(priority)) return "urgent";
        if ("HIGH".equals(priority)) return "high";
        if ("MEDIUM".equals(priority)) return "medium";
        return "low";
    }

    public String getStatusText() {
        return completed ? "Done" : "Pending";
    }

    public String getCssClassForList() {
        return (completed ? "todo-item completed" : "todo-item") + " " + getPriorityCssClass();
    }

    // - additional display properties for Mustache logic-less templates - */

    public String getPriorityBgColor() {
        if ("URGENT".equals(priority)) return "#fef2f2";
        if ("HIGH".equals(priority)) return "#fffbeb";
        if ("MEDIUM".equals(priority)) return "#eff6ff";
        return "#ecfdf5";
    }

    public String getPriorityTextColor() {
        if ("URGENT".equals(priority)) return "#ef4444";
        if ("HIGH".equals(priority)) return "#f59e0b";
        if ("MEDIUM".equals(priority)) return "#3b82f6";
        return "#10b981";
    }

    public String getStatusBgColor() {
        return completed ? "#f0fdf4" : "#fefce8";
    }

    public String getStatusTextColor() {
        return completed ? "#16a34a" : "#ca8a04";
    }

    public String getStatusIcon() {
        return completed ? "&#10003;" : "&#9711;";
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null) return "N/A";
        return updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getToggleText() {
        return completed ? "Undo" : "Complete";
    }
}
