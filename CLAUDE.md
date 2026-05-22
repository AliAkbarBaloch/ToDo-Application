# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Build, Run & Test Commands

All backend commands must be run from the `backend/` directory. All frontend commands from `frontend/`.

### Backend (Spring Boot / Maven) — run from `backend/`

```bash
cd backend
mvn compile                                          # compile (auto-runs Spotless check + Checkstyle at validate phase)
mvn spotless:apply                                   # auto-format all Java files with Google Java Format (AOSP 4-space)
mvn spotless:check                                   # check formatting without writing
mvn checkstyle:check                                 # run Checkstyle linter only
mvn test                                             # run all tests with JaCoCo coverage report
mvn test -Dtest=ClassName                            # run a single test class
mvn test -Dtest=ClassName#methodName                 # run a single test method
mvn verify                                           # run tests + enforce ≥80% line coverage
mvn org.pitest:pitest-maven:mutationCoverage         # run mutation tests (target ≥70%)
mvn spring-boot:run                                  # start the server (auto-runs Spotless + Checkstyle first)
```

### Frontend (React / Vite) — run from `frontend/`

```bash
cd frontend
npm install            # install dependencies (first time only)
npm run dev            # start dev server on http://localhost:5173 (auto-runs ESLint first via predev hook)
npm test               # run all Vitest tests once
npm run test:coverage  # run tests with v8 coverage report (threshold ≥80%)
npm run lint           # ESLint — check for errors
npm run lint:fix       # ESLint — auto-fix fixable errors
npm run format         # Prettier — reformat all files in src/
npm run format:check   # Prettier — check formatting without writing
npm run build          # production build to dist/ (auto-runs lint + format:check first via prebuild hook)
```

### Running the full stack

Start backend first, then frontend. The Vite dev server proxies all `/api/*` requests to `http://localhost:8080`, so no CORS configuration is needed during development.

