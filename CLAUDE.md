# CLAUDE.md — Architecture & Development Constraints

This file defines the architecture, technology stack, and coding conventions for the
ToDo application. All LLM-generated code **must** follow these constraints.

---

## Technology Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 17, Spring Boot 3.x (Maven)    |
| Database   | H2 (file-based, embedded)           |
| ORM        | Spring Data JPA / Hibernate         |
| Frontend   | React 18 + Vite                     |
| API        | RESTful JSON over HTTP              |

---

## Architecture: MVC + Repository Pattern

The backend follows the **Model-View-Controller (MVC)** pattern combined with the
**Repository Pattern** for data access:

```
Request → Controller → Service → Repository → Database
                ↓
            Response (JSON)
```

### Layers

- **Model** (`model/`): JPA entities — pure data classes, no business logic.
- **Repository** (`repository/`): Spring Data JPA interfaces — database access only.
- **Service** (`service/`): Business logic — validation, transformation, rules.
- **Controller** (`controller/`): REST endpoints — HTTP handling only, delegates to Service.

### Package Structure

```
com.todoapp
├── model/          ← JPA entities (Todo.java)
├── repository/     ← Spring Data JPA interfaces (TodoRepository.java)
├── service/        ← Business logic (TodoService.java)
├── controller/     ← REST controllers (TodoController.java)
└── dto/            ← Request/Response DTOs (TodoRequest.java, TodoResponse.java)
```

---

## API Design: RESTful Conventions

- Base path: `/api/todos`
- All responses: `application/json`
- Stateless requests — no server-side session state
- Resources identified by URLs; actions expressed via HTTP verbs

| Method | URL                     | Action                        | Success Code |
|--------|-------------------------|-------------------------------|--------------|
| GET    | `/api/todos`            | Return all todos              | 200          |
| GET    | `/api/todos/{id}`       | Return single todo            | 200          |
| POST   | `/api/todos`            | Create new todo               | 201          |
| PUT    | `/api/todos/{id}`       | Update existing todo          | 200          |
| PATCH  | `/api/todos/{id}/status`| Toggle completed/active       | 200          |
| DELETE | `/api/todos/{id}`       | Delete todo                   | 204          |

Query parameters: `?status=active|completed`, `?search=keyword`

Standard HTTP error codes: `400` (validation), `404` (not found), `409` (conflict)

---

## Database: H2 (File-Based)

- Use **file-based** H2 (not in-memory) so data survives server restarts.
- JPA DDL: `spring.jpa.hibernate.ddl-auto=update`
- H2 console enabled for development at `/h2-console`

---

## Frontend: React + Vite

- Vite dev server proxies `/api` requests to `http://localhost:8080`
- All API calls use the native `fetch` API
- Components in `src/components/`
- State management with React hooks (`useState`, `useEffect`)

---

## Coding Conventions

- **No business logic in Controllers** — delegate to Service layer
- **No database queries in Services** — delegate to Repository layer
- **Input validation** using `@Valid` + Bean Validation (`@NotBlank`, `@Size`, etc.)
- **DTOs** for request/response bodies — never expose JPA entities directly in API
- **HTML escaping**: always use `textContent` (not `innerHTML`) in React to prevent XSS

---

## Quality Attributes (from Architecture lecture)

| Attribute       | Approach                                                        |
|-----------------|-----------------------------------------------------------------|
| Simplicity      | MVC layers, each with a single responsibility                   |
| Usability       | RESTful API is self-documenting; React UI follows human-centred design |
| Reliability     | File-based H2 ensures data survives restarts; input validation prevents corrupt state |
| Maintainability | Repository pattern allows DB swap without touching business logic |
