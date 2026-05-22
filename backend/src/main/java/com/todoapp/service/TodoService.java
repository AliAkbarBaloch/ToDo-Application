package com.todoapp.service;

import com.todoapp.dto.TodoRequest;
import com.todoapp.dto.TodoResponse;
import com.todoapp.model.Todo;
import com.todoapp.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MVC — Service layer (business logic). No SQL or JPA details here — delegates all data access to
 * TodoRepository.
 */
@Service
@Transactional
public class TodoService {

    // US-08: High-priority tasks appear first; ties broken by creation date (newest first)
    private static final Map<Todo.Priority, Integer> PRIORITY_ORDER =
            Map.of(Todo.Priority.HIGH, 0, Todo.Priority.MEDIUM, 1, Todo.Priority.LOW, 2);

    private static final Comparator<Todo> BY_PRIORITY_THEN_CREATED =
            Comparator.comparingInt((Todo t) -> PRIORITY_ORDER.getOrDefault(t.getPriority(), 1))
                    .thenComparing(Comparator.comparing(Todo::getCreatedAt).reversed());

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoResponse> getAllTodos(String status, String search) {
        List<Todo> todos;
        if (search != null && !search.isBlank()) {
            todos = todoRepository.searchByKeyword(search.trim());
        } else if ("active".equalsIgnoreCase(status)) {
            todos = todoRepository.findByCompletedOrderByCreatedAtDesc(false);
        } else if ("completed".equalsIgnoreCase(status)) {
            todos = todoRepository.findByCompletedOrderByCreatedAtDesc(true);
        } else {
            todos = todoRepository.findAllByOrderByCreatedAtDesc();
        }
        // US-08: sort by priority (HIGH → MEDIUM → LOW), then newest first within each group
        return todos.stream().sorted(BY_PRIORITY_THEN_CREATED).map(TodoResponse::from).toList();
    }

    public TodoResponse getTodoById(Long id) {
        return TodoResponse.from(findOrThrow(id));
    }

    public TodoResponse createTodo(TodoRequest request) {
        Todo todo = new Todo();
        applyRequest(todo, request);
        // MN-03: use fromWithWarning so response includes warning:"due_date_in_past" when
        // applicable
        return TodoResponse.fromWithWarning(todoRepository.save(todo));
    }

    public TodoResponse updateTodo(Long id, TodoRequest request) {
        Todo todo = findOrThrow(id);
        applyRequest(todo, request);
        return TodoResponse.from(todoRepository.save(todo));
    }

    public TodoResponse toggleStatus(Long id) {
        Todo todo = findOrThrow(id);
        todo.setCompleted(!todo.isCompleted());
        return TodoResponse.from(todoRepository.save(todo));
    }

    public void deleteTodo(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new EntityNotFoundException("Todo not found: " + id);
        }
        todoRepository.deleteById(id);
    }

    private void applyRequest(Todo todo, TodoRequest request) {
        todo.setTitle(request.getTitle().trim());
        todo.setDescription(request.getDescription());
        todo.setDueDate(request.getDueDate());
        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }
    }

    private Todo findOrThrow(Long id) {
        return todoRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Todo not found: " + id));
    }
}
