# Notes Vault API (NVA)

A RESTful API for creating, retrieving, updating, and deleting notes. Built with Java 21 and Spring Boot 4.0.3, backed by PostgreSQL, and fully containerized with Docker and Docker Compose.

This project was originally developed as a software engineering challenge posed by Bluestaq. The submission is for them, but the project staying alive and continuing is for my own personal gain.This project follows an agile workflow with GitHub issues, feature branches, and pull requests.

![Architecture Diagram](docs/architecture.png)

---

## System overview

The Notes Vault API follows a standard three-layer Spring Boot architecture:

- **Controller**: handles HTTP requests and responses
- **Service**: contains all business logic
- **Repository**: manages database access via Spring Data JPA

All errors are handled centrally by a `GlobalExceptionHandler` and return structured JSON responses with a status code, message, and timestamp.

The project exceeds the minimum requirements of the challenge by including a PUT endpoint for updating notes, input validation on all incoming requests, and a health check endpoint via Spring Boot Actuator. Two layers of automated testing are included, covering both the API level with MockMvc and the service level with Mockito.

---

## Tech choices

| Technology | Reason |
|------------|--------|
| Java 21 | Selected using the Spring Initializr as the middle ground between stability and modernity. Java 17 was the safer LTS option but Java 21 brought meaningful language improvements worth adopting. Good balance between avoiding game-breaking changes while still taking a more modern route. |
| Spring Boot 4.0.3 | Java and Spring pair naturally together. Started with 3.4.3 for familiar footing, then led a migration through 3.5.11 and finally to 4.0.3 to gain real code migration experience. The Spring Initializr made initial scaffolding straightforward and the ecosystem is one I have worked with across other projects. |
| PostgreSQL 16 | A solid, reliable, production-grade relational database that I have encountered across the industry and one of my personal favorites. Chosen over lighter embedded options because real-world applications use real databases. Spring Data JPA sits on top of it, eliminating boilerplate repository code while keeping full access to the database when needed. |
| Docker & Docker Compose | The leading way to containerize applications. The ability to package a service with all of its dependencies in an isolated environment and run it on almost any machine with minimal setup is true software engineering power. A custom Ubuntu setup script is also included for first-time Docker installations. |
| GitHub | Where all of my code work lives. Used here not just for version control but for the full agile workflow including a Kanban project board, feature branching, and pull requests to simulate a real team environment. |
| Maven | Pairs naturally with Java and Spring. The build lifecycle, dependency management, and test execution capabilities made it the clear choice over Gradle for this stack. |
| H2 Database | Used exclusively as the test database so the main PostgreSQL data is never touched during test runs. A good embedded option for initial setup and test isolation that keeps the test suite fast and self-contained. |
| Visual Studio Code | Free, lightweight IDE with a strong plugin ecosystem. Custom extensions for Java, Spring, and Docker made it a capable environment for this entire project. |
| Bash | Working from a Linux Ubuntu terminal, Bash was the natural choice for the Docker setup script. More intuitive than PowerShell from personal experience and the right tool for automating environment setup on Linux. |
| draw.io | Best tool I have found for creating systems diagrams quickly and cleanly. The ability to build diagrams programmatically made it especially useful for documenting the architecture. |
| curl | Chosen over Postman intentionally to gain deeper experience with raw HTTP requests from the command line. Having some Postman experience already, curl offered a different perspective on how HTTP requests actually work under the hood. |
| JUnit 5 + Mockito | Given the Java, Spring, and Maven stack, these were the natural testing tools. MockMvc handled API level tests and Mockito handled service level isolation. They felt like the right tools for the job and that ended up paying off with 18 passing tests across two test classes. |

---

## How to run the project

> Please make sure you are running all commands from the root of the project repository. If you are not already there, run `cd notes-vault-api` first.

### Option 1 - Docker Compose (Recommended)

Requires Docker. No local Java or PostgreSQL installation needed. If you are on Ubuntu and need to install Docker first, run `./scripts/setup-docker.sh`.
```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`. PostgreSQL runs on host port `5433`.

To stop the containers:
```bash
docker compose down
```

### Option 2 - Local Setup

Requires Java 21, Maven, and PostgreSQL running locally.

**1. Create the database:**
```bash
sudo -u postgres psql
```
```sql
CREATE DATABASE notes_vault;
CREATE USER notes_user WITH PASSWORD 'notes_password';
GRANT ALL PRIVILEGES ON DATABASE notes_vault TO notes_user;
\q
```

**2. Run the application:**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### How to run the tests

Tests run against an H2 in-memory database and are intentionally excluded from the Docker build. No PostgreSQL or Docker required.
```bash
mvn test
```

Expected output: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`

---

## API usage examples

### Base URL

```
http://localhost:8080
```

### Endpoints

#### Create a note

```bash
curl -s -X POST http://localhost:8080/notes \
  -H "Content-Type: application/json" \
  -d '{"content": "My first note!"}' | jq
```

Response: `201 Created`

```json
{
  "id": "e3d2f1a0-...",
  "content": "My first note!",
  "createdAt": "2026-03-09T21:00:00"
}
```

#### Get all notes

```bash
curl -s http://localhost:8080/notes | jq
```

Response: `200 OK`, returns a JSON array of all notes

#### Get a note by ID

```bash
curl -s http://localhost:8080/notes/{id} | jq
```

Response: `200 OK` or `404 Not Found`

#### Update a note

```bash
curl -s -X PUT http://localhost:8080/notes/{id} \
  -H "Content-Type: application/json" \
  -d '{"content": "Updated content!"}' | jq
