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
