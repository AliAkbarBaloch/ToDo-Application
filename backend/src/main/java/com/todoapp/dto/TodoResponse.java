package com.todoapp.dto;

import com.todoapp.model.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for outgoing API responses.
 * Never expose JPA entities directly — this controls what the client sees.
 */
public class TodoResponse {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Todo.Priority priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TodoResponse from(Todo todo) {
        TodoResponse r = new TodoResponse();
        r.id = todo.getId();
        r.title = todo.getTitle();
        r.description = todo.getDescription();
        r.completed = todo.isCompleted();
        r.priority = todo.getPriority();
        r.dueDate = todo.getDueDate();
        r.createdAt = todo.getCreatedAt();
        r.updatedAt = todo.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public Todo.Priority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
