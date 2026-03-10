# Notes Vault API

A RESTful API for creating, retrieving, updating, and deleting notes. Built with Java 21 and Spring Boot 4.0.3, backed by PostgreSQL, and fully containerized with Docker and Docker Compose.

This project was developed as a software engineering challenge, following an agile workflow with GitHub issues, feature branches, and pull requests.

![Architecture Diagram](docs/architecture.png)

---

## System Overview

The Notes Vault API follows a standard three-layer Spring Boot architecture:

- **Controller** — handles HTTP requests and responses
- **Service** — contains all business logic
- **Repository** — manages database access via Spring Data JPA

All errors are handled centrally by a `GlobalExceptionHandler` and return structured JSON responses with a status code, message, and timestamp.

---

## Tech Choices

| Technology | Reason |
|------------|--------|
| Java 21 | LTS release with modern language features |
| Spring Boot 4.0.3 | Industry standard for building production-grade REST APIs |
| Spring Data JPA | Eliminates boilerplate database code via repository pattern |
| PostgreSQL 16 | Robust relational database — chosen over H2 to reflect real-world usage |
| Docker & Docker Compose | Single-command setup, portable across any environment |
| Maven | Standard Java build tool with reliable dependency management |
| JUnit 5 + Mockito | Two-layer test coverage — API level and service level |
| Spring Boot Actuator | Production-ready health check endpoint out of the box |

---

## How to Run

### Option 1 - Docker Compose (Recommended)

Requires Docker. No local Java or PostgreSQL installation needed.
```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.
PostgreSQL will be available on host port `5433`.

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

### Running the Tests

Tests run against an H2 in-memory database and do not require PostgreSQL or Docker to be running.
```bash
mvn test
```

Tests are intentionally excluded from the Docker build since they run against an isolated H2 database and do not require the Docker stack to be running.

Expected output: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`

---

## API Usage

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
Response: `200 OK` — JSON array of all notes

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

- `content` is required and cannot be blank
- `content` cannot exceed 500 characters
- Violations return `400 Bad Request` with a descriptive message

---

## Assumptions and Tradeoffs

**No authentication** - the challenge spec did not require auth. In a
production system this API would be secured with JWT or OAuth2. This is
tracked as a future improvement in the backlog.

**No pagination** - `GET /notes` returns all notes. For a production system
with large datasets, pagination would be essential. This is tracked as a
future improvement.

**UUID primary keys** - chosen over auto-increment integers for better
portability, security, and alignment with distributed system patterns.

**PostgreSQL over H2 for development** - running a real database locally
rather than an in-memory one means the development environment more closely
reflects production. H2 is used only for tests.

**Spring Boot 4.0.3** - chose the latest major version to demonstrate awareness of the current ecosystem and willingness to work on the cutting edge, including navigating the modularized test infrastructure changes introduced in 4.x.

---

## Future Improvements

The following stories are planned and tracked on the project board:

- **Authentication and Authorization** - secure all endpoints with JWT
- **Search notes** - `GET /notes?query=` full text search
- **Pagination** - limit and offset support on `GET /notes`
- **Filter by date** - retrieve notes created within a date range
- **Structured logging** - JSON formatted logs for production observability
- **React frontend** - a simple UI for interacting with the API
- **CI/CD pipeline** - GitHub Actions for automated test and build on push

---

## Project Structure
```
notes-vault-api/
├── docs/
│   └── architecture.png
├── scripts/
│   └── setup-docker.sh
├── src/
│   ├── main/
│   │   ├── java/com/bluestaq/notesvault/
│   │   │   ├── controller/    # NoteController
│   │   │   ├── exception/     # GlobalExceptionHandler, NoteNotFoundException
│   │   │   ├── model/         # Note, ErrorResponse
│   │   │   ├── repository/    # NoteRepository
│   │   │   └── service/       # NoteService
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/bluestaq/notesvault/
│       │   ├── NoteControllerTest.java
│       │   └── NoteServiceTest.java
│       └── resources/
│           └── application.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```