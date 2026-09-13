# Contributing

Thank you for contributing to Resource Planning.

## Prerequisites

The project requires:

* Java 25
* Maven Wrapper
* Docker
* Docker Compose

Additional infrastructure may be required depending on the feature being developed, including PostgreSQL, Kafka, Redis and Keycloak.

## Getting started

Clone the repository and initialize submodules:

```bash
git clone --recurse-submodules https://github.com/gersimuca/resource-planning.git

cd resource-planning

git submodule update --init --recursive
```

Verify Java:

```bash
java -version
```

The project currently targets Java 25.

## Build

Use the Maven Wrapper rather than a globally installed Maven version:

```bash
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

## Running locally

Start the required infrastructure using Docker Compose:

```bash
docker compose up -d
```

Then start the application:

```bash
./mvnw spring-boot:run
```

## Tests

Run all tests:

```bash
./mvnw test
```

Run the complete verification lifecycle:

```bash
./mvnw verify
```

Integration tests should use Testcontainers where the test requires external infrastructure.

## API testing

The repository contains `requests.http` with example API requests and smoke tests.

Use the REST client in IntelliJ IDEA or another compatible HTTP client to execute individual requests.

Environment-specific values are maintained separately from the request definitions.

## Database migrations

Database schema changes must be implemented using versioned migrations.

Never modify an existing migration that has already been applied to an environment.

Create a new migration instead:

```text
V<number>__<description>.sql
```

Example:

```text
V12__add_resource_capacity.sql
```

## Architecture decisions

Significant architectural decisions should be documented using an ADR under:

```text
design/adr/
```

An ADR should explain:

1. Context
2. Decision
3. Alternatives considered
4. Consequences

## Code style

Keep code consistent with the project's existing formatting and static-analysis configuration.

Before submitting a pull request:

```bash
./mvnw clean verify
```

## Pull requests

Pull requests should:

* Have a clear description
* Include tests for new behaviour
* Update documentation when required
* Include an ADR for significant architectural decisions
* Avoid unrelated changes
* Pass CI checks

## Commit messages

Use clear, descriptive commit messages.

Examples:

```text
feat: add customer search endpoint
fix: prevent duplicate resource allocation
test: add integration tests for planning service
docs: update Kafka architecture ADR
chore: update Spring Boot dependencies
```

## Dependency updates

Dependencies are managed through Renovate.

Avoid manually changing dependency versions unless there is a specific reason.

## Security

Do not commit:

* Passwords
* API keys
* Access tokens
* Private keys
* Production credentials
* Environment-specific secrets

Use the project's environment/configuration mechanisms instead.
