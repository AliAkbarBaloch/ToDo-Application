package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for MN-02: Must Not Delete a Task Without User Confirmation.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Clicking Delete shows a confirmation dialog before any deletion.
 *   <li>Confirming the dialog removes the task.
 *   <li>Dismissing the dialog leaves the task untouched.
 * </ul>
 */
class MN02DeleteConfirmSystemTest extends SystemTestBase {

    @Test
    void deleteTask_dialogAppearsBeforeDeletion() {
        addTask("Task requiring confirmation");

        boolean[] dialogShown = {false};
        page.onDialog(
                dialog -> {
                    dialogShown[0] = true;
                    dialog.accept();
                });
        page.locator(".btn-delete").first().click();

        page.waitForCondition(() -> page.locator(".task-item").count() == 0);
        assert dialogShown[0] : "Confirmation dialog must appear before deletion";
    }

    @Test
    void deleteTask_dialogDismissed_taskNotDeleted() {
        addTask("Should survive cancel");

        page.onDialog(dialog -> dialog.dismiss());
        page.locator(".btn-delete").first().click();

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item .task-title").first())
                .hasText("Should survive cancel");
    }
}
