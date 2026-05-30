package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-02: View All Todo Tasks.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>All stored tasks are displayed when the page loads.
 *   <li>An empty-state message is shown when no tasks exist.
 * </ul>
 */
class US02ViewTasksSystemTest extends SystemTestBase {

    @Test
    void viewTasks_withExistingTasks_allDisplayed() {
        addTask("Task One");
        addTask("Task Two");
        addTask("Task Three");

        // Reload to simulate opening the app fresh
        page.navigate(baseUrl());
        page.waitForSelector(".task-item");

        assertThat(page.locator(".task-item")).hasCount(3);
    }

    @Test
    void viewTasks_noTasks_emptyStateMessageShown() {
        // DB is empty — the empty-state block must be visible
        assertThat(page.locator(".empty-state")).isVisible();
        assertThat(page.locator(".empty-state p")).hasText("No tasks yet. Add one above!");
    }

    @Test
    void viewTasks_taskShowsTitleAndPriorityBadge() {
        addTask("Check display fields");

        assertThat(page.locator(".task-item .task-title").first()).hasText("Check display fields");
        // Default priority badge must be visible
        assertThat(page.locator(".task-item .priority-badge").first()).isVisible();
    }
}
