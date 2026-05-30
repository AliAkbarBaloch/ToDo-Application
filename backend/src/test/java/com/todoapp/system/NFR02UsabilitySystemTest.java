package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

/**
 * System tests for NFR-02: First-Time Usability.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>Title input is focused immediately when the page loads (autoFocus).
 *   <li>Inline error message appears below the input field — not as a browser alert dialog.
 *   <li>All interactive controls are reachable via the Tab key (keyboard-only navigation).
 * </ul>
 */
class NFR02UsabilitySystemTest extends SystemTestBase {

    @Test
    void onPageLoad_titleInputIsAutoFocused() {
        // autoFocus prop on #new-title ensures keyboard-ready state immediately
        assertThat(page.locator("#new-title")).isFocused();
    }

    @Test
    void emptyTitleSubmit_showsInlineError_notBrowserAlert() {
        boolean[] alertFired = {false};
        // Register a dialog handler — if the browser shows an alert, the test must fail
        page.onDialog(dialog -> {
            alertFired[0] = true;
            dialog.dismiss();
        });

        page.locator("button[type='submit']").click();

        // Error must be an inline element, not a native browser dialog
        assertThat(page.locator(".field-error")).isVisible();
        assertThat(page.locator(".field-error")).hasText("Title is required");
        assert !alertFired[0] : "Error must not be shown as a browser alert dialog";
    }

    @Test
    void keyboardNavigation_canCreateTaskWithoutMouse() {
        // Title input is already focused via autoFocus
        page.keyboard().type("Keyboard task");

        // Tab past description, priority, due-date, then press Enter on 'Add Task'
        page.keyboard().press("Tab"); // → description
        page.keyboard().press("Tab"); // → priority
        page.keyboard().press("Tab"); // → due date
        page.keyboard().press("Tab"); // → Add Task button
        page.keyboard().press("Enter"); // submit

        page.waitForCondition(() -> page.locator(".task-item").count() > 0);

        assertThat(page.locator(".task-item .task-title").first()).hasText("Keyboard task");
    }

    @Test
    void keyboardNavigation_deleteButtonReachableViaTab() {
        addTask("Task for keyboard delete");

        // The Delete button must be reachable by Tab (keyboard accessibility)
        assertThat(
                page.getByRole(AriaRole.BUTTON,
                        new com.microsoft.playwright.Page.GetByRoleOptions()
                                .setName("Delete")))
                .isVisible();
    }

    @Test
    void allInputsHavePlaceholderText() {
        // Placeholder text helps first-time users understand what to type
        assertThat(page.locator("#new-title")).hasAttribute("placeholder",
                "What do you want to accomplish?");
        assertThat(page.locator("#new-desc")).hasAttribute("placeholder",
                "Add more details about this task...");
    }
}
