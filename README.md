# Java Build Automaton 🚀

![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-brightgreen.svg?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin-blue.svg?style=for-the-badge&logo=gradle)
![GitHub Actions](https://img.shields.io/badge/GitHub-Webhook-black?style=for-the-badge&logo=github)

A simple, lightweight CI/CD server written in Spring Boot that automates the process of building and testing projects after every `push` to a GitHub repository.

## 🎯 Project Description

The goal of this project is to build a miniature Continuous Integration server from scratch to explore the fundamental principles of DevOps tools. The application listens for `push` events sent by GitHub webhooks, then updates an existing local clone of the repository via `git pull`, runs the build process (e.g., using Gradle/Maven), and reports on the outcome.

This is not just another CRUD application—it's a practical tool that solves a real-world automation problem in the software development lifecycle.

---

## ✨ Key Features

* **Webhook Reception:** Fully integrated with the GitHub API to listen for `push` events.
* **Dynamic Process Execution:** Securely runs system commands (`git`, `gradle`, `mvn`) from within the Java application using `ProcessBuilder`.
* **Workspace Isolation:** Each project is built in its own dedicated working directory.
* **Real-time Logging:** Captures logs from the build process into memory and stores them with the build; streaming to console/UI is not yet implemented.
* **Error Handling:** Detects build failures based on the process exit code.

---

## 🛠️ Tech Stack

* Backend: Java 21, Spring Boot 3.5.4
* Build Tool: Gradle (Kotlin DSL)
* API: Spring Web (REST), Spring Boot Actuator
* Frontend: Thymeleaf (dashboard and project details)
* Data: Spring Data JPA; H2 in dev, PostgreSQL in prod (Flyway migrations)
* Integration: GitHub Webhooks with HMAC SHA-256 signature validation
* Observability: CorrelationId filter, structured logging (logstash encoder)
* Containerization: Docker (multi-stage Dockerfile)

---

## 🚀 Getting Started

To run the project locally, follow the steps below.

### Prerequisites

* Java JDK 21 or newer
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
    * **Secret:** Set a strong secret. Use the same value in the app via `app.github.webhook-secret` (or env `APP_GITHUB_WEBHOOK_SECRET`).
    * Save the webhook.

6.  **Push a commit to your test repository and check the application logs; build output is captured and stored.**

---

## 🗺️ Roadmap

This project is actively under development. Here are the planned features:

See the full, living plan in [ROADMAP.md](./ROADMAP.md).

-   [ ] **Configuration Module:** Manage projects from a database instead of a hardcoded path.
-   [ ] **Build History Module:** Save every build (status, logs, commit hash) to a database.
-   [ ] **Enhance User Interface:** Expand existing Thymeleaf views to display detailed build logs and statuses.
-   [ ] **Notification System:** Send email or Discord/Slack notifications about the build outcome.
-   [ ] **Docker Support:** Run builds in isolated Docker containers.

---

## ⚙️ Configuration

This project currently seeds a demo Project at startup for convenience (see `DataSeeder`). To make it work on your machine:

- Adjust the seeded project's fields in `src/main/java/io/github/tomaszziola/javabuildautomaton/config/DataSeeder.java`:
  - name: Friendly display name
  - repositoryName: GitHub repo in the form `owner/repo` (e.g., `TomaszZiola/test`)
  - localPath: Absolute path on your machine where the repo exists. The app will run commands in this directory.
- Ensure that directory is a valid git working copy of the specified repository and that you have the appropriate access.

Database:
- Uses in-memory H2 by default; data (including seeded project and build history) resets on restart.
- H2 console is enabled at `/h2-console` (username `sa`, empty password).

Important:
- The build process uses the system `gradle` command (not the wrapper). Make sure Gradle is installed and on your PATH. Alternatively, modify `BuildExecutor` to use `./gradlew` in your repo.
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
- `app.github.webhook-secret`: the shared secret value
- `app.github.allow-missing-webhook-secret`: set to `true` in dev to skip validation when no secret is provided (default `false` in prod)

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
- In development, if you do not set a secret and `app.github.allow-missing-webhook-secret=true`, the header is not required.
- On GitHub, set the webhook "Secret" to the same value as `app.github.webhook-secret`.

---

## 🖥️ Web UI

- GET `/` — Dashboard listing all projects with basic info.
- GET `/projects/{projectId}` — Project details including recent builds.

Templates:
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/templates/project-details.html`

---

## 📡 API Endpoints (Current)

- POST `/webhook`
  - Headers: `X-Hub-Signature-256: sha256=<hex>` (required when `app.github.webhook-secret` is set)
  - Request: `{ "repository": { "full_name": "owner/repo" } }`
  - Response: `{ "status": string, "message": string }`

- GET `/api/projects`
  - Response: `[{ id, name, repositoryName, localPath, buildTool }]`

- GET `/api/projects/{projectId}/builds`
  - Response: `[{ id, status, startTime, endTime }]`

---

## ✅ Requirements

- Java 21+
- Git installed and available in PATH
- Gradle CLI installed and available in PATH (unless you adapt to use the project wrapper)

---

## ⚠️ Security and Limitations

- Secrets: GitHub webhook HMAC SHA-256 signature validation is implemented and enforced by a filter when a secret is configured.
  - Set `app.github.webhook-secret` to enable validation. In dev, `app.github.allow-missing-webhook-secret=true` allows skipping it (see `application-dev.properties`).
  - In production, set a strong secret and keep `app.github.allow-missing-webhook-secret=false`.
- Execution: Builds run on the host using your local Gradle and Git, in the configured working directory. There is no sandboxing or container isolation yet. Use only with trusted repositories.
- Logging: Build logs are captured and stored with each build; general application logs go to stdout. No retention/capping policies are implemented yet.

See the living roadmap for planned improvements: [ROADMAP.md](./ROADMAP.md).

---

## 👩‍💻 Development & Testing

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Tests: `./gradlew test`
- Coverage report: `./gradlew jacocoTestReport` (open `build/jacocoHtml/index.html`)
- Coverage gate: `./gradlew jacocoTestCoverageVerification` (min coverage 99%)
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
  -e APP_GITHUB_WEBHOOK_SECRET=your-secret \
  java-build-automaton:latest
```

Notes:
- In production, set a strong `APP_GITHUB_WEBHOOK_SECRET`. The filter will reject unsigned/invalid webhook requests.
- The app reads DB connection from `JDBC_CONNECTION_STRING`, `DB_USERNAME`, `DB_PASSWORD` (see `application-prod.properties`).
- Health endpoints are exposed at `/actuator/health` and `/actuator/info`.
