package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * System tests for US-06: Set a Due Date on a Task.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>A future due date is displayed on the task item.
 *   <li>A due date of today shows the 'due-today' CSS class (amber indicator).
 *   <li>An overdue active task shows the 'overdue' CSS class (red indicator).
 *   <li>A task without a due date is created successfully with no date shown.
 * </ul>
 */
class US06DueDateSystemTest extends SystemTestBase {

    @Test
    void dueDate_futureDate_displayedOnTaskItem() {
        String future = LocalDate.now().plusDays(7).toString();
        addTask("Future task", null, future);

        assertThat(page.locator(".due-date").first()).containsText("Due " + future);
    }

    @Test
    void dueDate_today_showsDueTodayClass() {
        String today = LocalDate.now().toString();
        addTask("Today task", null, today);

        assertThat(page.locator(".due-date.due-today")).hasCount(1);
    }

    @Test
    void dueDate_pastDate_activeTask_showsOverdueClass() {
        String past = LocalDate.now().minusDays(3).toString();
        // Accept the past-date confirm dialog from the API response (no UI warning blocks submit)
        addTask("Overdue task", null, past);

        assertThat(page.locator(".due-date.overdue")).hasCount(1);
    }

    @Test
    void dueDate_noDueDate_taskCreatedWithoutDateLabel() {
        addTask("No date task");

        assertThat(page.locator(".due-date")).hasCount(0);
    }
}
