package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-03: Mark a Task as Completed.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Clicking the checkbox marks a task completed (CSS class 'completed' applied).
 *   <li>Clicking again toggles the task back to active.
 *   <li>Completed status persists across a page reload.
 * </ul>
 */
class US03MarkCompleteSystemTest extends SystemTestBase {

    @Test
    void markComplete_activeTask_getsCompletedStyle() {
        addTask("Finish report");

        page.locator(".task-checkbox").first().click();

        // task-item element must carry the 'completed' CSS class
        assertThat(page.locator(".task-item.completed")).hasCount(1);
    }

    @Test
    void markComplete_completedTask_togglesBackToActive() {
        addTask("Finish report");
        page.locator(".task-checkbox").first().click();
        assertThat(page.locator(".task-item.completed")).hasCount(1);

        page.locator(".task-checkbox").first().click();

        assertThat(page.locator(".task-item.completed")).hasCount(0);
        assertThat(page.locator(".task-item")).hasCount(1);
    }

    @Test
    void markComplete_persistsAfterPageReload() {
        addTask("Persistent task");
        page.locator(".task-checkbox").first().click();
        assertThat(page.locator(".task-item.completed")).hasCount(1);

        page.navigate(baseUrl());
        page.waitForSelector(".task-item");

        assertThat(page.locator(".task-item.completed")).hasCount(1);
    }
}
