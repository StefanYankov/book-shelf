# Spring Fundamentals – May 2026 Individual Project Assignment

## Overview
This is the Individual Project Assignment for the Spring Fundamentals Course @ SoftUni. The project is smaller in scope compared to the Spring Advanced course and focuses on the core concepts covered during the lectures. Your web application must meet the following technology stack and general requirements.

## Technology Stack
* **Java version**: 17 or higher
* **Spring Boot version**: 3.4.0
* **Build tool**: Maven or Gradle
* **Database**: MySQL, PostgreSQL, MariaDB, or Oracle (relational database)
* **Backend**: Spring framework (you may use any Spring module that supports your application logic)
* **Frontend**:
    * Option 1: Spring MVC + Thymeleaf
    * Option 2: Frontend framework (React, Angular, Vue.js) + Spring RESTful API
* **Source Control**: Git - GitHub, GitLab, or Bitbucket. Links to the public repository required when submitting your project for the exam.

**Note**: Failure to follow these requirements will result in DIRECT DISQUALIFICATION from the exam.

## Project Architecture
Your solution must consist of a single Spring Boot application:
* **Main application**: the core backend system
* **Important note**: You may include additional microservices if needed.
* If you choose to implement a standalone frontend using React, Angular, or Vue.js, your main application must expose a RESTful API and is not required to use Thymeleaf.
* Each Spring Boot application must run independently on its own port.

## General Requirements

### Entities, Services, Repositories, and Controllers
* The Main application must define at least 3 domain entities.
* Each entity must be part of at least 1 valid functionality.
* Every entity must be supported both by:
    * Exactly 1 JPA Repository
    * At least 1 Service class
* Define as many Controllers (REST/MVC) as needed.
* **Important note**: You may have entities with purely technical purposes (e.g. logging, roles, tokens, audit, verification), but they are not counted towards the required number of domain entities!

### Web Pages and Front-end Design
* You must define at least 6 complete web pages.
* At least 4 of them must be dynamic.
* Up to 1 page out of the 6 required may be static (purely informational).
* (If you want to use FE Framework + SPA, make sure to define at least 5 web components the user can interact with)
* Ensure a well-designed UI and good UX (optionally).

### Functionalities
* The Main application must define at least 4 valid domain functionalities.
* For the Main application, a valid domain functionality must:
    * Be triggered by a user (e.g., filling a form, clicking a button) from the Frontend
    * Invoke a backend endpoint (POST, PUT, or DELETE)
    * Implement all CRUD operations for at least one main entity.
    * Show a visible result to the user (e.g., confirmation message, page, UI update)
* **Important note**: Read-only features (e.g. viewing details) are allowed, but do not count as valid functionalities.
* Functionalities that operate only on the User entity (e.g. login, registration, profile update, role management) do not count toward the required number of functionalities.

### Security
* Session-based login (store user_id in session)
* Access control:
    * Guests – only register, login and guest pages
    * Logged users - the rest of the endpoints
* Role checks must be applied (e.g. only adventurers can capture quests, only quest masters can create new items/quests)

### Database
* Use Spring Data JPA for database access.
* Each entity must use a UUID as its unique identifier/primary key.
* Sensitive data (e.g., passwords) must be stored hashed.
* Define at least 1 entity relationship in the project.

### Data Validation and Error Handling
* Every form in the application must include proper server-side validation.
* If a user submits a form with invalid data, the form should be redisplayed with appropriate error messages shown in red next to the specific fields.
* You must also enforce all business constraints and throw custom exceptions when these rules are violated.

### Code Quality and Style
* No dead code (unused methods, variables, or classes)
* No unused imports
* All classes, methods, variables, and packages must follow Java naming conventions
* Write a README.md documentation listing the application tech stack, supported features, functionalities, and integrations with other systems/applications.

### Git Commits
* The application must include at least 10 valid commits in 3 different days.
* A valid commit must follow the Conventional Commits format: `<type>: description`
* Accepted commit types: `feat`, `fix`, `refactor`, `docs`, `chore`

## Restrictions
* It is strictly forbidden to:
    * Copy-paste any significant portion of code from the course project or other course materials.
    * Base your project on the course project skeleton or reuse its structure directly.
    * Reuse frontend/backend modules (with or without minor modifications).
* Plagiarized or reused code will result in DIRECT DISQUALIFICATION.

## Submission Deadline
* You must submit a GitHub link to your project before 15:59 on 23 June 2026.
* Projects not submitted before the deadline will NOT be evaluated.

## Assessment Criteria
* Architecture & MVC structure – [0 - 20]
* Functionality (basic CRUD) – [0 - 30]
* Data Validation and Error Handling – [0 - 20]
* Code Quality and Style – [0 - 10]
* Git Commits – [0 - 20]