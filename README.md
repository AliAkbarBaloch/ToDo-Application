# Todo Application

A full-stack task management application built with **Spring Boot** (backend) and **React / Vite** (frontend).

---

## Features

- Create, view, edit, and delete tasks
- Set priority levels (High / Medium / Low) — tasks auto-sorted by priority
- Set optional due dates with visual overdue / due-today indicators
- Mark tasks as completed and toggle back to active
- Filter tasks by status (All / Active / Completed)
- Search tasks by keyword (title + description, case-insensitive)
- Data persists across browser refreshes via server-side H2 database
- Input sanitisation — HTML/script content displayed as literal text (XSS-safe)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3, Spring Data JPA, H2 (file-based) |
| Frontend | React 18, Vite 5, JavaScript (ES2022) |
| Testing | JUnit 5 + Mockito, Playwright (E2E), Vitest + Testing Library |
| Quality | Checkstyle, Spotless (Google Java Format), ESLint, Prettier |
| CI/CD | GitHub Actions |

---

## Prerequisites

- Java 17 or later
- Maven 3.8+
- Node.js 20+

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/AliAkbarBaloch/ToDo-Application.git
cd ToDo-Application
```

### 2. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.
The H2 console is at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/tododb`).

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The app opens at `http://localhost:5173`. The Vite dev server proxies all `/api/*` requests to the backend — no CORS configuration needed.

---

## Running Tests

### Backend unit + integration tests

```bash
cd backend
mvn test
```

### Backend system (Playwright E2E) tests

```bash
# One-time: install Chromium
cd backend
mvn -q exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI \
    -Dexec.args="install chromium" -Dexec.classpathScope=test

# Build the frontend first
cd ../frontend && npm run build

# Run all 49 E2E tests
cd ../backend && mvn test -Psystem-tests
```

### Frontend tests

```bash
cd frontend
npm test                  # run once
npm run test:coverage     # with coverage report (threshold ≥ 80 %)
```

---

## API Reference

Base path: `/api/todos`

| Method | URL | Description |
|---|---|---|
| GET | `/api/todos` | All tasks (sorted by priority) |
| GET | `/api/todos?status=active\|completed` | Filtered list |
| GET | `/api/todos?search=keyword` | Full-text search |
| POST | `/api/todos` | Create task |
| PUT | `/api/todos/{id}` | Update task |
| PATCH | `/api/todos/{id}/status` | Toggle completed ↔ active |
| DELETE | `/api/todos/{id}` | Delete task |

---

## Project Structure

```
├── backend/                  Spring Boot application
│   ├── src/main/java/com/todoapp/
│   │   ├── controller/       REST endpoints + global exception handler
│   │   ├── service/          Business logic (priority sort, validation)
│   │   ├── repository/       Spring Data JPA repositories
│   │   ├── model/            JPA entity (Todo)
│   │   └── dto/              Request / Response DTOs
│   └── src/test/java/com/todoapp/
│       ├── service/          Unit tests (Mockito)
│       ├── controller/       Integration tests (@SpringBootTest)
│       └── system/           Playwright E2E tests
└── frontend/                 React + Vite application
    └── src/
        ├── App.jsx            Root component — owns all state and API calls
        └── components/        AddTodoForm, EditTodoForm, TodoItem, FilterBar
```

---

## License

MIT
