# Java Build Automaton 🚀

![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen.svg?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin-blue.svg?style=for-the-badge&logo=gradle)
![GitHub Actions](https://img.shields.io/badge/GitHub-Webhook-black?style=for-the-badge&logo=github)

A simple, lightweight CI/CD server written in Spring Boot that automates the process of building and testing projects after every `push` to a GitHub repository.

## 🎯 Project Description

The goal of this project is to build a miniature Continuous Integration server from scratch to explore the fundamental principles of DevOps tools. The application listens for `push` events sent by GitHub webhooks, clones the repository if needed and then updates it via `git pull`, runs the build process (e.g., using Gradle/Maven), and reports on the outcome.

This is not just another CRUD application—it's a practical tool that solves a real-world automation problem in the software development lifecycle.

---

## ✨ Key Features

* **Webhook Reception:** Listens for GitHub `push` events via Webhooks.
* **Dynamic Process Execution:** Runs system commands (`git`, `gradle`, `mvn`) using `ProcessBuilder`.
* **Workspace Isolation:** Each project builds in its own working directory.
* **Asynchronous Build Queue:** Enqueue builds and run them concurrently on virtual threads; limits via `build.max-parallel` and `build.queue.capacity`.
* **Persisted Build History & UI:** Build results and logs are stored in PostgreSQL and visible in the Dashboard, Project, and Build Details pages.
* **Logging:** Captures process output and stores it with the build (not streamed in real time yet).
* **Robust Error Handling:** Detects failures via exit codes and exceptions.

---

## 🛠️ Tech Stack

* Backend: Java 25, Spring Boot 3.5.6
* Build Tool: Gradle (Kotlin DSL)
* API: Spring Web (REST), Spring Boot Actuator
* Frontend: Thymeleaf (dashboard and project details)
* Data: Spring Data JPA; PostgreSQL with Flyway migrations
* Integration: GitHub Webhooks with HMAC SHA-256 signature validation
* Observability: CorrelationId filter, structured logging (logstash encoder)
* Containerization: Docker (multi-stage Dockerfile)

---

## 🚀 Getting Started

To run the project locally, follow the steps below.

### Prerequisites

* Java JDK 25 or newer
* Git
* [Ngrok](https://ngrok.com/) (for testing webhooks locally)
* A configured test repository on GitHub

### Installation and Launch

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YourUsername/java-build-automaton.git](https://github.com/YourUsername/java-build-automaton.git)
    cd java-build-automaton
    ```

2.  **Build the project using Gradle:**
    ```bash
    ./gradlew build
    ```

3.  **Run the Spring Boot application:**
    ```bash
    # Ensure DB env variables are set (see Configuration)
    export JDBC_CONNECTION_STRING=jdbc:postgresql://localhost:5432/jba
    export DB_USERNAME=jba
    export DB_PASSWORD=jba

    ./gradlew bootRun
    ```
    The application will start on port `8080`. Open http://localhost:8080/ to view the dashboard.

4.  **Expose your local server with Ngrok:**
    ```bash
    ngrok http 8080
    ```
    Copy the public URL provided by Ngrok (e.g., `https://xxxx-xx-xxx.ngrok.io`).

5.  **Configure the Webhook on GitHub:**
    * In your test repository, go to `Settings` > `Webhooks` > `Add webhook`.
    * **Payload URL:** Paste the URL from Ngrok, adding `/webhook` at the end.
    * **Content type:** Change to `application/json`.
    * **Secret:** Set a strong secret. Use the same value in the app via `webhook.webhook-secret` (or env `WEBHOOK_WEBHOOK_SECRET`).
    * Save the webhook.

6.  **Push a commit to your test repository and check the application logs; build output is captured and stored.**

---

## 🗺️ Roadmap

This project is actively under development. Here are the planned features:

See the full, living plan in [ROADMAP.md](./ROADMAP.md).

-   [ ] **Configuration/Admin UI:** Manage projects via an admin UI (create/update/delete).
-   [x] **Build History Module:** Persist every build (status, logs, commit hash) to the database.
-   [x] **Basic Web UI:** Dashboard, project details, and build details pages.
-   [ ] **Notification System:** Send email or Discord/Slack notifications about the build outcome.
-   [ ] **Docker Support:** Run builds in isolated Docker containers.

---

## ⚙️ Configuration

Database (PostgreSQL + Flyway):
- The app uses PostgreSQL. Flyway runs on startup to create/update the schema.
- Configure via environment variables (or Spring properties):
  - JDBC_CONNECTION_STRING or SPRING_DATASOURCE_URL (e.g., `jdbc:postgresql://localhost:5432/jba`)
  - DB_USERNAME or SPRING_DATASOURCE_USERNAME
  - DB_PASSWORD or SPRING_DATASOURCE_PASSWORD
- Example (local Postgres via Docker):
  ```bash
  docker run -d --name jba-db -p 5432:5432 \
    -e POSTGRES_USER=jba -e POSTGRES_PASSWORD=jba -e POSTGRES_DB=jba postgres:16

  export JDBC_CONNECTION_STRING=jdbc:postgresql://localhost:5432/jba
  export DB_USERNAME=jba
  export DB_PASSWORD=jba
  ```

Workspace:
- Set the base directory for local workspaces (where repositories are cloned and built):
  - Property: `workspace.base-dir`
  - Environment variable: `WORKSPACE_BASE_DIR`
- The application creates a subdirectory per repository under this base directory.

Projects:
- Preferred: Create projects via the Web UI at `http://localhost:8080/projects/create`:
  - Repository URL: `https://github.com/{user}/{repo}.git`
  - Build Tool: `GRADLE` or `MAVEN`
  The app derives the repo owner/name automatically and manages a workspace under the configured base directory.

- Or via REST:
  - POST `/api/projects/create` with JSON:
    `{ "repositoryUrl": "https://github.com/{user}/{repo}.git", "buildTool": "GRADLE|MAVEN" }`

- Advanced: Manual SQL insertion is no longer required for normal usage.

Build Queue and Concurrency:
- Builds are enqueued and executed asynchronously on virtual threads.
- Configure limits in application properties or env vars:
  - build.max-parallel (default 3) — max concurrent builds
  - build.queue.capacity (default 100) — queue size

Important:
- The build process uses the system `gradle`/`mvn` command (not the wrapper). Ensure they are installed and on your PATH. Alternatively, adapt `BuildExecutor` to prefer `./gradlew`/`./mvnw` if present.
- Webhook signature validation is implemented (HMAC SHA-256 via X-Hub-Signature-256). See Security notes and examples below.

---

## 🧪 Local Testing Without GitHub

You can simulate a GitHub webhook locally with curl:

```bash
curl -X POST http://localhost:8080/webhook \
  -H 'Content-Type: application/json' \
  -d '{
        "repository": { "full_name": "TomaszZiola/test" }
      }'
```

Expected response format:

```json
{
  "status": "FOUND | NOT_FOUND",
  "message": "..."
}
```

- status="FOUND" means the project was found and a build was started.
- status="NOT_FOUND" means the repository name didn’t match any project in the database.

---

## 🔐 Webhook Signature Validation

When a GitHub webhook secret is configured, all POST `/webhook` requests must include the `X-Hub-Signature-256` header. The value must be `sha256=<hex>` where `<hex>` is the lowercase HMAC SHA-256 of the raw request body using the shared secret. Validation is enforced by `WebhookSignatureFilter`.

Configuration properties:
- `webhook.webhook-secret`: the shared secret value
- `webhook.allow-missing-secret`: set to `true` in dev to skip validation when no secret is provided (default `false` in prod)

Example: generating the signature with OpenSSL and sending the request

```bash
BODY='{"repository": {"full_name": "owner/repo"}}'
SECRET='your-secret'
SIG=$(printf "%s" "$BODY" | openssl dgst -sha256 -hmac "$SECRET" -binary | xxd -p -c 256)

curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -H "X-Hub-Signature-256: sha256:${SIG}" \
  -d "$BODY"
```

Notes:
- In development, if you do not set a secret and `webhook.allow-missing-secret=true`, the header is not required.
- On GitHub, set the webhook "Secret" to the same value as `webhook.webhook-secret`.

---

## 🖥️ Web UI

- GET `/` — Dashboard listing all projects with basic info.
- GET `/projects/create` — Create a new project (form).
- POST `/projects/create` — Submit project creation.
- GET `/projects/{projectId}` — Project details including recent builds.
- GET `/projects/{projectId}/builds/{buildId}` — Build details including logs and execution info.

Templates:
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/projects-create.html`
- `src/main/resources/templates/project-details.html`
- `src/main/resources/templates/build-details.html`

---

## 📡 API Endpoints (Current)

- POST `/webhook`
  - Headers: `X-Hub-Signature-256: sha256=<hex>` (required when `webhook.webhook-secret` is set)
  - Request: `{ "repository": { "full_name": "owner/repo" } }`
  - Response: `{ "status": string, "message": string }`

- GET `/api/projects`
  - Response: `[{ id, createdAt, updatedAt, username, repositoryName, fullName, repositoryUrl, buildTool }]`

- POST `/api/projects/create`
  - Request: `{ "repositoryUrl": "https://github.com/{user}/{repo}.git", "buildTool": "GRADLE|MAVEN" }`
  - Response: a single Project DTO with fields as above

- GET `/api/projects/{projectId}/builds`
  - Response: `[{ id, status, startTime, endTime }]`

---

## ✅ Requirements

- Java 25+
- Git installed and available in PATH
- Gradle CLI installed and available in PATH (unless you adapt to use the project wrapper)

---

## ⚠️ Security and Limitations

- Secrets: GitHub webhook HMAC SHA-256 signature validation is implemented and enforced by a filter when a secret is configured.
  - Set `webhook.webhook-secret` to enable validation. In dev, you can set the env var `WEBHOOK_ALLOW_MISSING_SECRET=true` (maps to `webhook.allow-missing-secret`) to temporarily skip validation.
  - In production, set a strong secret and keep `webhook.allow-missing-secret=false`.
- Execution: Builds run on the host using your local Gradle and Git, in the configured working directory. There is no sandboxing or container isolation yet. Use only with trusted repositories.
- Logging: Build logs are captured and stored with each build; general application logs go to stdout. No retention/capping policies are implemented yet.

See the living roadmap for planned improvements: [ROADMAP.md](./ROADMAP.md).

---

## 👩‍💻 Development & Testing

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Tests: `./gradlew test`
- Coverage report: `./gradlew jacocoTestReport` (open `build/jacocoHtml/index.html`)
- Coverage gate: `./gradlew jacocoTestCoverageVerification` (min coverage 94%)
- All-in-one check: `./gradlew check` (depends on PMD, Spotless, and coverage verification)
- Code Quality: PMD is configured (see `config/pmd/ruleset.xml`). You can run `./gradlew pmdMain pmdTest`.
- Code Style: Spotless is configured; run `./gradlew spotlessApply` to auto-format and remove unused imports.

---

## Health Check

The application exposes two Actuator endpoints for operational visibility:

-   **Health Status:** Check if the application is running.
    ```sh
    curl http://localhost:8080/actuator/health
    ```

-   **Application Info:** Get basic build information.
    ```sh
    curl http://localhost:8080/actuator/info
    ```

## 📄 License

No license file is currently included in this repository. Until a license is added, all rights are reserved. If you intend to use this code, please contact the author or add a LICENSE file (e.g., MIT).


---

## 🐳 Docker

Build the image:

```bash
docker build -t java-build-automaton:latest .
```

Run with PostgreSQL (example):

```bash
# Create an isolated network for DB and app (optional)
docker network create jba-net

# Start PostgreSQL
docker run -d --name jba-db --network jba-net \
  -e POSTGRES_USER=jba -e POSTGRES_PASSWORD=jba -e POSTGRES_DB=jba \
  -p 5432:5432 postgres:16

# Start the application (prod profile)
docker run -d --name jba-app --network jba-net -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JDBC_CONNECTION_STRING=jdbc:postgresql://jba-db:5432/jba \
  -e DB_USERNAME=jba -e DB_PASSWORD=jba \
  -e WEBHOOK_WEBHOOK_SECRET=your-secret \
  java-build-automaton:latest
```

Notes:
- In production, set a strong `WEBHOOK_WEBHOOK_SECRET`. The filter will reject unsigned/invalid webhook requests.
- The app reads DB connection from `JDBC_CONNECTION_STRING`, `DB_USERNAME`, `DB_PASSWORD` (or standard Spring `SPRING_DATASOURCE_*` vars). See `src/main/resources/application.properties` for details.
- Health endpoints are exposed at `/actuator/health` and `/actuator/info`.
