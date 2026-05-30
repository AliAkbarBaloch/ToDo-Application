package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * System tests for US-09: Search Tasks by Keyword.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Typing a keyword shows only matching tasks.
 *   <li>Search is case-insensitive.
 *   <li>No matches shows "No tasks match your search."
 *   <li>Clearing the search restores the full list.
 * </ul>
 */
class US09SearchSystemTest extends SystemTestBase {

    @Test
    void search_keyword_showsOnlyMatchingTasks() {
        addTask("Buy milk");
        addTask("Walk the dog");

        page.locator("[aria-label='Search tasks']").fill("milk");
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item")).hasCount(1);
        assertThat(page.locator(".task-item .task-title").first()).hasText("Buy milk");
    }

    @Test
    void search_caseInsensitive_matchesRegardlessOfCase() {
        addTask("Buy milk");

        page.locator("[aria-label='Search tasks']").fill("MILK");
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        assertThat(page.locator(".task-item .task-title").first()).hasText("Buy milk");
    }

    @Test
    void search_noMatch_showsNoResultsMessage() {
        addTask("Buy milk");

        page.locator("[aria-label='Search tasks']").fill("xyz123");
        page.waitForCondition(() -> page.locator(".task-item").count() == 0);

        assertThat(page.locator(".empty-state p")).hasText("No tasks match your search.");
    }

    @Test
    void search_cleared_restoresFullList() {
        addTask("Buy milk");
        addTask("Walk the dog");

        page.locator("[aria-label='Search tasks']").fill("milk");
        page.waitForCondition(() -> page.locator(".task-item").count() == 1);

        page.locator("[aria-label='Search tasks']").fill("");
        page.waitForCondition(() -> page.locator(".task-item").count() == 2);

        assertThat(page.locator(".task-item")).hasCount(2);
    }
}
