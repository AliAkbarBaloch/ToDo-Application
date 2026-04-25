# Architecture Document — ToDo Application

## 1. Overview

The ToDo application is a full-stack web application with three distinct layers:
a browser-based React frontend, a Java Spring Boot REST API server, and an H2
file-based relational database — all running on the JVM.

```
┌─────────────────────────────────────────────────────┐
│                    Browser (Client)                  │
│              React 18 + Vite (Port 5173)             │
└───────────────────────┬─────────────────────────────┘
                        │  HTTP / JSON  (REST API)
                        ▼
┌─────────────────────────────────────────────────────┐
│               Spring Boot Server (JVM)               │
│                      Port 8080                       │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │  Controller  │→ │   Service    │→ │Repository │  │
│  │  (MVC Layer) │  │(Business     │  │(Data      │  │
│  │              │  │ Logic Layer) │  │ Access)   │  │
│  └──────────────┘  └──────────────┘  └─────┬─────┘  │
└────────────────────────────────────────────┼────────┘
                                             │  JPA / Hibernate
                                             ▼
┌─────────────────────────────────────────────────────┐
│                  H2 Database (File-Based)            │
│                  ./data/tododb.mv.db                 │
└─────────────────────────────────────────────────────┘
```

---

## 2. Architecture Patterns

### 2.1 Model-View-Controller (MVC)

The backend applies MVC to separate concerns:

| Component      | Responsibility                                                  | Package              |
|----------------|-----------------------------------------------------------------|----------------------|
| **Model**      | JPA entity representing a Todo item (data + mapping)           | `model/`             |
| **View**       | JSON responses returned to the React frontend                  | DTOs in `dto/`       |
| **Controller** | Receives HTTP requests, validates input, delegates to Service  | `controller/`        |
| **Service**    | Applies business rules, orchestrates data flow                 | `service/`           |

### 2.2 Repository Pattern

The `TodoRepository` interface (extending `JpaRepository`) acts as the middleman between
the Service layer and the H2 database.

**Benefits:**
- Business logic (Service) has no knowledge of SQL or JPA implementation details
- Repository can be mocked in unit tests — no real database required
- Swapping H2 for PostgreSQL requires only a dependency + config change, not code

### 2.3 RESTful API Design

The API follows REST principles:
- **Stateless**: each HTTP request contains all information needed to process it
- **Resource-based URLs**: `/api/todos`, `/api/todos/{id}`
- **Standard HTTP verbs**: GET, POST, PUT, PATCH, DELETE
- **Standard HTTP status codes**: 200, 201, 204, 400, 404

---

## 3. Technology Stack

| Concern         | Technology                  | Reason                                              |
|-----------------|-----------------------------|-----------------------------------------------------|
| Language        | Java 17                     | LTS release, full Spring Boot 3.x support           |
| Backend Framework | Spring Boot 3.x           | Auto-configuration, embedded Tomcat, starter POMs  |
| Database        | H2 (file-based)             | No external process needed; data persists on restart|
| ORM             | Spring Data JPA / Hibernate | Eliminates boilerplate SQL; Repository abstraction  |
| Build Tool      | Maven                       | Standard in Spring Boot ecosystem                   |
| Frontend        | React 18                    | Component model, large ecosystem                    |
| Frontend Build  | Vite                        | Fast dev server with HMR; proxy support             |
| API Format      | JSON                        | Universal browser support                           |

---

## 4. Data Model

```
Todo
├── id           : Long        (auto-generated primary key)
├── title        : String      (required, 1–200 chars)
├── description  : String      (optional)
├── completed    : Boolean     (default: false)
├── priority     : Enum        (LOW, MEDIUM, HIGH; default: MEDIUM)
├── dueDate      : LocalDate   (optional)
├── createdAt    : LocalDateTime (auto-set on creation)
└── updatedAt    : LocalDateTime (auto-set on update)
```

---

## 5. API Endpoints

| Method | Endpoint                     | Description               | Request Body      | Response         |
|--------|------------------------------|---------------------------|-------------------|------------------|
| GET    | `/api/todos`                 | Get all todos             | —                 | `List<TodoResponse>` |
| GET    | `/api/todos?status=active`   | Filter by status          | —                 | `List<TodoResponse>` |
| GET    | `/api/todos?search=keyword`  | Search todos              | —                 | `List<TodoResponse>` |
| GET    | `/api/todos/{id}`            | Get single todo           | —                 | `TodoResponse`   |
| POST   | `/api/todos`                 | Create todo               | `TodoRequest`     | `TodoResponse`   |
| PUT    | `/api/todos/{id}`            | Update todo               | `TodoRequest`     | `TodoResponse`   |
| PATCH  | `/api/todos/{id}/status`     | Toggle completed          | —                 | `TodoResponse`   |
| DELETE | `/api/todos/{id}`            | Delete todo               | —                 | 204 No Content   |

---

## 6. Project Structure

```
todo-application/
├── backend/                          ← Spring Boot (Maven)
│   ├── src/main/java/com/todoapp/
│   │   ├── TodoApplication.java      ← Entry point
│   │   ├── model/
│   │   │   └── Todo.java             ← JPA entity
│   │   ├── repository/
│   │   │   └── TodoRepository.java   ← Spring Data JPA interface
│   │   ├── service/
│   │   │   └── TodoService.java      ← Business logic
│   │   ├── controller/
│   │   │   └── TodoController.java   ← REST endpoints
│   │   └── dto/
│   │       ├── TodoRequest.java      ← Incoming request body
│   │       └── TodoResponse.java     ← Outgoing response body
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                         ← React + Vite
│   ├── src/
│   │   ├── components/
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
├── mockup/
│   └── index.html                    ← Static HTML mockup (mid-fidelity prototype)
├── ARCHITECTURE.md                   ← This file
└── CLAUDE.md                         ← LLM architecture constraints
```

---

## 7. Quality Attributes

| Attribute       | Design Decision                                                           |
|-----------------|---------------------------------------------------------------------------|
| **Simplicity**  | Each layer has exactly one responsibility (SRP)                          |
| **Usability**   | RESTful API is self-describing; React UI designed around user needs       |
| **Reliability** | File-based H2 persists across restarts; Bean Validation prevents bad data |
| **Maintainability** | Repository pattern decouples storage from logic; swap DB with config change |
