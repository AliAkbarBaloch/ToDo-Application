package com.todoapp.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todoapp.dto.TodoRequest;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private TodoRepository todoRepository;

    @Autowired private ObjectMapper objectMapper;

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

    // ── POST /api/todos ───────────────────────────────────────────────────────

    @Test
    void createTodo_validRequest_returns201WithCreatedTodo() throws Exception {
        TodoRequest req = buildRequest("New task", Todo.Priority.MEDIUM, null);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));
    }

    @Test
    void createTodo_emptyTitle_returns400WithFieldError() throws Exception {
        TodoRequest req = buildRequest("", Todo.Priority.LOW, null);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTodo_titleTooLong_returns400WithFieldError() throws Exception {
        TodoRequest req = buildRequest("A".repeat(201), Todo.Priority.LOW, null);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTodo_pastDueDate_returns201WithWarning() throws Exception {
        TodoRequest req = buildRequest("Old task", Todo.Priority.LOW, LocalDate.now().minusDays(1));

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warning", is("due_date_in_past")));
    }

    // ── DELETE /api/todos/{id} ────────────────────────────────────────────────

    @Test
    void deleteTodo_existingId_returns204() throws Exception {
        Todo saved = persistTodo("To delete", false, Todo.Priority.LOW);

        mockMvc.perform(delete("/api/todos/{id}", saved.getId())).andExpect(status().isNoContent());
    }

    @Test
    void deleteTodo_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/api/todos/{id}", 9999L)).andExpect(status().isNotFound());
    }

    // ── PATCH /api/todos/{id}/status ─────────────────────────────────────────

    @Test
    void toggleStatus_activeTodo_returnsCompletedTodo() throws Exception {
        Todo saved = persistTodo("Task", false, Todo.Priority.MEDIUM);

        mockMvc.perform(patch("/api/todos/{id}/status", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    // ── PUT /api/todos/{id} ───────────────────────────────────────────────────

    @Test
    void updateTodo_existingId_returns200WithUpdatedTodo() throws Exception {
        Todo saved = persistTodo("Original", false, Todo.Priority.LOW);
        TodoRequest req = buildRequest("Updated title", Todo.Priority.HIGH, null);

        mockMvc.perform(
                        put("/api/todos/{id}", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void updateTodo_missingId_returns404() throws Exception {
        TodoRequest req = buildRequest("Title", Todo.Priority.MEDIUM, null);

        mockMvc.perform(
                        put("/api/todos/{id}", 9999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
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
        mockMvc.perform(get("/api/todos/{id}", 9999L)).andExpect(status().isNotFound());
    }

    @Test
    void getAllTodos_searchKeyword_returnsMatchingTodos() throws Exception {
        persistTodo("Buy groceries", false, Todo.Priority.LOW);
        persistTodo("Walk the dog", false, Todo.Priority.HIGH);

        mockMvc.perform(get("/api/todos").param("search", "grocer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy groceries"));
    }

    @Test
    void getAllTodos_statusFilter_returnsOnlyMatchingTodos() throws Exception {
        persistTodo("Active task", false, Todo.Priority.MEDIUM);
        persistTodo("Completed task", true, Todo.Priority.MEDIUM);

        mockMvc.perform(get("/api/todos").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Active task"));
    }

    // ── NFR-03: XSS / input sanitisation ────────────────────────────────────────

    @Test
    void createTodo_scriptTagInTitle_returns201AndStoredAsLiteralText() throws Exception {
        // React escapes JSX content by default; the server must store and return the raw
        // string unchanged so the client can display it as literal text (not execute it).
        String xssTitle = "<script>alert('xss')</script>";
        TodoRequest req = buildRequest(xssTitle, Todo.Priority.LOW, null);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(xssTitle)));
    }

    @Test
    void createTodo_htmlInDescription_returns201AndStoredAsLiteralText() throws Exception {
        String xssDesc = "<img src=x onerror=alert(1)>";
        TodoRequest req = buildRequest("Normal title", Todo.Priority.LOW, null);
        req.setDescription(xssDesc);

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is(xssDesc)));
    }

    @Test
    void createTodo_descriptionTooLong_returns400WithFieldError() throws Exception {
        TodoRequest req = buildRequest("Title", Todo.Priority.LOW, null);
        req.setDescription("A".repeat(2001));

        mockMvc.perform(
                        post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void getAllTodos_withData_returnsTodosInPriorityOrder() throws Exception {
        persistTodo("Low task", false, Todo.Priority.LOW);
        persistTodo("High task", false, Todo.Priority.HIGH);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("High task"))
                .andExpect(jsonPath("$[1].title").value("Low task"));
    }

    // ── US-09: search + status filter combined ───────────────────────────────

    @Test
    void getAllTodos_searchAndStatusFilter_returnOnlyMatchingActiveTask() throws Exception {
        // "Buy milk" active, "Buy milk done" completed — searching "milk" with Active filter
        // must return ONLY the active one (US-09 AC: search and filter work together)
        Todo active = persistTodo("Buy milk", false, Todo.Priority.LOW);
        active.setTitle("Buy milk");
        Todo completed = persistTodo("Buy milk done", true, Todo.Priority.LOW);

        mockMvc.perform(get("/api/todos").param("search", "milk").param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Buy milk"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }
}
