> [!IMPORTANT]
> **Project Status: In Development**
> This repository contains the final project for the "Java Web - May 2026" course, part of the "Spring Fundamentals" module at SoftUni.

---
# Book Shelf API

## Overview

This project is the **final project** for the university course:
**Java Web - May 2026 (Spring Fundamentals)** at **SoftUni**.

## Project Introduction

The **Book Shelf API** is a Java-based web application developed as a final project for the **Spring Fundamentals** course at **SoftUni**. It provides a centralized RESTful API for managing a personal book collection, including features for cataloging books, managing user libraries, and handling reviews. The system supports various user roles (e.g., User, Admin) with distinct access levels, ensuring secure management of the book data.

## Table of Contents
- [Architecture & Technologies](#architecture-and-technologies)
- [Installation & Setup](#installation-and-setup)
- [Implemented Features](#implemented-features)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)

## Architecture and Technologies
- **Backend**: Spring Boot 3.x / Java 21
- **Database**: PostgreSQL
- **Migrations**: Flyway
- **Containerization**: Docker & Docker Compose
- **Internal Communication**: Service-to-Service (where appropriate) to promote a Modular Monolith design.
- **Security**: Spring Security 6+
- **API Pattern**: RESTful with DTO/Entity separation, OpenAPI (Swagger) for documentation
- **Testing**: JUnit 5, Testcontainers, Mockito, AssertJ

## Implemented Features

The project is being developed using a strict **Domain-Driven Design (DDD)** approach, organized into vertical slices:

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

## Project Structure
The project follows a classic **Layered Architecture** (Package-by-Layer) to separate concerns based on technical function.

```
book-shelf/
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/bg/softuni/bookshelf/
│   │   │   ├── 📜 BookShelfApplication.java
│   │   │   ├── 📂 data/              # JPA Entities and Repositories
│   │   │   ├── 📂 service/            # Service layer (business logic)
│   │   │   └── 📂 shared/             # Cross-cutting concerns (Exceptions, Infrastructure)
│   │   └── 📂 resources/
│   │       ├── 📂 db/migration/       # Flyway SQL scripts
│   │       └── 📄 application.yaml    # Backend Configuration
│   │
│   └── 📂 test/                  # Test Suite
│       └── 📂 java/bg/softuni/bookshelf/
│           └── ...                # Unit and Integration tests per layer
│
├── 📄 build.gradle                # Backend build script
├── 📄 compose.yaml                # Docker Compose (Postgres setup)
└── 📄 README.md                   # Project documentation
```

## Installation and Setup

1. **Infrastructure**:
    ```bash
    docker compose up -d
    ```

2. **Run Backend**:
    *   Open the project in IntelliJ IDEA.
    *   Run the `BookShelfApplication.java` file.
    *   The backend will be available on `http://localhost:8080`.
    *   *Note: Flyway will automatically create the database schema on startup.*

3. **Run Tests**:
    *   To run the complete test suite, use the Gradle wrapper:
    ```bash
    ./gradlew test
    ```

## API Documentation
Once the application is running, the OpenAPI (Swagger UI) documentation is available at:

http://localhost:8080/swagger-ui.html
