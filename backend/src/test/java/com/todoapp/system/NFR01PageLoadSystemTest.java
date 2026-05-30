package com.todoapp.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.todoapp.model.Todo;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * System tests for NFR-01: Page Load Performance.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>GET /api/todos responds within 500 ms when the list holds 100 tasks.
 *   <li>Full page load (empty list) completes within 2 seconds.
 *   <li>Full page load with 100 tasks completes within 2 seconds.
 *   <li>10 consecutive page loads all complete within 2 seconds.
 * </ul>
 */
class NFR01PageLoadSystemTest extends SystemTestBase {

    /** Inserts {@code count} tasks directly via the repository (fast, no HTTP round-trip). */
    private void seedTasks(int count) {
        List<Todo> tasks = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Todo t = new Todo();
            t.setTitle("Perf task " + i);
            t.setPriority(Todo.Priority.MEDIUM);
            tasks.add(t);
        }
        todoRepository.saveAll(tasks);
    }

    @Test
    void apiEndpoint_with100Tasks_respondsWithin500ms() {
        seedTasks(100);

        // page.waitForResponse blocks until the matching response arrives;
        // wall-clock measures total time from navigate() to API response received.
        long start = System.currentTimeMillis();
        page.waitForResponse(
                response ->
                        response.url().contains("/api/todos")
                                && !response.url().contains("/status"),
                () -> page.navigate(baseUrl()));
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).as("Time until /api/todos responded (ms)").isLessThan(500);
    }

    @Test
    void pageLoad_emptyList_completesWithin2Seconds() {
        long start = System.currentTimeMillis();
        page.navigate(baseUrl());
        page.waitForSelector(".app-header");
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).as("Page load time (ms)").isLessThan(2000);
    }

    @Test
    void pageLoad_with100Tasks_completesWithin2Seconds() {
        seedTasks(100);

        long start = System.currentTimeMillis();
        page.navigate(baseUrl());
        page.waitForCondition(() -> page.locator(".task-item").count() >= 10);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).as("Page load time with 100 tasks (ms)").isLessThan(2000);
    }

    @Test
    void pageLoad_10ConsecutiveLoads_allCompleteWithin2Seconds() {
        seedTasks(20);

        for (int i = 1; i <= 10; i++) {
            long start = System.currentTimeMillis();
            page.navigate(baseUrl());
            page.waitForSelector(".task-item");
            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).as("Consecutive load #" + i + " (ms)").isLessThan(2000);
        }
    }
}
