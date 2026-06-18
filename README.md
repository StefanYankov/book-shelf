> [!IMPORTANT]
> **Project Status: In Development**
> This repository contains the final project for the "Java Web - May 2026" course, part of the "Spring Fundamentals" module at SoftUni.

---
# Book Shelf (Goodreads clone)

## Overview

This project is the **final project** for the university course:
**Java Web - May 2026 (Spring Fundamentals)** at **Software University**.

## Project Introduction

The **Book Shelf API** is a Java-based web application developed as a final project for the **Spring Fundamentals** course at **Software University**. It provides a centralized RESTful API for managing a personal book collection, including features for cataloging books, managing user libraries, and handling reviews. The system supports various user roles (e.g., User, Admin) with distinct access levels, ensuring secure management of the book data.

## Table of Contents
- [Architecture & Technologies](#architecture-and-technologies)
- [Installation & Setup](#installation-and-setup)
- [Implemented Features](#implemented-features)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [License](#license)
- [Acknowledgments](#acknowledgments)
- [Repository](#repository)

## Architecture and Technologies
- **Backend**: Spring Boot 3.4.0 / Java 21
- **Frontend**: Angular 22 (Standalone, Zoneless, Signals)
- **Database**: PostgreSQL 17
- **Migrations**: Flyway
- **Containerization**: Docker & Docker Compose
- **Security**: Spring Security 6+ with stateless JWT authentication.
- **API Pattern**: RESTful with DTO/Entity separation, OpenAPI (Swagger) for documentation.
- **Testing**:
    - **Backend**: JUnit 5, Testcontainers, Mockito, AssertJ
    - **Frontend**: Vitest, JSDOM

## Implemented Features

The project is being developed using a strict **Domain-Driven Design (DDD)** approach, organized into vertical slices:

### Backend Application Structure & Features

*   **System Foundation**:
    *   Centralized RFC 7807 `ProblemDetail` exception handling (`GlobalExceptionHandler`).
    *   JSR-380 input validation on all DTOs.
    *   Automated Flyway database migrations.
*   **Domain Model**:
    *   A complete JPA entity model with  relationships (`Book`, `Author`, `User`, etc.).
    *   `@Version` annotation on base entities for optimistic locking.
    *   `JOINED` inheritance strategy for the `User` hierarchy.
*   **Core Services**:
    *   Full CRUD services implemented for `Book`, `Language`, `Genre`, `Publisher`, and `Author`.
    *   Application-level, case-insensitive duplicate name validation for all relevant entities.
    *   Robust handling of `DataIntegrityViolationException` on delete operations.
*   **Book and Author Services**:
    *   Scalable, paginated queries with `JOIN FETCH` to prevent N+1 problems.
    *   Abstracted `ImageUploadService` for flexible integration with cloud storage providers.
    *   Service-to-service communication between `AuthorService` and `BookService` to retrieve an author's books.

### Frontend Application Structure & Features

The Angular frontend is built with a standalone component architecture and follows a modular, feature-driven structure based on user roles:

## Project Structure
The project follows a standard monorepo structure with a clear separation between the backend and frontend applications.

```
book-shelf/
├── 📂 .github/workflows/         # CI/CD Pipelines
│   ├── 📄 backend-ci.yaml
│   └── 📄 frontend-ci.yml
│
├── 📂 frontend/                  # Angular Application
│   ├── 📂 src/app/
│   │   ├── 📂 api/                # Auto-generated API client
│   │   ├── 📂 core/               # Core services, guards, interceptors
│   │   ├── 📂 features/           # Feature components (pages)
│   │   ├── 📂 layout/             # Layout components (shells)
│   │   └── 📂 shared/             # Reusable components, pipes, etc.
│   └── 📄 angular.json
│
├── 📂 src/                       # Spring Boot Application
│   ├── 📂 main/
│   │   ├── 📂 java/bg/softuni/bookshelf/
│   │   │   ├── 📜 BookShelfApplication.java
│   │   │   ├── 📂 config/             # Spring Security and App configuration
│   │   │   ├── 📂 data/               # JPA Entities and Repositories
│   │   │   ├── 📂 service/            # Service layer (business logic)
│   │   │   ├── 📂 shared/             # Cross-cutting concerns
│   │   │   └── 📂 web/                # Controllers and Exception Handling
│   │   └── 📂 resources/
│   │       ├── 📂 db/migration/       # Flyway SQL scripts
│   │       └── 📄 application.yaml
│   │
│   └── 📂 test/
│
├── 📄 build.gradle                # Backend build script
├── 📄 compose.yaml                # Docker Compose (Postgres setup)
└── 📄 README.md                   # Project documentation
```

## Installation and Setup

The application is designed to be run with Docker Compose for the database and local servers for the backend and frontend.

1. **Infrastructure**:
    ```bash
    docker compose up -d
    ```

2. **Run Backend**:
    *   Open the project in IntelliJ IDEA.
    *   Run the `BookShelfApplication.java` file.
    *   The backend will be available on `http://localhost:8080`.
    *   *Note: Flyway will automatically create the database schema on startup.*

3.  **Run the Frontend**:
    *   Navigate to the `frontend/` directory in a separate terminal.
    *   Run `npm install` to install dependencies.
    *   Run `npm start` (which is an alias for `ng serve`).
    *   The frontend will be available on `http://localhost:4200`.

4. **Run Backend Tests**:
    *   To run the complete test suite, use the Gradle wrapper:
    ```bash
    ./gradlew test
    ```

4. **Run Frontend Tests**:
     *   Navigate to the `frontend/` directory in a separate terminal.
    ```bash
    ng test
    ```

## API Documentation
Once the application is running, the OpenAPI (Swagger UI) documentation is available at:

http://localhost:8080/swagger-ui.html

---

## License

The project is licensed under the MIT License.

## Acknowledgments
- Developed as part of the [**Spring Fundamentals**](https://softuni.bg/trainings/5311/spring-fundamentals-may-2026) course / [**Java Web**](https://softuni.bg/modules/120/java-web-may-2026/1629) module at [**Software University**](https://softuni.bg/).
- Special thanks to the course instructor for creating the project requirements.

## Repository
GitHub Repository: [https://github.com/StefanYankov/book-shelf](https://github.com/StefanYankov/book-shelf)