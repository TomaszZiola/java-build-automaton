# First Task from Roadmap (M0)

Title: Expose Health and Info Endpoints via Spring Boot Actuator

Context: From ROADMAP.md → M0 — Foundation hardening. One of the earliest deliverables is to provide basic health endpoints and app info so the service can be monitored and verified in different environments.

Why: This gives immediate operational visibility with minimal code impact and helps validate that the service is up after deploys. It also sets the stage for later observability work.

Scope:
- Add Spring Boot Actuator dependency.
- Expose `/actuator/health` and `/actuator/info` over HTTP.
- Include basic build/app info (name, version) in `/actuator/info`.
- Ensure endpoints are visible in dev by default; keep production exposure configurable via properties.

Deliverables:
- Dependency: `org.springframework.boot:spring-boot-starter-actuator`.
- Configuration in `application.properties` (dev defaults):
  - `management.endpoints.web.exposure.include=health,info`
  - `management.endpoint.health.probes.enabled=true`
  - Optional: `management.endpoint.health.show-details=when_authorized` (or `always` for dev)
- Build metadata in `/actuator/info` (choose one):
  - Use Spring Boot Gradle plugin’s `springBoot { buildInfo() }` and ensure the build task generates `META-INF/build-info.properties`.
  - Or add minimal custom application info properties (app.name, app.version) to be mapped into `info` via `info.*` properties.
- README update: Add a short “Health Check” subsection under API or Operations with example curl calls.

Acceptance Criteria:
- `GET /actuator/health` returns 200 with `{"status":"UP"}` in a running local dev instance.
- `GET /actuator/info` returns 200 with at least name and version fields (from build info or properties).
- Tests still pass (`./gradlew test`).
- No endpoints other than `health` and `info` are exposed by default in dev config.

Out of Scope (for future tasks):
- Authentication/authorization for Actuator endpoints.
- Additional endpoints like metrics or env.
- Structured JSON logging; to be handled in separate task per M0.

Proposed Branch Name:
- `feat/actuator-health-endpoints`

Proposed First Commit Message (Conventional Commits):
- `feat(api): expose Actuator health and info endpoints with basic app info`

Implementation Notes:
- In `build.gradle.kts` add:
  - `implementation("org.springframework.boot:spring-boot-starter-actuator")`
  - Inside `springBoot {}` block add `buildInfo()` to generate build metadata, then ensure that `bootRun` depends on tasks that generate build info (usually handled automatically when building).
- In `src/main/resources/application.properties` add the management.* keys listed above.
- Optionally, restrict exposure per profile (e.g., leave minimal exposure in `application.properties` and override in `application-prod.properties`).

How to Start Locally:
1) Create branch: `git checkout -b feat/actuator-health-endpoints`
2) Add dependency + properties as described.
3) Run: `./gradlew bootRun`
4) Verify:
   - `curl -s http://localhost:8080/actuator/health`
   - `curl -s http://localhost:8080/actuator/info`
5) Commit with the provided message and push branch.

---

# Second Task from Roadmap (M0)

Title: Structured JSON Logging with Correlation ID

Context: From ROADMAP.md → M0 — Foundation hardening. Along with health endpoints, early observability is essential. Structured logs enable easier parsing, search, and future metrics/tracing. A correlation ID per request (especially webhook deliveries) allows tying together related log events.

Why: Improves debuggability and prepares the app for future Observability and Operations milestones. This change has low risk and can be toggled per profile.

Scope:
- Introduce a Correlation ID filter that assigns an ID to each incoming HTTP request.
  - If header `X-Correlation-Id` is present, reuse it; otherwise generate a UUID.
  - Store the ID in MDC under key `correlationId` for all downstream logs.
- Switch application logging to JSON in dev by using Logback with a JSON encoder.
  - Include standard fields: timestamp, level, logger, message, thread, correlationId.
- Ensure build/process logs that are streamed from external processes are still logged; include correlationId in the surrounding log statements where applicable.

Deliverables:
- Dependency: `net.logstash.logback:logstash-logback-encoder`.
- New filter class: `io.github.tomaszziola.javabuildautomaton.config.CorrelationIdFilter` registered for all requests.
- Logback configuration file `src/main/resources/logback-spring.xml` that outputs JSON logs for the console appender with the fields above.
- Documentation: brief README section under Observability about correlation IDs and JSON logs, including how to override in prod.

Acceptance Criteria:
- For any HTTP request, logs contain a `correlationId` field with a non-empty value.
- If a client sends `X-Correlation-Id: test-123`, logs show `correlationId":"test-123"`.
- Log lines are valid JSON per line when running locally.
- Existing tests pass (`./gradlew test`).

Out of Scope (for this task):
- Tracing/metrics, log shipping backends.
- Adding correlationId into BuildService’s spawned process output lines; we’ll rely on surrounding context logs.

Proposed Branch Name:
- `feat/logging-correlation-id-json`

Proposed First Commit Message (Conventional Commits):
- `feat(observability): add correlation id filter and JSON logging via logback`

