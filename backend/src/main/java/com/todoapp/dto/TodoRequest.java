package com.todoapp.dto;

import com.todoapp.model.Todo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** DTO for incoming create/update requests. Keeps JPA entities out of the API layer. */
public class TodoRequest {

    @NotBlank(message = "Title must not be empty")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    // NFR-03: bound unbounded text input as defence-in-depth against oversized payloads
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Todo.Priority priority;

    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Todo.Priority getPriority() {
        return priority;
    }

    public void setPriority(Todo.Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
