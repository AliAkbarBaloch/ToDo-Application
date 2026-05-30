package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * System tests for MN-03: Must Not Accept a Due Date in the Past Without Warning.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Selecting a past due date and blurring the field shows the inline warning.
 *   <li>Selecting today's date shows no warning.
 *   <li>Task can still be saved even when the past-date warning is visible.
 * </ul>
 */
class MN03PastDateSystemTest extends SystemTestBase {

    @Test
    void pastDate_afterBlur_showsInlineWarning() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        page.locator("#new-due").fill(yesterday);
        page.locator("#new-due").blur();

        assertThat(page.locator(".due-date-warning")).isVisible();
        assertThat(page.locator(".due-date-warning")).containsText("Due date is in the past");
    }

    @Test
    void todayDate_noWarningShown() {
        String today = LocalDate.now().toString();
        page.locator("#new-due").fill(today);
        page.locator("#new-due").blur();

        assertThat(page.locator(".due-date-warning")).not().isVisible();
    }

    @Test
    void pastDate_taskCanStillBeSaved() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        page.locator("#new-title").fill("Past due task");
        page.locator("#new-due").fill(yesterday);
        page.locator("#new-due").blur();
        assertThat(page.locator(".due-date-warning")).isVisible();

        page.locator("button[type='submit']").click();
        page.waitForCondition(() -> page.locator(".task-item").count() > 0);

        assertThat(page.locator(".task-item")).hasCount(1);
    }
}
