# Notes Vault API

A lightweight backend service for creating, viewing, and deleting notes.
Built with Java and Spring Boot, backed by PostgreSQL, and containerized
with Docker.

---

## System Architecture

The diagram below shows how the system is structured from the client
down through each layer to the database.

![System Architecture](docs/architecture.png)

### How it works

- The **Client** sends HTTP requests to the API
- The **Controller** receives the request and delegates to the service
- The **Service** contains the business logic and calls the repository
- The **Repository** handles all database operations via Spring Data JPA
- The **Model** defines what a Note looks like in the database
- **PostgreSQL** persists the data in the `notes_vault` database
