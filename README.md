# Todo Application

A full-stack task management application built with Spring Boot and React, structured around a layered REST architecture — controller, service, and repository — with DTO-based API boundaries, Bean Validation, centralized exception handling, and file-based persistence. The project is tested at four levels (unit, integration, component, and browser-driven end-to-end) and gated by a CI pipeline that enforces formatting, linting, and coverage thresholds on every push.

## Engineering Highlights

- **Layered backend architecture** — controllers handle HTTP only, business rules (priority ordering, warning generation) live in the service layer, and Spring Data JPA repositories are the only layer that touches persistence.
- **DTO-based API boundary** — `TodoRequest`/`TodoResponse` are the only types that cross the HTTP boundary; the `Todo` JPA entity is never serialized directly, so persistence changes don't leak into the API contract.
- **Centralized exception handling** — a `@RestControllerAdvice` converts `MethodArgumentNotValidException` into a structured `{"errors": {field: message}}` 400 response and `EntityNotFoundException` into a 404, so every controller method stays free of try/catch blocks.
- **Multi-level test suite** — 36 backend JUnit tests (Mockito-based service unit tests + `@SpringBootTest`/MockMvc integration tests), 21 Vitest/Testing Library component tests, and 49 Playwright end-to-end tests covering every user story and several non-functional requirements.
- **Enforced code quality pipeline** — Spotless (Google Java Format) and Checkstyle run at Maven's `validate` phase, so formatting and lint violations block `compile` locally, not just in CI; ESLint and Prettier do the equivalent on the frontend via npm pre-hooks.
- **Coverage gates, not just reports** — JaCoCo fails `mvn verify` below 80% line coverage; Vitest's v8 provider fails the frontend `test:coverage` run below 80% lines/statements.
- **Mutation testing** — a Pitest profile (`com.todoapp.service`, `controller`, `dto`) targets a 70% mutation-kill threshold, checking that the test suite catches behavioral changes, not just line execution.
- **Performance budget verified by test, not assumption** — Playwright system tests assert `GET /api/todos` responds within 500ms with 100 seeded tasks, and full page loads stay under 2 seconds, including across 10 consecutive loads.

## Features

### Task Management
- Create, edit, delete, complete, and reopen tasks
- Optional description field (up to 2000 characters) alongside the required title (up to 200 characters)
- Deleting a task requires confirming a browser `window.confirm()` dialog before the `DELETE` request is sent

### Organization
- Priority levels (High / Medium / Low); the task list is always sorted High → Medium → Low, with ties broken by newest-first within each group
- Optional due dates, with overdue tasks and tasks due today visually distinguished from tasks due later
- Status filtering (All / Active / Completed) with live counts per filter, combinable with keyword search
- Case-insensitive keyword search across both title and description

### User Experience
- Client-side validation blocks empty or over-length titles before a request is sent; the same rule is re-enforced server-side via Bean Validation
- A due date in the past shows an inline warning (both when creating and editing a task) without blocking submission
- Loading, error, and two distinct empty states (no tasks yet vs. no search results) are rendered explicitly rather than left blank
- The task title input auto-focuses on load, and the add-task form clears itself after a successful submission
- Responsive layout: form fields stack to a single column and buttons expand to full width below a 600px viewport

## Architecture

```text
User / Browser
      |
      v
+-------------------+
|  React 18 + Vite  |
+-------------------+
      |
   REST / JSON
      |
      v
+---------------------------------------+
|            Spring Boot Backend        |
|                                       |
|  TodoController                       |
|       |                               |
|       +----> GlobalExceptionHandler   |
|       |                               |
|       v                               |
|  TodoService                          |
|       |                               |
|       v                               |
|  TodoRepository                       |
|  (Spring Data JPA)                    |
+--------------------|------------------+
                     |
                     v
              +--------------+
              | H2 Database  |
              | file-based   |
              +--------------+
```

**Frontend.** `App.jsx` owns all server state (`todos`, `filter`, `search`, `editingId`) and every `fetch` call. Child components (`AddTodoForm`, `EditTodoForm`, `TodoItem`, `FilterBar`) are presentational — they receive data and callback props and hold no server state of their own. In development, Vite's dev server proxies `/api/*` to `localhost:8080`, so no CORS configuration is needed; in production the built frontend is served as static resources from Spring Boot itself, and `@CrossOrigin` on the controller only permits the Vite dev origin.

**Backend.** Each layer has one job:

| Layer | Responsibility |
|---|---|
| `controller/TodoController` | Maps HTTP verbs to service calls; no business logic |
| `controller/GlobalExceptionHandler` | Converts validation and not-found exceptions into structured JSON error responses |
| `service/TodoService` | Business rules: priority/date sorting, past-due-date warning, search + status filtering |
| `repository/TodoRepository` | Spring Data JPA — declarative queries only, no logic |
| `model/Todo` | JPA entity; `@PrePersist`/`@PreUpdate` hooks manage `createdAt`/`updatedAt` |
| `dto/TodoRequest`, `dto/TodoResponse` | The only types that cross the HTTP boundary |

## Engineering Decisions

**DTOs instead of exposing the entity.** `TodoRequest` and `TodoResponse` are separate types from the `Todo` JPA entity. This keeps persistence details (`@Entity`, lifecycle callbacks) out of the API contract and lets the response shape change (e.g. adding the `warning` field) independently of the database schema.

**Priority sorting happens in the service layer, not SQL.** `TodoService` defines a `Comparator` (`HIGH → MEDIUM → LOW`, then newest-first) and applies it in memory after fetching from the repository, rather than an `ORDER BY` clause. This keeps ordering logic testable with plain Mockito unit tests, independent of the database.

**Two response factory methods.** `TodoResponse.from()` is used for every response except creation; `TodoResponse.fromWithWarning()` is used only by `createTodo()`, adding a `"warning": "due_date_in_past"` field to the `201` response when the supplied due date is already in the past. This keeps the past-date warning a create-time concern rather than a general response transformation.

**File-based H2 instead of in-memory.** The application database (`jdbc:h2:file:./data/tododb`) persists to disk, so task data survives an application restart — verified by a dedicated persistence system test. Integration and unit tests instead run against `jdbc:h2:mem:testdb`, so the production database file is never touched by the test suite.

**Delete confirmation is a UI-only safeguard.** The browser `window.confirm()` dialog before delete is a frontend affordance; the `DELETE /api/todos/{id}` endpoint itself performs no confirmation step and deletes on any valid request. This is a deliberate simplicity trade-off appropriate for a single-user local application, not a general access-control mechanism.

**Testing at four levels.** Service logic is covered by fast Mockito unit tests with no Spring context; HTTP + persistence wiring is covered by `@SpringBootTest`/MockMvc integration tests against real H2; individual React components are covered by Vitest/Testing Library; and full user flows (including all ten user stories and several misuse/non-functional cases) are covered by Playwright system tests against a running server. Each level catches a different class of regression at a different cost.

## Tech Stack

| Area | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5 (Web, Data JPA, Validation), Maven |
| Frontend | React 18.3, Vite 5.4, JavaScript (ES2022) |
| Persistence | H2 database, file-based (`jdbc:h2:file:./data/tododb`) |
| Testing | JUnit 5 + Mockito (unit), Spring Boot Test + MockMvc (integration), Vitest + React Testing Library (component), Playwright 1.44 (end-to-end), JaCoCo (coverage), Pitest (mutation testing) |
| Code Quality | Spotless (Google Java Format, AOSP style), Checkstyle 10.18.1, ESLint 10 (react-hooks plugin), Prettier 3 |
| CI/CD | GitHub Actions |

## Testing Strategy

### Backend Unit Tests — `service/TodoServiceTest.java` (16 tests)
Mockito-mocked `TodoRepository`, no Spring context. Covers priority/date sort ordering, the past-due-date warning rule, search + status-filter combination logic, and not-found handling for update/toggle/delete.

### Backend Integration Tests — `controller/TodoControllerIntegrationTest.java` (19 tests)
`@SpringBootTest` with `MockMvc` against a real in-memory H2 instance. Covers every HTTP endpoint, request validation (400 responses with field-level errors), not-found responses (404), XSS-payload round-tripping (the server stores and returns raw strings unchanged), and combined search + status-filter queries.

### Frontend Component Tests — Vitest + Testing Library (21 tests)
`AddTodoForm`, `EditTodoForm`, `TodoItem`, and `FilterBar` are tested in isolation with mocked props/callbacks — form validation, edit pre-fill behavior, checkbox/button interactions, and due-date label rendering.

### End-to-End Tests — Playwright system tests (49 tests)
Run against a full Spring Boot instance serving the production frontend build, driven through a real Chromium browser. Organized by user story (US-01 through US-10: create, view, complete, edit, delete, due dates, filter, priority, search, persistence across restart) plus misuse cases (empty title, delete confirmation, past-date warning) and non-functional requirements: page-load performance budgets (NFR-01), first-time usability without instructions (NFR-02), and XSS payloads rendered as literal text with no script execution (NFR-03).

```bash
# Backend unit + integration tests
cd backend && mvn test

# Backend system (Playwright) tests — one-time Chromium install, then build + run
mvn -q exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="install chromium" -Dexec.classpathScope=test
cd ../frontend && npm run build && cd ../backend
mvn test -Psystem-tests

# Frontend component tests
cd frontend && npm test
```

