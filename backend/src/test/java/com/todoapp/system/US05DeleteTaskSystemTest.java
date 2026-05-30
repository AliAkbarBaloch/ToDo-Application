package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-05: Delete a Task.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Confirming deletion removes the task from the list immediately.
 *   <li>Cancelling the dialog leaves the task unchanged.
 * </ul>
 *
 * <p>Note: MN-02 (confirmation dialog required) is covered in MN02DeleteConfirmSystemTest.
 */
class US05DeleteTaskSystemTest extends SystemTestBase {

    @Test
    void deleteTask_confirmed_taskRemovedFromList() {
        addTask("Task to delete");
        assertThat(page.locator(".task-item")).hasCount(1);

        // Accept the window.confirm dialog automatically
        page.onDialog(dialog -> dialog.accept());
        page.locator(".btn-delete").first().click();

        assertThat(page.locator(".task-item")).hasCount(0);
    }

    @Test
    void deleteTask_cancelled_taskRemainsInList() {
        addTask("Task to keep");
        assertThat(page.locator(".task-item")).hasCount(1);

        // Dismiss the window.confirm dialog
        page.onDialog(dialog -> dialog.dismiss());
        page.locator(".btn-delete").first().click();

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item .task-title").first()).hasText("Task to keep");
    }
}
