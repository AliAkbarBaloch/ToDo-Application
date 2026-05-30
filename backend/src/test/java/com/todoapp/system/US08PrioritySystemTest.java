package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-08: Set a Priority Level on a Task.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>High priority tasks show a 'High' badge.
 *   <li>Tasks created without selecting priority default to 'Medium'.
 *   <li>Tasks are sorted High → Medium → Low in the list.
 * </ul>
 */
class US08PrioritySystemTest extends SystemTestBase {

    @Test
    void priority_highSelected_showsHighBadge() {
        addTask("Important task", "HIGH", null);

        assertThat(page.locator(".priority-badge.priority-HIGH").first()).isVisible();
        assertThat(page.locator(".priority-badge.priority-HIGH").first()).containsText("High");
    }

    @Test
    void priority_noSelection_defaultsMedium() {
        addTask("Default task");

        assertThat(page.locator(".priority-badge.priority-MEDIUM").first()).isVisible();
        assertThat(page.locator(".priority-badge.priority-MEDIUM").first()).containsText("Medium");
    }

    @Test
    void priority_sortOrder_highBeforeMediumBeforeLow() {
        addTask("Low task", "LOW", null);
        addTask("High task", "HIGH", null);
        addTask("Medium task", "MEDIUM", null);

        // The service sorts HIGH → MEDIUM → LOW regardless of creation order
        assertThat(page.locator(".task-item .task-title").nth(0)).hasText("High task");
        assertThat(page.locator(".task-item .task-title").nth(1)).hasText("Medium task");
        assertThat(page.locator(".task-item .task-title").nth(2)).hasText("Low task");
    }
}