## Code Quality & CI

Backend formatting is enforced by Spotless (Google Java Format, AOSP style) and Java conventions by Checkstyle (naming, imports, method length, empty catch blocks, string-literal equality, and more); both are bound to Maven's `validate` phase, so they run automatically before `compile` on every `mvn` invocation, including `mvn spring-boot:run`. The frontend uses ESLint (with the `react-hooks` plugin enforcing the Rules of Hooks) and Prettier, wired into `predev`/`prebuild` npm hooks so they run before the dev server starts or a production build is produced.

```text
Push / Pull Request
        │
        ├── Backend job:  mvn -B verify
        │     → Spotless check → Checkstyle → compile → JUnit 5 + JaCoCo → 80% line-coverage gate
        │
        └── Frontend job: npm ci → eslint → prettier --check → vitest --coverage (80% gate) → vite build
```

Both jobs run in parallel on every push and pull request to `main`. Coverage thresholds are configured in `backend/pom.xml` (JaCoCo `check` goal, 80% `LINE` `COVEREDRATIO`) and `frontend/vite.config.js` (Vitest `coverage.thresholds`, 80% lines/statements).

## Security Notes

The frontend never uses `dangerouslySetInnerHTML`; all task titles and descriptions are rendered through JSX (`{todo.title}`), which React escapes by default. This means user-entered HTML or `<script>` content is rendered as literal text rather than executed — verified directly by both an integration test (server round-trips the raw string unchanged) and Playwright system tests (no `<script>`/`<b>`/`<img>` element is injected into the DOM, and no `alert()` dialog fires). The server performs no HTML escaping of its own; encoding on the backend as well would double-encode the already-escaped output, so this is intentionally left to the frontend rendering layer.

This is a single-user local development setup with no authentication layer: the H2 web console is enabled at `/h2-console`, and the delete endpoint has no server-side confirmation step (see [Engineering Decisions](#engineering-decisions)). Both are reasonable for a local/portfolio deployment but would need to be revisited — console disabled, auth added — before any multi-user or public deployment.

## API Reference

Base path: `/api/todos` — all responses `application/json`.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/todos` | All tasks, sorted by priority then newest-first |
| GET | `/api/todos?status=active\|completed` | Filtered by status |
| GET | `/api/todos?search=keyword` | Case-insensitive search across title + description |
| GET | `/api/todos/{id}` | Single task |
| POST | `/api/todos` | Create a task — `201`, includes `"warning": "due_date_in_past"` when applicable |
| PUT | `/api/todos/{id}` | Update title / description / priority / due date |
| PATCH | `/api/todos/{id}/status` | Toggle completed ↔ active |
| DELETE | `/api/todos/{id}` | Delete — `204 No Content` |

Validation failures return `400` with `{"errors": {"field": "message"}}`; a missing task ID returns `404`.

## Project Structure

```text
backend/src/main/java/com/todoapp/
├── controller/    REST endpoints + centralized exception handling
├── service/       Business logic: sorting, filtering, warning generation
├── repository/    Spring Data JPA — declarative queries only
├── model/         JPA entity (Todo)
└── dto/           Request/response contracts (TodoRequest, TodoResponse)

backend/src/test/java/com/todoapp/
├── service/       Unit tests (Mockito)
├── controller/     Integration tests (@SpringBootTest + MockMvc)
└── system/         Playwright end-to-end tests

frontend/src/
├── App.jsx             Owns all API/server state; passes data + callbacks down as props
├── components/         AddTodoForm, EditTodoForm, TodoItem, FilterBar — presentational only
└── test/                Vitest + Testing Library component tests
```

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 20+

### Clone
```bash
git clone https://github.com/AliAkbarBaloch/ToDo-Application.git
cd ToDo-Application
```

### Start the backend
```bash
cd backend
mvn spring-boot:run
```
The API runs at `http://localhost:8080`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/tododb`, username `sa`, no password).

### Start the frontend
```bash
cd frontend
npm install
npm run dev
```
The app runs at `http://localhost:5173`, proxying `/api/*` to the backend — no CORS setup required in development.

### Run tests
```bash
# Backend
cd backend && mvn test              # unit + integration
mvn verify                          # + JaCoCo coverage gate

# Frontend
cd frontend && npm test             # component tests
npm run test:coverage               # + coverage report
```

## Future Improvements

- Pagination or infinite scroll once the task list grows beyond what a single fetch reasonably returns
- Optimistic UI updates for toggle/delete, instead of waiting on the round-trip before updating local state
- User authentication and per-user task ownership, which would also motivate moving the delete-confirmation and access-control logic server-side
- Migrating from file-based H2 to a networked database (e.g. PostgreSQL) if the app moves beyond single-instance local use
