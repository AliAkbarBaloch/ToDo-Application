package com.todoapp.system;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * System tests for NFR-03: Input Sanitisation and XSS Prevention.
 *
 * <p>Acceptance criteria verified:
 *
 * <ul>
 *   <li>{@code <script>alert('xss')</script>} in title is shown as literal text — no alert fires.
 *   <li>{@code <b>bold</b>} in title is shown as plain text — not rendered as HTML bold.
 *   <li>No {@code <script>} child element appears inside the rendered title DOM node.
 * </ul>
 */
class NFR03XssPreventionSystemTest extends SystemTestBase {

    @Test
    void scriptTagInTitle_displayedAsLiteralText_noAlertFired() {
        String xssTitle = "<script>alert('xss')</script>";

        boolean[] alertFired = {false};
        page.onDialog(
                dialog -> {
                    alertFired[0] = true;
                    dialog.dismiss();
                });

        addTask(xssTitle);

        // Title must appear as visible plain text
        assertThat(page.locator(".task-item .task-title").first()).hasText(xssTitle);
        // No browser alert must have fired
        assertFalse(alertFired[0], "XSS alert must not execute");
    }

    @Test
    void htmlTagInTitle_displayedAsPlainText_notRenderedAsHtml() {
        String htmlTitle = "<b>bold</b>";
        addTask(htmlTitle);

        // Text content must equal the raw string
        assertThat(page.locator(".task-item .task-title").first()).hasText(htmlTitle);
        // No <b> element must exist inside the title node (React escapes by default)
        assertThat(page.locator(".task-title b")).hasCount(0);
    }

    @Test
    void htmlInDescription_displayedAsPlainText() {
        page.locator("#new-title").fill("Normal title");
        page.locator("#new-desc").fill("<img src=x onerror=alert(1)>");
        page.locator("button[type='submit']").click();
        page.waitForCondition(() -> page.locator(".task-item").count() > 0);

        // Description must show as literal text, no <img> element injected
        assertThat(page.locator(".task-description").first())
                .hasText("<img src=x onerror=alert(1)>");
        assertThat(page.locator(".task-item img")).hasCount(0);
    }
}