The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/tododb`).

---

## CI Pipeline (GitHub Actions)

Workflow file: `.github/workflows/ci.yml`
Triggers: every push and pull request to `main`.
Both jobs run in parallel on `ubuntu-latest`.

### Backend job — `mvn -B verify`

Single Maven command covers all CI requirements in sequence:

| Maven phase | What runs |
|---|---|
| `validate` | **Spotless** format check (Google Java Format AOSP) |
| `validate` | **Checkstyle** lint (naming, imports, method length, …) |
| `compile` | `javac` — fails on any compilation error |
| `test` | **JUnit 5** (35 tests) + JaCoCo coverage report |
| `verify` | **JaCoCo gate** — build fails if line coverage < 80 % |

### Frontend job — explicit steps

| Step | Command | Gate |
|---|---|---|
| Install | `npm ci` | exact lockfile install |
| Lint | `npm run lint` | ESLint 0 errors |
| Format check | `npm run format:check` | Prettier — all files clean |
| Test + coverage | `npm run test:coverage` | Vitest 21 tests; statements ≥ 80 %, lines ≥ 80 % |
| Build | `npm run build` | Vite production build succeeds |

> **Note:** `package-lock.json` must be kept up-to-date with `npm install --package-lock-only`
> when adding/upgrading packages, so `npm ci` on Linux runners includes all platform binaries.

---

## Linters & Formatters

### Backend

| Tool | Config file | Bound to |
|---|---|---|
| **Spotless** (Google Java Format 1.22.0, AOSP style) | `pom.xml` | `validate` phase — runs on every `mvn` command |
| **Checkstyle** 10.18.1 | `backend/checkstyle.xml` | `validate` phase — runs on every `mvn` command |

Fix formatting: `mvn spotless:apply`. Checkstyle violations must be fixed manually.

### Frontend

| Tool | Config file | Bound to |
|---|---|---|
| **ESLint** 10 (react-hooks plugin) | `frontend/eslint.config.js` | `predev` hook → auto-runs on `npm run dev` |
| **Prettier** 3 (singleQuote, no semi, tabWidth 2) | `frontend/.prettierrc` | `prebuild` hook → auto-runs on `npm run build` |

Fix formatting: `npm run format`. Fix lint: `npm run lint:fix`.

---

## Architecture: MVC + Repository Pattern

```
Browser (React/Vite :5173)
        │  fetch /api/*
        ▼
TodoController  →  TodoService  →  TodoRepository  →  H2 (file: backend/data/tododb.mv.db)
        │                 │
   GlobalExceptionHandler  │
   (400 / 404 responses)  DTO mapping (TodoRequest → Todo entity → TodoResponse)
```

### Backend layers (all under `backend/src/main/java/com/todoapp/`)

| Layer | File | Rule |
|---|---|---|
| Controller | `controller/TodoController.java` | HTTP only — no business logic |
| Exception handler | `controller/GlobalExceptionHandler.java` | Converts `MethodArgumentNotValidException` → 400, `EntityNotFoundException` → 404 |
| Service | `service/TodoService.java` | All business logic lives here, including priority sort |
| Repository | `repository/TodoRepository.java` | Spring Data JPA — no logic, only query methods |
| Entity | `model/Todo.java` | JPA entity; `@PrePersist`/`@PreUpdate` set timestamps |
| DTOs | `dto/TodoRequest.java`, `dto/TodoResponse.java` | Never expose the entity directly in API responses |

### Frontend (`frontend/src/`)

All API state lives in `App.jsx`. Child components receive data and callbacks as props — they own no server state.

| Component | Responsibility |
|---|---|
| `App.jsx` | Fetches todos; holds `todos`, `filter`, `search`, `editingId` state; all API call handlers |
| `components/AddTodoForm.jsx` | Create form; calls `onSubmit(data)` prop; title input is auto-focused on mount |
| `components/EditTodoForm.jsx` | Inline edit form pre-filled from `todo` prop; calls `onSave(data)` / `onCancel()` |
| `components/TodoItem.jsx` | Displays one task; calls `onToggle`, `onEdit`, `onDelete` props |
| `components/FilterBar.jsx` | Status filter buttons + search input; calls `onFilterChange` / `onSearchChange` |

---

## Testing Architecture

### Backend tests (`backend/src/test/java/com/todoapp/`)

| Class | Type | Tests | What it covers |
|---|---|---|---|
| `service/TodoServiceTest.java` | Unit (Mockito) | 16 | All `TodoService` methods with mocked repository |
| `controller/TodoControllerIntegrationTest.java` | Integration (`@SpringBootTest`) | 18 | All HTTP endpoints + XSS + validation against in-memory H2 |
| `TodoApplicationTests.java` | Smoke | 1 | Spring context loads |

Total: **35 tests**

Integration tests use `src/test/resources/application.properties` which configures an in-memory H2 (`jdbc:h2:mem:testdb`) — the production file-based database is never touched by tests.

`Todo.createdAt` has no public setter (`@PrePersist` sets it). Unit tests use reflection to set it when needed:
```java
Field f = Todo.class.getDeclaredField("createdAt");
f.setAccessible(true);
f.set(todo, LocalDateTime.now());
```

### Frontend tests (`frontend/src/test/`)

| File | Tests |
|---|---|
| `AddTodoForm.test.jsx` | 5 |
| `EditTodoForm.test.jsx` | 5 |
| `TodoItem.test.jsx` | 9 |
| `FilterBar.test.jsx` | 2 |

Total: **21 tests**

All tests use Vitest + `@testing-library/react`. Setup file is `src/test/setup.js` (imports `@testing-library/jest-dom`). Tests are co-located in `src/test/` with one file per component. `vi` globals are available without importing (configured in `vite.config.js`).

Coverage is generated by `@vitest/coverage-v8`. The `frontend/coverage/` directory is gitignored.

---

## Key Non-Obvious Implementation Details

**Priority sorting is done in the Service layer, not the database.**
`TodoService` defines a static `BY_PRIORITY_THEN_CREATED` comparator (HIGH → MEDIUM → LOW, then newest first) and applies it after every repository fetch in `getAllTodos()`. Do not add `ORDER BY priority` to JPQL queries.

**`TodoResponse` has two factory methods.**
`TodoResponse.from(todo)` is used everywhere except creation. `TodoResponse.fromWithWarning(todo)` is used only in `createTodo()` — it adds `"warning": "due_date_in_past"` to the HTTP 201 response when `dueDate < today`.

**Delete confirmation is a UI-only safeguard (`window.confirm`).**
`App.jsx` calls `window.confirm("Are you sure you want to delete "…"? This cannot be undone.")` before sending `DELETE /api/todos/{id}`. The API itself has no confirmation step — it deletes on any valid request.

**Filter + toggle interaction.**
When a status filter is active (`active` or `completed`) and the user toggles a task's status, `App.jsx` removes that task from the displayed list (it no longer matches the filter) instead of updating it in place.

**`@Valid` on controller + `GlobalExceptionHandler`.**
`TodoRequest` constraints: `title` — `@NotBlank` + `@Size(max=200)`; `description` — `@Size(max=2000)`. Spring throws `MethodArgumentNotValidException` on failure; `GlobalExceptionHandler` catches it and returns `{"errors": {"fieldName": "message"}}`.

**XSS prevention is handled entirely by React's JSX escaping.**
The server stores and returns raw strings unchanged — this is correct. React's JSX (`{todo.title}`) escapes all content by default; `dangerouslySetInnerHTML` is never used. Do not add server-side HTML escaping; it would double-encode content.

**ISO date strings are parsed as UTC by JavaScript.**
`new Date('YYYY-MM-DD')` is UTC midnight, not local midnight. `TodoItem.jsx`'s `getDueDateLabel()` compensates via `setHours(0,0,0,0)` on both sides. Frontend tests that assert on due-date labels must use regex matchers (e.g. `/Due Today/`) because the rendered span includes an emoji prefix (`📅`).

---

## API Reference

Base path: `/api/todos` — all responses `application/json`.

| Method | URL | Action | Success |
|---|---|---|---|
| GET | `/api/todos` | All todos (sorted by priority then date) | 200 |
| GET | `/api/todos?status=active\|completed` | Filtered list | 200 |
| GET | `/api/todos?search=keyword` | Full-text search on title + description | 200 |
| GET | `/api/todos/{id}` | Single todo | 200 |
| POST | `/api/todos` | Create todo | 201 (may include `warning` field) |
| PUT | `/api/todos/{id}` | Update title/description/priority/dueDate | 200 |
| PATCH | `/api/todos/{id}/status` | Toggle completed ↔ active | 200 |
| DELETE | `/api/todos/{id}` | Delete | 204 |

Error codes: `400` validation (with `{"errors": {...}}` body), `404` not found.

---

## Coding Constraints

- **No business logic in Controllers** — delegate to Service.
- **No queries in Services** — delegate to Repository.
- **DTOs only in API** — never pass `Todo` entity to/from the controller.
- **`TodoResponse.from()`** for all responses except `createTodo`, which uses `fromWithWarning()`.
- **Frontend XSS**: React escapes content by default — never use `dangerouslySetInnerHTML`.
- **H2 file** (`backend/data/tododb.mv.db`) must not be deleted — it is the persistent store.
