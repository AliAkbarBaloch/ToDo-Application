package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-01: Create a New Todo Task.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Entering a valid title and clicking "Add Task" adds the task to the list as Active.
 *   <li>Submitting with an empty title shows "Title is required" and creates no task.
 *   <li>Title input enforces maxLength=200 (HTML guard for the 200-char limit).
 * </ul>
 */
class US01CreateTaskSystemTest extends SystemTestBase {

    @Test
    void createTask_validTitle_appearsInListAsActive() {
        page.locator("#new-title").fill("Buy groceries");
        page.locator("button[type='submit']").click();

        page.waitForCondition(() -> page.locator(".task-item").count() > 0);

        assertThat(page.locator(".task-item .task-title").first()).hasText("Buy groceries");
        assertThat(page.locator(".task-item").first()).not().hasClass("completed");
    }

    @Test
    void createTask_emptyTitle_showsRequiredError_noTaskCreated() {
        page.locator("button[type='submit']").click();

        assertThat(page.locator(".field-error")).hasText("Title is required");
        assertThat(page.locator(".task-item")).hasCount(0);
    }

    @Test
    void createTask_titleInput_enforcesMaxLength200() {
        // The HTML maxLength attribute is the UI-level guard for the 200-character limit;
        // the server-side @Size(max=200) provides the API-level guard (integration tests).
        assertThat(page.locator("#new-title")).hasAttribute("maxlength", "200");
    }
}
