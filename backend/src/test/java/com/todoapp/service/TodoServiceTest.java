package com.todoapp.service;

import com.todoapp.dto.TodoRequest;
import com.todoapp.dto.TodoResponse;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    // Helper: build a Todo without going through JPA lifecycle
    private Todo makeTodo(Long id, String title, Todo.Priority priority, LocalDateTime createdAt) {
        Todo t = new Todo();
        t.setId(id);
        t.setTitle(title);
        t.setPriority(priority);
        setCreatedAt(t, createdAt);
        return t;
    }

    private void setCreatedAt(Todo todo, LocalDateTime value) {
        try {
            Field f = Todo.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(todo, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper: build a TodoRequest
    private TodoRequest makeRequest(String title, Todo.Priority priority, LocalDate dueDate) {
        TodoRequest req = new TodoRequest();
        req.setTitle(title);
        req.setPriority(priority);
        req.setDueDate(dueDate);
        return req;
    }

    // ── toggleStatus ─────────────────────────────────────────────────────────

    @Test
    void toggleStatus_activeTodo_becomesCompleted() {
        Todo todo = makeTodo(30L, "Active", Todo.Priority.MEDIUM, LocalDateTime.now());
        todo.setCompleted(false);
        when(todoRepository.findById(30L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        TodoResponse result = todoService.toggleStatus(30L);

        assertThat(result.isCompleted()).isTrue();
    }

    @Test
    void toggleStatus_completedTodo_becomesActive() {
        Todo todo = makeTodo(31L, "Done", Todo.Priority.HIGH, LocalDateTime.now());
        todo.setCompleted(true);
        when(todoRepository.findById(31L)).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        TodoResponse result = todoService.toggleStatus(31L);

        assertThat(result.isCompleted()).isFalse();
    }

    @Test
    void toggleStatus_missingId_throwsEntityNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.toggleStatus(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── updateTodo ────────────────────────────────────────────────────────────

    @Test
    void updateTodo_existingId_updatesFieldsAndReturnsResponse() {
        Todo existing = makeTodo(20L, "Old title", Todo.Priority.LOW, LocalDateTime.now());
        when(todoRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(todoRepository.save(existing)).thenReturn(existing);

        TodoRequest req = makeRequest("New title", Todo.Priority.HIGH, null);
        TodoResponse result = todoService.updateTodo(20L, req);

        assertThat(result.getTitle()).isEqualTo("New title");
        assertThat(result.getPriority()).isEqualTo(Todo.Priority.HIGH);
    }

    @Test
    void updateTodo_missingId_throwsEntityNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.updateTodo(99L, makeRequest("x", Todo.Priority.LOW, null)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createTodo ────────────────────────────────────────────────────────────

    @Test
    void createTodo_noDueDate_returnsResponseWithNoWarning() {
        TodoRequest req = makeRequest("New task", Todo.Priority.MEDIUM, null);
        Todo saved = makeTodo(5L, "New task", Todo.Priority.MEDIUM, LocalDateTime.now());
        when(todoRepository.save(any(Todo.class))).thenReturn(saved);

        TodoResponse result = todoService.createTodo(req);

        assertThat(result.getTitle()).isEqualTo("New task");
        assertThat(result.getWarning()).isNull();
    }

    @Test
    void createTodo_futureDueDate_returnsResponseWithNoWarning() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        TodoRequest req = makeRequest("Future task", Todo.Priority.HIGH, tomorrow);
        Todo saved = makeTodo(7L, "Future task", Todo.Priority.HIGH, LocalDateTime.now());
        saved.setDueDate(tomorrow);
        when(todoRepository.save(any(Todo.class))).thenReturn(saved);

        TodoResponse result = todoService.createTodo(req);

        assertThat(result.getWarning()).isNull();
    }

    @Test
    void createTodo_pastDueDate_returnsResponseWithDueDateInPastWarning() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TodoRequest req = makeRequest("Old task", Todo.Priority.LOW, yesterday);
        Todo saved = makeTodo(6L, "Old task", Todo.Priority.LOW, LocalDateTime.now());
        saved.setDueDate(yesterday);
        when(todoRepository.save(any(Todo.class))).thenReturn(saved);

        TodoResponse result = todoService.createTodo(req);

        assertThat(result.getWarning()).isEqualTo("due_date_in_past");
    }

    // ── getTodoById ───────────────────────────────────────────────────────────

    @Test
    void getTodoById_existingId_returnsTodoResponse() {
        Todo todo = makeTodo(10L, "Existing task", Todo.Priority.HIGH, LocalDateTime.now());
        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));

        TodoResponse result = todoService.getTodoById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Existing task");
    }

    @Test
    void getTodoById_missingId_throwsEntityNotFoundException() {
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.getTodoById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAllTodos ───────────────────────────────────────────────────────────

    @Test
    void getAllTodos_activeFilter_returnsOnlyActiveTodos() {
        Todo active = makeTodo(1L, "Active task", Todo.Priority.MEDIUM, LocalDateTime.now());
        active.setCompleted(false);

        when(todoRepository.findByCompletedOrderByCreatedAtDesc(false)).thenReturn(List.of(active));

        List<TodoResponse> result = todoService.getAllTodos("active", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCompleted()).isFalse();
        verify(todoRepository).findByCompletedOrderByCreatedAtDesc(false);
    }

    @Test
    void getAllTodos_searchKeyword_delegatesToSearchRepository() {
        Todo match = makeTodo(3L, "Buy groceries", Todo.Priority.MEDIUM, LocalDateTime.now());

        when(todoRepository.searchByKeyword("grocery")).thenReturn(List.of(match));

        List<TodoResponse> result = todoService.getAllTodos(null, "grocery");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Buy groceries");
        verify(todoRepository).searchByKeyword("grocery");
    }

    @Test
    void getAllTodos_completedFilter_returnsOnlyCompletedTodos() {
        Todo done = makeTodo(2L, "Done task", Todo.Priority.LOW, LocalDateTime.now());
        done.setCompleted(true);

        when(todoRepository.findByCompletedOrderByCreatedAtDesc(true)).thenReturn(List.of(done));

        List<TodoResponse> result = todoService.getAllTodos("completed", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isCompleted()).isTrue();
        verify(todoRepository).findByCompletedOrderByCreatedAtDesc(true);
    }

    @Test
    void getAllTodos_noFilter_returnsSortedByPriorityThenNewestFirst() {
        Todo low  = makeTodo(1L, "Low task",    Todo.Priority.LOW,    LocalDateTime.now().minusHours(1));
        Todo high = makeTodo(2L, "High task",   Todo.Priority.HIGH,   LocalDateTime.now());
        Todo med  = makeTodo(3L, "Medium task", Todo.Priority.MEDIUM, LocalDateTime.now().minusHours(2));

        when(todoRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(low, high, med));

        List<TodoResponse> result = todoService.getAllTodos(null, null);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("High task");
        assertThat(result.get(1).getTitle()).isEqualTo("Medium task");
        assertThat(result.get(2).getTitle()).isEqualTo("Low task");
    }
}
