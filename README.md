> [!IMPORTANT]
> **Project Status: In Development**
> This repository contains the final project for the "Java Web - May 2026" course, part of the "Spring Fundamentals" module at SoftUni.

---
# Book Shelf (Goodreads clone)

## Overview

This project is the **final project** for the university course:
**Java Web - May 2026** module at **Software University**.

## Project Introduction

The **Book Shelf API** is a Java-based web application developed as a final project for the **Java Web** module at **Software University**. It provides a centralized RESTful API for managing a personal book collection, including features for cataloging books, managing user libraries, and handling reviews. The system supports various user roles (e.g., User, Admin) with distinct access levels, ensuring secure management of the book data.

## Table of Contents
- [Architecture and Technologies](#architecture-and-technologies)
- [Installation and Setup](#installation-and-setup)
- [Implemented Features](#implemented-features)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Test Credentials](#test-credentials)
- [License](#license)
- [Acknowledgments](#acknowledgments)
- [Repository](#repository)

## Architecture and Technologies
- **Backend**: Spring Boot 3.4.0 / Java 21
- **Frontend**: Angular 22 (Standalone, Zoneless, Signals)
- **Database**: PostgreSQL 17
- **Migrations**: Flyway
- **Containerization**: Docker and Docker Compose
- **Security**: Spring Security 6+ with stateless JWT authentication and Servlet-based unauthenticated entry point control.
- **API Pattern**: RESTful with DTO/Entity separation, OpenAPI (Swagger) for documentation.
- **Testing**:
    - **Backend**: JUnit 5, Testcontainers, Mockito, AssertJ
    - **Frontend**: Vitest, JSDOM

## Implemented Features

The project is being developed using a strict **Domain-Driven Design (DDD)** approach, organized into vertical slices:

-   **Identity and Access Management (IAM)**:
    -   Full Spring Security integration with stateless JWT (JSON Web Token) generation and validation.
    -   Role-based access control (`USER`, `ADMIN`).
    -   Secure email verification and single-use password recovery tokens.
    -   Public self-registration endpoint for new users with proactive duplication checks.
    -   Declarative test harness via customized @WithMockApplicationUser annotations supporting role slicing in unit tests.
-   **Book Discovery**:
    -   Public-facing REST endpoints for searching and viewing books.
    -   A modern, Signal-based Angular UI for real-time book searching with debouncing.
    -   A dedicated, routed component for viewing the full details of a single book.
-   **Bookshelf Management**:
    -   Custom user-defined bookshelf entities with database-level pagination tracking.
    -   Dedicated controller endpoints for adding, listing, and removing items.

### Backend Application Structure and Features

-   **System Foundation**:
    -   Centralized RFC 7807 `ProblemDetail` exception handling (`GlobalExceptionHandler`).
    -   Standardized HTTP 401 Unauthorized response codes on unauthenticated filter attempts.
    -   JSR-380 input validation on all DTOs.
    -   Automated Flyway database migrations.
-   **Domain Model**:
    -   A complete JPA entity model with  relationships (`Book`, `Author`, `User`, etc.).
    -   `@Version` annotation on base entities for optimistic locking.
    -   `JOINED` inheritance strategy for the `User` hierarchy.
-   **Core Services**:
    -   Full CRUD services implemented for `Book`, `Language`, `Genre`, `Publisher`, and `Author`.
    -   Application-level, case-insensitive duplicate name validation for all relevant entities.
    -   Robust handling of `DataIntegrityViolationException` on delete operations.
-   **Book and Author Services**:
    -   Scalable, paginated queries with `JOIN FETCH` to prevent N+1 problems.
    -   Abstracted `ImageUploadService` for flexible integration with cloud storage providers.
    -   Service-to-service communication between `AuthorService` and `BookService` to retrieve an author's books.
-   **Administrative Services**:
    -   Decoupled web layer projections utilizing `UserSecurityDto` and `UserSecurityViewDto` to protect JPA boundaries.
    -   Method security authorization controls enforcing access bounds via explicit `@PreAuthorize("hasRole('ADMIN')")` declarations.
    -   Stateful user locking and unlocking operations writing history to an emergency persistent status track.
    -   Strict validation guards blocking self-lock attempts (`ErrorCode.SELF_LOCK_PREVENTION`) with immediate HTTP 403 responses.
    -   Administrative metadata override engines (`moderateBook`) implemented in the core catalog domain to curating titles and summaries.

### Frontend Application Structure and Features

The Angular frontend is built with a standalone component architecture and follows a modular, feature-driven structure based on user roles:

-   **Core Authentication & Security Guards:**
    -   `AuthService`: Manages JWT tokens, user login/logout, and token decoding.
    -   `AuthInterceptor`: Pure transport-layer wrapper that appends headers without introducing routing side effects.
    -   `LandingGuard`: Evaluates unauthenticated views and landing redirects for incoming public traffic.
    -   `AuthGuard`: Validates session states and enforces global password rotation routes by checking `pwd_chg_req` status.
    -   `UserGuard` / `AdminGuard`: Subsystem isolation gates that completely shield matching layout workspaces from conflicting roles.
-   **Public Zone (`features/public` and `features/auth`):**
    -   `PublicLayout`: Provides a shared public shell (Catalog browsing, Login, Registration) and footer for unauthenticated guest entry points.
    -   `Login`, `Register`, `ForgotPassword`, `ResetPassword`: User authentication and account management forms.
-   **Authenticated User Zone (`layout/app-layout`):**
    -   `AppLayout`: Dedicated workspace shell providing the core catalog interface and bookshelves for authenticated users.
    -   `AuthenticatedHeader`: Implements absolute routing targets directing users directly to the `/app/books` interface.
    -   `Profile`: Component managing user details and personal security updates using reactive agile validation rule trackers to enforce complexity parameters inline.
-   **Administrative Zone (`layout/admin-layout`):**
    -   `AdminLayout`: Isolated control panel shell entirely segregated from user layouts to provide system-level administration interfaces.
    -   `AdminHeader`: Dynamically restricts layout navigation paths when a forced credential rotation is active.
    -   `AdminHome`: A dedicated, lightweight dashboard landing station for the administrative root layout view.
    -   `UserList`: Deep-linked user management directory that updates route query parameters (`?page=X`) to support direct administrative link bookmarking.
    -   `ContentModeration`: Multi-tab interface featuring non-blocking reactive dialog structures to let administrators sanitize book summaries and fields.
    -   `AdminProfile`: Decoupled profile component using complexity rules to enforce administrative credential changes.
-   **Core Views & Components:**
    -   `BookList`: Integrates a contextual Bootstrap action dropdown iterating user storage signal collections with an `@for` loop block to streamline item grouping tasks.
    -   `BookDetail`: A dedicated page for viewing all metadata for a single book, with integrated "Add to Shelf" functionality.

## Project Structure
The project follows a standard monorepo structure with a clear separation between the backend and frontend applications.

```
book-shelf/
├── 📂 .github/workflows/               # CI/CD Pipelines
│   ├── 📄 backend-ci.yaml
│   └── 📄 frontend-ci.yml
│
├── 📂 frontend/                        # Angular Application
│   ├── 📂 src/app/
│   │   ├── 📂 api/                     # Auto-generated API client
│   │   ├── 📂 core/                    # Core services, guards, interceptors
│   │   ├── 📂 features/                # Feature components (pages)
│   │   ├── 📂 layout/                  # Layout components (shells)
│   │   └── 📂 shared/                  # Reusable components, pipes, etc.
│   └── 📄 angular.json
│
├── 📂 src/                             # Spring Boot Application
│   ├── 📂 main/
│   │   ├── 📂 java/bg/softuni/bookshelf/
│   │   │   ├── 📜 BookShelfApplication.java
│   │   │   ├── 📂 config/              # Spring Security and App configuration
│   │   │   ├── 📂 data/                # JPA Entities and Repositories
│   │   │   ├── 📂 service/             # Service layer (business logic)
│   │   │   ├── 📂 shared/              # Cross-cutting concerns
│   │   │   └── 📂 web/                 # Controllers and Exception Handling
│   │   └── 📂 resources/
│   │       ├── 📂 db/migration/        # Flyway SQL scripts
│   │       ├── 📄 application.yaml     # General fallback configuration
│   │       └── 📄 application-dev.yaml # Development profile parameters
│   │
│   └── 📂 test/
│
├── 📄 build.gradle                     # Backend build script
├── 📄 compose.yaml                     # Docker Compose (Postgres setup)
└── 📄 README.md                        # Project documentation
```

## Installation and Setup

The application is designed to be run with Docker Compose for the database and local servers for the backend and frontend.

1. **Infrastructure**:
    ```bash
    docker compose up -d
    ```

2. **Run Backend**:
    -   Open the project in IntelliJ IDEA.
    -   Run the `BookShelfApplication.java` file.
    -   The backend will be available on `http://localhost:8080`.
    -   -Note: Flyway will automatically create the database schema and seed development reference blocks using the application-dev profile parameters..-

3.  **Run the Frontend**:
    -   Navigate to the `frontend/` directory in a separate terminal.
    -   Run `npm install` to install dependencies.
    -   Run `npm start` (which is an alias for `ng serve`).
    -   The frontend will be available on `http://localhost:4200`.

4. **Run Backend Tests**:
    -   To run the complete test suite, use the Gradle wrapper:
    ```bash
    ./gradlew test
    ```

4. **Run Frontend Tests**:
     -   Navigate to the `frontend/` directory in a separate terminal.
    ```bash
    ng test
    ```

## **User Registration & Email Verification**

When registering a new user profile via the frontend application, the system generates a secure, one-time verification token. Since no physical mail server is configured locally, the verification details are printed directly to the **Backend Console Logs**.

### **1. Locate the Verification Link**

Upon submitting the registration form, watch your backend console for logs from the NoOpEmailServiceImpl block:

```text
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.auth.AuthenticationServiceImpl   : Email verification token generated for user [syankov2].
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : \==========================================================================
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : 📧 MOCK EMAIL DISPATCHED
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Type: EMAIL VERIFICATION
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : To: syankoff3@gmail.com
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Action Required: Please click the following link to activate your account.
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Link: http://localhost:4200/verify?token=dc08bf4b-700e-40a6-a186-e1bc3ea819fd
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : \==========================================================================
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.auth.AuthenticationServiceImpl   : Verification email sent to: syankoff3@gmail.com
```

### **2. Verify Your Account**

1. Copy the link provided in the console log (e.g., http://localhost:4200/verify?token=dc08bf4b...).
2. Paste this URL into your web browser.
3. The frontend application will capture the token parameters and make a request to /api/auth/verify-email to activate your account.

## API Documentation
Once the application is running, the OpenAPI (Swagger UI) documentation is available at:

http://localhost:8080/swagger-ui.html

http://localhost:8080/swagger-ui.html

## Test Credentials

The local configuration environment seeds the following testing user definitions automatically on application startup.

- **Admin**: `admin` / `admin`
> [!IMPORTANT]
> The admin user is required to change their password on first login.


- **Standard User 1**: `user1` / `password`
- **Standard User 2**: `user2` / `password`

> [!NOTE]
> The seeded development database contains static password hashes that may not align with runtime encoder salts. If authentication requests decline these criteria, use the **Password Reset** interface to assign a valid runtime hash sequence.
#### **Resolving Pre-seeded Login Failures:**

If you cannot authenticate using the default credentials, use the **Password Reset Flow** to sync the password with your runtime encoder salt:

1. Navigate to the **Forgot Password** page in the UI.
2. Enter the email address of the pre-seeded account you wish to access (e.g., admin@example.com or user1@example.com).
3. Open your backend console to locate the dispatched reset link:
   📧 MOCK EMAIL DISPATCHED
   Type: PASSWORD RESET
   Link: http://localhost:4200/reset-password?token=some-reset-token-uuid

4. Copy the link, paste it into your browser, and set a new password (e.g., password).
5. This saves a freshly hashed password using the current runtime salt configuration, allowing you to log in successfully.

## Admin Recovery CLI

In a production environment, if the primary administrator is locked out, a privileged user with SSH access to the server can perform an emergency password reset.

1.  **Access the Server**: Securely connect to the server where the application `.jar` is running.
2.  **Run the Command**: Execute the following command to force a password reset for the specified user.

    ```bash
    java -jar app.jar --spring.shell.command.script.enabled=true force-password-reset <username>
    ```

3.  **Retrieve Temporary Password**: The command will output a new, secure, one-time password to the console.

    ```text
    Password for user 'admin' has been reset to: aBcDeFg12345
    ```

4.  **Securely Transmit**: Securely provide this temporary password to the administrator. Upon their next login, they will be required to change it immediately.

---

## License

The project is licensed under the MIT License.

## Acknowledgments
- Developed as part of the [**Java Web**](https://softuni.bg/modules/120/java-web-may-2026/1629) module at [**Software University**](https://softuni.bg/).
- Special thanks to the course instructor for creating the project requirements.

## Repository
GitHub Repository: [https://github.com/StefanYankov/book-shelf](https://github.com/StefanYankov/book-shelf)
