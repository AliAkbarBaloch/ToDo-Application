package com.todoapp.system;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Abstract base for all Playwright-based system (E2E) tests.
 *
 * <p>Spring Boot starts on a random port; Playwright's Chromium is launched once per JVM (shared
 * across all subclasses via context caching). Each test gets a fresh {@link Page} and a clean
 * database. Run with: {@code mvn test -Dgroups=SystemTest}
 *
 * <p>Pre-requisite: build the frontend once before running system tests: {@code cd frontend && npm
 * run build}
 */
@Tag("SystemTest")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class SystemTestBase {

    // ── Playwright singletons (created once for the whole test run) ───────────
    static Playwright playwright;
    static Browser browser;

    // ── Per-test state ────────────────────────────────────────────────────────
    Page page;

    @LocalServerPort int port;

    @Autowired TodoRepository todoRepository;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void stopBrowser() {
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
        page = browser.newPage();
        page.navigate(baseUrl());
        // Wait until the app shell has loaded (header is always present)
        page.waitForSelector(".app-header");
    }

    @AfterEach
    void tearDown() {
        page.close();
    }

    // ── Helpers shared by every test ──────────────────────────────────────────

    String baseUrl() {
        return "http://localhost:" + port;
    }

    /** Type a title and click Add Task; waits for the item to appear in the list. */
    void addTask(String title) {
        int before = page.locator(".task-item").count();
        page.locator("#new-title").fill(title);
        page.locator("button[type='submit']").click();
        page.waitForCondition(() -> page.locator(".task-item").count() > before);
    }

    /** Add a task with all optional fields filled in. */
    void addTask(String title, String priority, String dueDate) {
        int before = page.locator(".task-item").count();
        page.locator("#new-title").fill(title);
        if (priority != null) page.locator("#new-priority").selectOption(priority);
        if (dueDate != null) page.locator("#new-due").fill(dueDate);
        page.locator("button[type='submit']").click();
        page.waitForCondition(() -> page.locator(".task-item").count() > before);
    }
}
