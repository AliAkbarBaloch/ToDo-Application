package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

/**
 * System tests for US-04: Edit an Existing Task.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Clicking Edit opens an inline form pre-filled with current values.
 *   <li>Saving an updated title reflects the change immediately in the list.
 *   <li>Clicking Cancel closes the form without modifying the task.
 * </ul>
 */
class US04EditTaskSystemTest extends SystemTestBase {

    @Test
    void editTask_clickEdit_formPrefilledWithCurrentTitle() {
        addTask("Original title");

        page.locator(".btn-edit").first().click();
        page.waitForSelector(".edit-form");

        assertThat(page.locator(".edit-form input[type='text']").first())
                .hasValue("Original title");
    }

    @Test
    void editTask_saveUpdatedTitle_appearsInListImmediately() {
        addTask("Old title");

        page.locator(".btn-edit").first().click();
        page.waitForSelector(".edit-form");
        page.locator(".edit-form input[type='text']").first().fill("New title");
        page.locator(".btn-save").click();
        page.waitForSelector(".task-title");

        assertThat(page.locator(".task-item .task-title").first()).hasText("New title");
    }

    @Test
    void editTask_cancel_taskRemainsUnchanged() {
        addTask("Unchanged title");

        page.locator(".btn-edit").first().click();
        page.waitForSelector(".edit-form");
        page.locator(".edit-form input[type='text']").first().fill("Modified");
        page.getByRole(
                        AriaRole.BUTTON,
                        new com.microsoft.playwright.Page.GetByRoleOptions().setName("Cancel"))
                .click();

        assertThat(page.locator(".task-item .task-title").first()).hasText("Unchanged title");
    }
}
