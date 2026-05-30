package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-10: Persistent Server-Side Data Storage.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Tasks created in the browser are still present after a page reload (data not lost on
 *       refresh).
 *   <li>A task marked as completed retains its status after a reload.
 * </ul>
 *
 * <p>Note: The test uses the in-memory H2 database shared for the duration of the server
 * process. A full server-restart test requires a file-based database and is covered by the
 * H2 file configuration in production.
 */
class US10PersistenceSystemTest extends SystemTestBase {

    @Test
    void persistence_tasksStillPresentAfterPageReload() {
        addTask("Task One");
        addTask("Task Two");
        addTask("Task Three");

        page.navigate(baseUrl());
        page.waitForSelector(".task-item");

        assertThat(page.locator(".task-item")).hasCount(3);
    }

    @Test
    void persistence_completedStatusSurvivestPageReload() {
        addTask("Check persistence");
        page.locator(".task-checkbox").first().click();
        assertThat(page.locator(".task-item.completed")).hasCount(1);

        page.navigate(baseUrl());
        page.waitForSelector(".task-item");

        assertThat(page.locator(".task-item.completed")).hasCount(1);
    }
}