```

Response: `200 OK` or `404 Not Found`

#### Delete a note

```bash
curl -s -X DELETE http://localhost:8080/notes/{id} -w "%{http_code}"
```

Response: `204 No Content` or `404 Not Found`

#### Health check

```bash
curl -s http://localhost:8080/actuator/health | jq
```

Response: `200 OK` with database connectivity status

### Validation

- `content is required and cannot be blank`
- `content cannot exceed 500 characters`
- Violations return `400 Bad Request` with a descriptive message

---

## Assumptions and Tradeoffs

Some technology assumptions are already covered in the Tech Choices section above. This section focuses on the decisions made during development given time constraints, my current technical abilities, and what made the most sense for the project.

**Mirroring the Bluestaq stack as closely as possible** - Based on what I know about the UDL team and the Fall 2024 Bluestaq code challenges, I made educated guesses about their stack. Java, REST APIs, Git, containerization, and cloud platforms were all confirmed requirements. I assumed GitLab for version control and story boarding since that is common in enterprise environments, but opted for GitHub because that is where I keep my projects and work most naturally. I assumed Docker and Docker Compose as the containerization layer since they are the industry standard. For the database I assumed PostgreSQL or something relational, which aligned with prior knowledge from the Fall 2024 challenges, so PostgreSQL was a natural fit. Although, I see MongoDB as a possibliity if you all went the non-relational route. 

**H2 for testing, PostgreSQL for everything else** - the initial tradeoff was using H2 as a lightweight in-memory database to get the project running quickly. Once the foundation was solid, H2 was scoped strictly to test isolation while PostgreSQL handled all development and production workloads. This keeps the test suite fast and self-contained while keeping the dev environment close to production.

**Spring Boot version migration** - started on 3.4.3 because it felt familiar and I wanted to get the project up and running without getting blocked on import issues introduced in 4.x. That was a deliberate tradeoff of comfort over cutting-edge. Once the core API was working, I migrated from 3.4.3 to 3.5.11 to 4.0.3 to correct for that initial decision and land on the latest major version. The migration exposed real modularization changes in Spring Boot 4.x that I had to work through, which was valuable experience.

**CRUD completeness over advanced features** - prioritized delivering a complete CRUD experience (POST, GET, PUT, DELETE) with input validation and a clean test suite. Spring Security with JWT and search/filter operations require more time and a deeper level of technical growth than this timeline allowed. These are tracked in the backlog and are things I am actively continuing to build out on my own time after submission because I want to develop these skills properly.

---

## Future improvements

The following stories are planned and tracked on the project board. Some of these were listed as optional enhancements in the original challenge spec (authentication, input validation, additional endpoints like update and search). Others are personal goals I am pursuing to grow this project well beyond the scope of the submission.

**Backlog stories remaining:**
- **Authentication and Authorization** - secure all endpoints with JWT
- **Search notes** - `GET /notes?query=` full text search
- **Pagination** - limit and offset support on `GET /notes`
- **Filter by date** - retrieve notes created within a date range
- **Structured logging** - JSON formatted logs for production observability
- **CI/CD pipeline** - GitHub Actions for automated test and build on push

**Expanding the project itself:**
- **React frontend** - a UI for interacting with the API, with the goal of integrating it into my personal website at [jakobwest.dev](https://jakobwest.dev) - Hope to push some big changes and polish my website soon. 
- **Additional database categories** - expand the data model beyond a single note type
- **Database diagrams and systems diagrams** - more visual documentation to complement the existing architecture diagram
- **Expanded test coverage** - additional edge cases, integration tests, and contract tests
- **Automation** - scripted workflows for setup, teardown, and environment management

**The bigger picture:**

This project does not exist in isolation. My long term goal is to connect three projects I am actively working on into one robust, full stack software engineering showcase: this Notes Vault API, my personal website, and the [DevOps Roadmap Hivebox project](https://devopsroadmap.io/projects/hivebox/) along with its capstone. The idea is to bring backend development, frontend integration, DevSecOps tooling, and cloud infrastructure together into something that reflects the full range of skills I am building. Each project feeds into the next, and the end result will be something I am genuinely proud of.

---

## Project Structure

```
notes-vault-api/
├── docs/                          # Architecture diagram
├── scripts/
│   └── setup-docker.sh            # Ubuntu Docker installation script
├── src/
│   ├── main/
│   │   ├── java/com/bluestaq/notesvault/
│   │   │   ├── controller/        # REST endpoints
│   │   │   ├── exception/         # Global exception handler and custom exceptions
│   │   │   ├── model/             # JPA entities and response models
│   │   │   ├── repository/        # Spring Data JPA interfaces
│   │   │   ├── service/           # Business logic
│   │   │   └── NotesVaultApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/bluestaq/notesvault/
│       │   ├── NoteControllerTest.java   # MockMvc integration tests
│       │   ├── NoteServiceTest.java      # Mockito unit tests
│       │   └── NotesVaultApplicationTests.java
│       └── resources/
│           └── application.properties   # H2 test database config
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```