package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-07: Filter Tasks by Status.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Active filter shows only active tasks.
 *   <li>Completed filter shows only completed tasks.
 *   <li>All filter shows every task.
 * </ul>
 */
class US07FilterSystemTest extends SystemTestBase {

    @Test
    void filter_activeOnly_showsOnlyActiveTasks() {
        addTask("Active task");
        addTask("Task to complete");
        // Mark the second task as completed
        page.locator(".task-checkbox").last().click();

        page.locator(".filter-btn", new com.microsoft.playwright.Page.LocatorOptions()
                        .setHasText("Active"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item .task-title").first()).hasText("Active task");
    }

    @Test
    void filter_completedOnly_showsOnlyCompletedTasks() {
        addTask("Active task");
        addTask("Task to complete");
        page.locator(".task-checkbox").last().click();

        page.locator(".filter-btn", new com.microsoft.playwright.Page.LocatorOptions()
                        .setHasText("Completed"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item.completed")).hasCount(1);
    }

    @Test
    void filter_all_showsAllTasks() {
        addTask("Task A");
        addTask("Task B");
        page.locator(".task-checkbox").first().click();

        // Switch to Active then back to All
        page.locator(".filter-btn", new com.microsoft.playwright.Page.LocatorOptions()
                        .setHasText("Active"))
                .click();
        page.locator(".filter-btn", new com.microsoft.playwright.Page.LocatorOptions()
                        .setHasText("All"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 2);

        assertThat(page.locator(".task-item")).hasCount(2);
    }
}
