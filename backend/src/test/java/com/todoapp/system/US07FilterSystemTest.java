package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
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

    /** Marks the task whose title matches {@code title} as completed via its checkbox. */
    private void completeTaskByTitle(String title) {
        page.locator(".task-item")
                .filter(new Locator.FilterOptions().setHasText(title))
                .locator(".task-checkbox")
                .click();
    }

    @Test
    void filter_activeOnly_showsOnlyActiveTasks() {
        addTask("Keep me active");
        addTask("Complete me");
        completeTaskByTitle("Complete me");

        page.locator(
                        ".filter-btn",
                        new com.microsoft.playwright.Page.LocatorOptions().setHasText("Active"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item.completed")).hasCount(0);
        assertThat(page.locator(".task-item .task-title").first()).hasText("Keep me active");
    }

    @Test
    void filter_completedOnly_showsOnlyCompletedTasks() {
        addTask("Keep me active");
        addTask("Complete me");
        completeTaskByTitle("Complete me");

        page.locator(
                        ".filter-btn",
                        new com.microsoft.playwright.Page.LocatorOptions().setHasText("Completed"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item.completed")).hasCount(1);
    }

    @Test
    void filter_all_showsAllTasks() {
        addTask("Task A");
        addTask("Task B");
        completeTaskByTitle("Task A");

        page.locator(
                        ".filter-btn",
                        new com.microsoft.playwright.Page.LocatorOptions().setHasText("Active"))
                .click();
        page.locator(
                        ".filter-btn",
                        new com.microsoft.playwright.Page.LocatorOptions().setHasText("All"))
                .click();
        page.waitForCondition(() -> page.locator(".task-item").count() == 2);

        assertThat(page.locator(".task-item")).hasCount(2);
    }
}
