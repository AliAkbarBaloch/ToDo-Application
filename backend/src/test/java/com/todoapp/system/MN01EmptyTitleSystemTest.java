package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for MN-01: Must Not Create a Task with an Empty Title.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Clicking Add Task with an empty title shows "Title is required" and creates no task.
 *   <li>Clicking Add Task with a whitespace-only title shows the same error.
 * </ul>
 */
class MN01EmptyTitleSystemTest extends SystemTestBase {

    @Test
    void emptyTitle_showsRequiredError_noTaskCreated() {
        page.locator("button[type='submit']").click();

        assertThat(page.locator(".field-error")).hasText("Title is required");
        assertThat(page.locator(".task-item")).hasCount(0);
    }

    @Test
    void whitespaceOnlyTitle_showsRequiredError_noTaskCreated() {
        page.locator("#new-title").fill("   ");
        page.locator("button[type='submit']").click();

        assertThat(page.locator(".field-error")).hasText("Title is required");
        assertThat(page.locator(".task-item")).hasCount(0);
    }
}
