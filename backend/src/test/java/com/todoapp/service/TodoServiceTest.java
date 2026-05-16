package com.todoapp.service;

import com.todoapp.dto.TodoResponse;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

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

    // ── getAllTodos (no filter) ────────────────────────────────────────────────

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
