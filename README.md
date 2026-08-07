> [!IMPORTANT]
> **Project Status: In Development**
> This repository contains the **final project** for the "Java Web - May 2026" module at **Software University**.

---
# Book Shelf (Goodreads clone)

## Overview

This project is the **final project** for the university course:
**Java Web - May 2026** module at **Software University**.

## Project Introduction

The **Book Shelf API** is a Java-based web application developed as a final project for the **Java Web** module at **Software University**. It provides a centralized RESTful API for managing a personal book collection, including features for cataloging books, managing user libraries, handling reviews, and tracking yearly reading challenges. The system supports various user roles (e.g., User, Admin) with distinct access levels, ensuring secure management of the book data. Roles are augmented by admin-managed permissions for delegating specific capabilities without granting full administrative rights. Reading challenges are provided by a dedicated REST microservice, consumed by the main application through a Feign client with JWT propagation.

## Table of Contents
- [Architecture and Technologies](#architecture-and-technologies)
- [Installation and Setup](#installation-and-setup)
- [Configuration Toggles](#configuration-toggles)
- [Implemented Features](#implemented-features)
- [Integrations](#integrations)
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
- **Security**: Spring Security 6+ with stateless JWT authentication and Servlet-based unauthenticated/forbidden entry point control.
- **Inter-service communication**: Spring Cloud OpenFeign - the main application consumes the reading challenge microservice via a Feign client, forwarding the caller's JWT and translating downstream errors into the application's standard problem response.
- **Scheduling and Caching**: Spring's scheduling (`@Scheduled`) runs periodic maintenance jobs; Spring's caching abstraction (`@Cacheable`/`@CacheEvict`) caches the book catalog with eviction on writes.
- **Aspect-Oriented Programming**: Spring AOP provides execution-time logging (`@LogExecutionTime`) and audit logging of sensitive operations (`@Audited`).
- **Image storage**: Cloudinary, behind an `ImageUploadService` abstraction. Cloudinary is used when enabled by configuration; otherwise a no-op implementation is used, so the application runs without a Cloudinary account.
- **API Pattern**: RESTful with DTO/Entity separation, OpenAPI (Swagger) for documentation.
- **Testing**:
    - **Backend**: JUnit 5, Testcontainers, Mockito, AssertJ, WireMock (Spring Cloud Contract)
    - **Frontend**: Vitest, JSDOM

## Implemented Features

The project is being developed using a strict **Domain-Driven Design (DDD)** approach, organized into vertical slices:

-   **Identity and Access Management (IAM)**:
    -   Full Spring Security integration with stateless JWT (JSON Web Token) generation and validation.
    -   Role-based access control (`USER`, `ADMIN`).
    -   Permissions layered on top of roles: an administrator can grant or revoke specific capabilities (e.g. `MODERATE_REVIEWS`, `MODERATE_BOOKS`) to standard users. Permissions are embedded in the JWT as authorities alongside the role, and every grant/revoke is written to an auditable account-status event track.
    -   Secure email verification and single-use password recovery tokens.
    -   Public self-registration endpoint for new users with proactive duplication checks.
    -   Declarative test harness via customized `@WithMockApplicationUser` annotations supporting role and permission slicing in unit tests.
-   **Book Discovery**:
    -   Public-facing REST endpoints for searching and viewing books.
    -   A Signal-based Angular UI for real-time book searching with debouncing.
    -   A dedicated, routed component for viewing the full details of a single book.
-   **Bookshelf Management**:
    -   Custom user-defined bookshelf entities with database-level pagination tracking.
    -   Dedicated controller endpoints for adding, listing, and removing items.
-   **Reviews**:
    -   Polymorphic review targets via a soft `(targetId, targetType)` association, allowing reviews on books today and authors/publishers later without schema churn.
    -   Full CRUD with ownership rules - a review may be edited only by its author and deleted by its author, an administrator, or a user holding the `MODERATE_REVIEWS` permission - enforced at the service layer.
    -   Paginated listing that resolves author names through a single batched lookup (avoids the N+1 query pattern).
    -   Composite uniqueness on `(user, target)` preventing duplicate reviews for the same target.
-   **Content Moderation**:
    -   Book moderation is available to administrators and to standard users granted the delegatable `MODERATE_BOOKS` permission - mirroring how review moderation is delegated via `MODERATE_REVIEWS`.
    -   Authorization is enforced per action at the API layer (`hasRole('ADMIN') or hasAuthority('MODERATE_BOOKS')`), so a delegated moderator is a first-class capability rather than a UI-only affordance.
    -   Administrators retain the full moderation surface (books and bookshelves); delegated moderators receive a books-only moderation page.
-   **Cover Images**:
    -   Book covers are uploaded through the `ImageUploadService` abstraction (Cloudinary when enabled, no-op otherwise).
    -   Removing a book deletes its cover image after the removal commits: `deleteBook` publishes an event, and an asynchronous `@TransactionalEventListener` bound to `AFTER_COMMIT` deletes the image only when the transaction succeeds. The remote delete is best-effort and does not affect the transaction.
-   **Reading Challenges** (backed by the reading challenge microservice):
    -   Users set a yearly reading goal and adjust their progress from the frontend; the main application proxies these operations to the microservice via a Feign client.
    -   Progress is set to an absolute value, and completion is recomputed on every change, so reducing progress below the goal un-completes a challenge - allowing users to correct mistakes.
    -   The caller's JWT is forwarded to the microservice, which independently validates it and resolves the user; downstream `404`/`409` responses are translated into the main application's standard problem response.

### Backend Application Structure and Features

-   **System Foundation**:
    -   Centralized RFC 7807 `ProblemDetail` exception handling (`GlobalExceptionHandler`).
    -   Consistent RFC 7807 `ProblemDetail` responses for both HTTP 401 (unauthenticated) and 403 (forbidden) failures raised in the security filter chain, matching the format emitted by the controller advice.
    -   JSR-380 input validation on all DTOs.
    -   Automated Flyway database migrations.
-   **Cross-cutting Concerns (AOP)**:
    -   `@LogExecutionTime` - an `@Around` aspect logging method duration, applied to catalog reads.
    -   `@Audited` - an `@Around` aspect recording the principal, operation, optional UUID target, and outcome for sensitive administrative and moderation operations; method arguments are not logged, to avoid recording sensitive data. This is distinct from the account-status event trail, which drives account state.
-   **Domain Model**:
    -   A complete JPA entity model with relationships (`Book`, `Author`, `User`, etc.).
    -   `@Version` annotation on base entities for optimistic locking.
    -   `JOINED` inheritance strategy for the `User` hierarchy.
    -   Soft, framework-agnostic references (`UUID`) for polymorphic associations such as review targets, keeping aggregates decoupled.
    -   Admin-managed `Set<Permission>` on standard users (`user_permissions` table via Flyway `V7`), mapped to Spring Security authorities at authentication time.
-   **Core Services**:
    -   Full CRUD services implemented for `Book`, `Language`, `Genre`, `Publisher`, and `Author`.
    -   Application-level, case-insensitive duplicate name validation for all relevant entities.
    -   Handling of `DataIntegrityViolationException` on delete operations.
-   **Book and Author Services**:
    -   Paginated queries using `JOIN FETCH` to prevent N+1 problems for mapped associations.
    -   Cross-aggregate lookups (e.g. resolving review author names) resolved via batched `findAllById` in the service layer rather than persistence-layer joins, keeping decoupled aggregates independent.
    -   `ImageUploadService` abstraction for cloud storage providers, with a Cloudinary implementation and a no-op fallback selected by configuration.
    -   Service-to-service communication between `AuthorService` and `BookService` to retrieve an author's books.
-   **Administrative Services**:
    -   Decoupled web layer projections utilizing `UserSecurityDto` and `UserSecurityViewDto` to protect JPA boundaries.
    -   Method security authorization controls enforcing access bounds via explicit `@PreAuthorize` declarations.
    -   Stateful user locking and unlocking operations writing history to a persistent account-status event track. Locks may be permanent or temporary (time-boxed), with expired temporary locks reconciled automatically.
    -   Strict validation guards blocking self-lock attempts (`ErrorCode.SELF_LOCK_PREVENTION`) with immediate HTTP 403 responses.
    -   Permission management API: dedicated endpoints to grant, revoke, and read a user's permissions (`POST` / `DELETE` / `GET /api/admin/users/{id}/permissions`), each admin-guarded and audited via account-status events.
    -   Content moderation endpoints (`/api/moderation`): book moderation delegatable via `MODERATE_BOOKS`, bookshelf moderation and deletion administrator-only, each gated per action so URL-level role rules do not pre-empt method security.
-   **Scheduling and Caching**:
    -   Book catalog reads (`getById`) are cached via Spring's caching abstraction and evicted on update, delete, and moderation, keeping cached data consistent with writes.
    -   A cron job purges expired verification and password-reset tokens nightly.
    -   A fixed-delay job reconciles expired temporary account locks into unlock events; temporary locks (with an expiry) are lifted automatically, while permanent locks persist until an explicit administrative unlock.
-   **Reading Challenge Integration**:
    -   `ReadingChallengeClient` - a Feign client mirroring the microservice's REST contract.
    -   `FeignClientConfig` - forwards the caller's `Authorization` header to the microservice and translates downstream HTTP errors into `BusinessException` so clients receive the standard problem response.
    -   `ReadingChallengeProxyService` and `ReadingChallengeController` expose `/api/challenges` (create, adjust progress, read) to the frontend.

### Frontend Application Structure and Features

The Angular frontend is built with a standalone component architecture and follows a modular, feature-driven structure based on user roles:

-   **Core Authentication and Security Guards:**
    -   `AuthService`: Manages JWT tokens, user login/logout, and token decoding; exposes reactive `authorities`, `isAdmin`, and `hasAuthority(...)` signals so the UI can gate actions on both roles and permissions.
    -   `AuthInterceptor`: Transport-layer wrapper that appends headers without introducing routing side effects.
    -   `LandingGuard`: Evaluates unauthenticated views and landing redirects for incoming public traffic.
    -   `AuthGuard`: Validates session states and enforces global password rotation routes by checking `pwd_chg_req` status.
    -   `UserGuard` / `AdminGuard`: Subsystem isolation gates that shield matching layout workspaces from conflicting roles.
    -   `permissionGuard(permission)`: Functional guard factory admitting only users holding a specific authority, enabling permission-delegated routes (e.g. a `MODERATE_BOOKS` holder reaching the book moderation page).
-   **Public Zone (`features/public` and `features/auth`):**
    -   `PublicLayout`: Shared public shell (Catalog browsing, Login, Registration) and footer for unauthenticated guest entry points.
    -   `Login`, `Register`, `ForgotPassword`, `ResetPassword`: User authentication and account management forms.
-   **Authenticated User Zone (`layout/app-layout`):**
    -   `AppLayout`: Workspace shell providing the catalog interface and bookshelves for authenticated users.
    -   `AuthenticatedHeader`: Navigation shell linking the catalog, bookshelves, reading challenge, and profile; a moderation link is shown only to users holding `MODERATE_BOOKS`.
    -   `Home`: Dashboard landing view with a reading challenge prompt card linking to the challenge page.
    -   `Profile`: Component managing user details and personal security updates using reactive validation to enforce complexity parameters inline.
    -   `ReadingChallenge`: Yearly reading challenge page (`/app/challenges`) with set-goal, progress, and completed states; progress is adjusted with +1 / -1 steppers that set the absolute books-read value.
    -   `BookModeration`: Books-only moderation page (`/app/moderation`) for `MODERATE_BOOKS` holders, guarded by `permissionGuard`.
-   **Administrative Zone (`layout/admin-layout`):**
    -   `AdminLayout`: Control panel shell segregated from user layouts to provide system-level administration interfaces.
    -   `AdminHeader`: Restricts layout navigation paths when a forced credential rotation is active.
    -   `AdminHome`: Dashboard landing view for the administrative root layout.
    -   `UserList`: Deep-linked user management directory that updates route query parameters (`?page=X`) to support direct administrative link bookmarking; supports lock/unlock (permanent or temporary) and on-demand permission management - a user's permissions are lazily loaded on request and granted/revoked through a reason-gated dialog.
    -   `ContentModeration`: Administrator content moderation for books and bookshelves.
    -   `AdminProfile`: Profile component using complexity rules to enforce administrative credential changes.
-   **Core Views and Components:**
    -   `BookList`: Contextual Bootstrap action dropdown iterating user storage signal collections with an `@for` loop block.
    -   `BookDetail`: Page for viewing all metadata for a single book, with integrated "Add to Shelf" functionality and an embedded review section (add, edit, delete) with a moderation-aware delete affordance.

## Integrations

- **Reading Challenge Microservice**: The main application delegates reading challenge operations to a separate Spring Boot REST microservice through a Feign client. The main application authenticates the user and forwards the JWT; the microservice independently validates the token and owns the reading challenge data in its own PostgreSQL database.
    - Microservice repository: https://github.com/StefanYankov/reading-challenge-svc
- **Cloudinary**: Book cover images are stored in Cloudinary when the integration is enabled. See [Configuration Toggles](#configuration-toggles).

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
│   │   │   ├── 📂 config/              # Spring Security, Feign, caching, scheduling, async, Cloudinary, and App configuration
│   │   │   ├── 📂 data/                # JPA Entities and Repositories
│   │   │   ├── 📂 service/             # Service layer (business logic, Feign client)
│   │   │   ├── 📂 shared/              # Cross-cutting concerns (including AOP aspects)
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

> [!NOTE]
> The reading challenge features require the reading challenge microservice to be running (default `http://localhost:8081`). See its repository for setup. When the microservice is not running, the main application returns an error for challenge operations but otherwise functions normally.

1. **Infrastructure**:
    ```bash
    docker compose up -d
    ```

2. **Run Backend**:
    -   Open the project in IntelliJ IDEA.
    -   Run the `BookShelfApplication.java` file.
    -   The backend will be available on `http://localhost:8080`.
    -   Note: Flyway will automatically create the database schema and seed development reference data using the application-dev profile parameters.

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

5. **Run Frontend Tests**:
    -   Navigate to the `frontend/` directory in a separate terminal.
    ```bash
    ng test
    ```

## Configuration Toggles

The application runs with sensible defaults and does not require any optional integration to be configured. The following toggles are provided for reviewers who want to activate optional or environment-specific behavior.

### Cloudinary image storage (optional)

Book cover upload uses Cloudinary only when explicitly enabled. When disabled (the default), a no-op image service returns a placeholder, so the application runs without a Cloudinary account and all tests pass.

To enable, provide the following environment variables (e.g. via the IntelliJ run configuration) and restart the backend:

| Variable | Required | Description |
|----------|----------|-------------|
| `CLOUDINARY_ENABLED` | Yes (to enable) | Set to `true` to activate Cloudinary; defaults to `false`. |
| `CLOUDINARY_CLOUD_NAME` | Yes (when enabled) | Cloudinary cloud name. |
| `CLOUDINARY_API_KEY` | Yes (when enabled) | Cloudinary API key. |
| `CLOUDINARY_API_SECRET` | Yes (when enabled) | Cloudinary API secret. |

Credentials are read only from environment variables and are never committed. With Cloudinary enabled, uploading a cover (via `POST /api/books` multipart) returns a Cloudinary URL, and deleting a book removes its cover image after the deletion commits.

### Scheduling (enabled by default; disabled under the test profile)

Scheduled maintenance jobs are controlled by `application.scheduling.enabled` (`true` in the base configuration, `false` under the `test` profile so jobs do not run during integration tests).

### Admin auto-seeding (development only)

Development-profile user seeding is controlled by `application.security.auto-seed-admin` (`true` in the `dev` profile, `false` in the base configuration). See [Test Credentials](#test-credentials).

## **User Registration and Email Verification**

When registering a new user profile via the frontend application, the system generates a secure, one-time verification token. Since no physical mail server is configured locally, the verification details are printed directly to the **Backend Console Logs**.

### **1. Locate the Verification Link**

Upon submitting the registration form, watch your backend console for logs from the NoOpEmailServiceImpl block:

```text
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.auth.AuthenticationServiceImpl   : Email verification token generated for user [syankov2].
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : ==========================================================================
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : 📧 MOCK EMAIL DISPATCHED
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Type: EMAIL VERIFICATION
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : To: syankoff3@gmail.com
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Action Required: Please click the following link to activate your account.
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : Link: http://localhost:4200/verify?token=dc08bf4b-700e-40a6-a186-e1bc3ea819fd
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.i.email.NoOpEmailServiceImpl     : ==========================================================================
12:49:48 INFO  --- [nio-8080-exec-7] b.s.b.s.auth.AuthenticationServiceImpl   : Verification email sent to: syankoff3@gmail.com
```

### **2. Verify Your Account**

1. Copy the link provided in the console log (e.g., http://localhost:4200/verify?token=dc08bf4b...).
2. Paste this URL into your web browser.
3. The frontend application will capture the token parameters and make a request to /api/auth/verify-email to activate your account.

## API Documentation
Once the application is running, the OpenAPI (Swagger UI) documentation is available at:

http://localhost:8080/swagger-ui.html

## Test Credentials

The development environment seeds the following accounts on startup. Their passwords are encoded at boot using the application's runtime `PasswordEncoder`, so the credentials below always work against a freshly seeded database.

| Username | Password | Role | Delegated permission |
|----------|----------|------|----------------------|
| `admin`  | `admin`  | ADMIN | - |
| `user1`  | `password` | USER | - |
| `user2`  | `password` | USER | - |
| `user3`  | `password` | USER | `MODERATE_REVIEWS` |
| `user4`  | `password` | USER | `MODERATE_BOOKS` |
| `user5`  | *(reset required)* | USER | - |

> [!IMPORTANT]
> The `admin` account is required to change its password on first login. Admin accounts do not use the self-service "forgot password" flow (which is restricted to standard users); if an administrator is locked out, use the Admin Recovery CLI described below, or reset the development database.

> [!NOTE]
> **Delegated permissions take effect on next login.** Authorities are embedded in the JWT at authentication time, so a permission granted to a currently logged-in user appears only after they log out and back in (a fresh token). `user3` and `user4` are pre-seeded with their permissions, so they demonstrate review and book moderation immediately after logging in.

> [!NOTE]
> **`user5` demonstrates the password-reset flow.** It is seeded with an unrecoverable password, so there are no known credentials - use the **Forgot Password** page with `user5@bookshelf.com`, then open the backend console for the dispatched reset link (see below) to set a working password.

#### Resolving Login via Password Reset

For any account without known credentials (e.g. `user5`), use the reset flow to assign a password hashed with the current runtime encoder:

1. Navigate to the **Forgot Password** page in the UI.
2. Enter the account's email (e.g. `user5@bookshelf.com`).
3. Open the backend console to locate the dispatched reset link:
   ```text
   📧 MOCK EMAIL DISPATCHED
   Type: PASSWORD RESET
   Link: http://localhost:4200/reset-password?token=some-reset-token-uuid
   ```
4. Copy the link, paste it into your browser, and set a new password.

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
