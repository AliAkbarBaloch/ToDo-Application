package com.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.dto.TodoRequest;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() {
        todoRepository.deleteAll();
    }

    private Todo persistTodo(String title, boolean completed, Todo.Priority priority) {
        Todo t = new Todo();
        t.setTitle(title);
        t.setCompleted(completed);
        t.setPriority(priority);
        return todoRepository.save(t);
    }

    private TodoRequest buildRequest(String title, Todo.Priority priority, LocalDate dueDate) {
        TodoRequest req = new TodoRequest();
        req.setTitle(title);
        req.setPriority(priority);
        req.setDueDate(dueDate);
        return req;
    }

    // ── GET /api/todos ────────────────────────────────────────────────────────

    @Test
    void getAllTodos_emptyDatabase_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/todos/{id} ───────────────────────────────────────────────────

    @Test
    void getTodoById_existingId_returns200WithTodo() throws Exception {
        Todo saved = persistTodo("My task", false, Todo.Priority.HIGH);

        mockMvc.perform(get("/api/todos/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My task"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void getTodoById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/api/todos/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTodos_searchKeyword_returnsMatchingTodos() throws Exception {
        persistTodo("Buy groceries", false, Todo.Priority.LOW);
        persistTodo("Walk the dog",  false, Todo.Priority.HIGH);

        mockMvc.perform(get("/api/todos").param("search", "grocer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy groceries"));
    }

    @Test
    void getAllTodos_statusFilter_returnsOnlyMatchingTodos() throws Exception {
        persistTodo("Active task",    false, Todo.Priority.MEDIUM);
        persistTodo("Completed task", true,  Todo.Priority.MEDIUM);

        mockMvc.perform(get("/api/todos").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Active task"));
    }

    @Test
    void getAllTodos_withData_returnsTodosInPriorityOrder() throws Exception {
        persistTodo("Low task",  false, Todo.Priority.LOW);
        persistTodo("High task", false, Todo.Priority.HIGH);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("High task"))
                .andExpect(jsonPath("$[1].title").value("Low task"));
    }
}