Implementation Notes:
- Add the encoder dependency in `build.gradle.kts`.
- Implement a Spring `OncePerRequestFilter` that manages MDC and header propagation.
- Provide `logback-spring.xml` with a `ConsoleAppender` using Logstash encoder and provider to include `correlationId` from MDC.
- Make JSON logging the default in dev; allow overriding by profile in the future (e.g., different appenders for prod).

How to Start Locally:
1) Create branch: `git checkout -b feat/logging-correlation-id-json`
2) Add the dependency and CorrelationIdFilter + logback-spring.xml.
3) Run: `./gradlew bootRun`
4) Send a request with a header:
   - `curl -H 'X-Correlation-Id: demo-123' http://localhost:8080/actuator/health`
5) Observe console logs contain JSON with `correlationId":"demo-123"`.

---

# Third Task from Roadmap (M0)

Title: Build Entity and Persistence for Build Records (+ Minimal REST Listing)

Context: From ROADMAP.md → M0 — Foundation hardening. We need to persist build executions triggered by webhooks so we can track outcomes, durations, and logs. ROADMAP deliverables call out a Build entity and profile-based persistence (H2 for dev). Acceptance criteria for M0 include listing recent builds via REST.

Why: Persisting builds is essential for visibility and future features (UI, notifications, statuses). It enables operators and developers to see what happened, when, and why, and it unlocks metrics and history.

Scope:
- Define a JPA entity Build with fields aligned to Roadmap Data Model:
  - id (UUID or Long), projectId (FK) or @ManyToOne Project, commitSha, ref (branch/tag),
    status [PENDING|RUNNING|SUCCESS|FAILED|CANCELLED|TIMED_OUT],
    createdAt, startedAt, finishedAt, durationMs,
    logUrl (nullable, placeholder), errorMessage (nullable, short reason on failure).
- Create BuildRepository (Spring Data JPA) with methods to find latest builds by project id (e.g., top 20 by createdAt desc).
- Integrate BuildService with persistence lifecycle:
  - On build trigger, create Build with PENDING → set RUNNING at start.
  - Update SUCCESS or FAILED at the end; set timestamps and durationMs; set errorMessage on failures.
  - Log buildId in key events (start/end) to correlate.
- Minimal REST API to list builds per project (to satisfy M0 acceptance):
  - GET /api/projects/{projectId}/builds?limit=20 (default 20 if not provided).
  - Return recent builds in descending order by createdAt.
- Dev profile uses H2 (already present). Allow JPA auto DDL for now.
- Tests: unit tests for BuildService lifecycle transitions and controller listing endpoint.

Deliverables:
- New class: io.github.tomaszziola.javabuildautomaton.buildsystem.Build (JPA @Entity).
- New interface: io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository.
- BuildService updates to create/update Build records around the external process execution.
- New controller (or extend existing API layer) for listing builds per project: GET /api/projects/{id}/builds.
- Tests covering: repository ordering, service status transitions, and API response shape.

Acceptance Criteria:
- When a webhook triggers a build for a known project, a Build record is created with status RUNNING shortly after trigger, then transitions to SUCCESS if the build process exits with 0, or FAILED otherwise with errorMessage set.
- GET /api/projects/{id}/builds returns the last 20 builds by default with fields: id, status, commitSha, ref, createdAt, startedAt, finishedAt, durationMs.
- All existing tests pass (`./gradlew test`).
- No PII or secrets are logged; error logs do not include repo secrets.

Out of Scope (for this task):
- Web UI for builds.
- GitHub commit statuses or notifications.
- Dockerized/external runners; use current local process execution.
- Log storage/streaming; use console logs and placeholder logUrl.

Proposed Branch Name:
- feat/build-entity-and-persistence

Proposed First Commit Message (Conventional Commits):
- feat(build): introduce Build entity, persistence, and REST listing endpoint

Implementation Notes:
- Use @Enumerated(EnumType.STRING) for BuildStatus enum to avoid ordinal issues.
- Relationship: Build has ManyToOne Project or store projectId as a column if you prefer loose coupling for now; choose the simplest consistent with current Project model.
- Timestamps: use Instant and @PrePersist for createdAt; set startedAt when process begins; finishedAt at completion; compute durationMs safely (null-safe if not finished).
- In BuildService.startBuildProcess(project):
  - Create and save Build(PENDING), then update to RUNNING prior to executing commands.
  - On success: SUCCESS; on exception/exitCode != 0: FAILED with errorMessage.
  - Ensure InterruptedException handling does not mask the original cause; if interrupted, set thread interrupt flag and mark build FAILED with appropriate message before rethrowing.
- REST: New controller under package api (e.g., io.github...api.BuildController) with ProjectRepository existence checks and optional limit param (default 20, max 100).
- Tests: Use H2 and @DataJpaTest for repository; @WebMvcTest for controller; service unit tests can mock repository interactions.

How to Start Locally:
1) Create branch: git checkout -b feat/build-entity-and-persistence
2) Implement Build entity, repository, and service/controller changes as described.
3) Run: ./gradlew test
4) Start app: ./gradlew bootRun
5) Trigger a webhook or call existing endpoints to start a build, then verify:
   - GET http://localhost:8080/api/projects/{id}/builds
6) Commit with the provided message and push branch.
