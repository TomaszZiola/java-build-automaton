# Java Build Automaton — Product Roadmap

Owner: Project Lead
Last updated: 2025-08-18

## Vision
Democratize CI for small projects and learning environments by providing a simple, self‑hosted CI server that reacts to GitHub push events, clones/updates repositories, runs builds in isolated workspaces, and reports outcomes. Emphasis on simplicity, safety, observability, and extensibility.

## Guiding Principles
- Minimal configuration: onboard a project in under 2 minutes.
- Safety first: run untrusted code with sensible isolation.
- Clear feedback: first‑class logs, statuses, and notifications.
- Extensible core: pluggable runners, notifiers, and storage.
- Operable: metrics, health, and structured logs from day one.

## Milestones

### M0 — Foundation hardening (Weeks 1–2)
Scope:
- Stabilize webhook ingestion, process management, and basic project model.
- Add lightweight persistence for projects and build records.
- Introduce basic health endpoints and structured logging.

Deliverables:
- Build entity (id, projectId, commit, status, start/end time, duration, log pointer).
- PostgreSQL or H2 profile-based persistence.
- Health: `/actuator/health`, `/actuator/info` and app version.
- Structured logs (JSON) with correlation id per webhook delivery.

Acceptance criteria:
- Can list last 20 builds per project via REST.
- 95% of builds complete with correct status for a known matrix of repos.

### M1 — Safe execution (Weeks 3–5)
Scope:
- Introduce execution isolation abstraction and a default LocalRunner strategy with user‑configurable workspace.
- Add pre/post hooks (e.g., validate branch, prune workspace, artifact cleanup).

Deliverables:
- Runner SPI: Runner interface + LocalRunner implementation.
- Workspace policy: per project directory, size limits, TTL cleanup job.
- Pre-checks: branch allowlist, max log size, timeout per step.

Acceptance criteria:
- Building an untrusted repo cannot write outside its workspace.
- Builds time out according to project policy and emit a clear reason.

### M2 — Build history and UI (Weeks 6–8)
Scope:
- Persist and expose build history.
- Minimal web UI to browse projects and builds.

Deliverables:
- REST: list projects, get project, list builds, get build, tail logs endpoint.
- Web UI (Thymeleaf or lightweight SPA) with: projects list, project details, build list/detail & log viewer.

Acceptance criteria:
- Non‑technical users can identify failing builds in UI within 3 clicks.

### M3 — Notifications and statuses (Weeks 9–10)
Scope:
- Pluggable notification system and GitHub commit status updates.

Deliverables:
- Notifier SPI with Slack/Discord/Email adapters.
- GitHub status API integration per build (pending/success/failure) with deep link to UI.

Acceptance criteria:
- On build completion, a Slack message and GitHub status appear consistently across sample repos.

### M4 — Containerized builds (Weeks 11–14)
Scope:
- Optional DockerRunner to run builds in containers.

Deliverables:
- DockerRunner with images for Gradle and Maven (cache enabled).
- Project-level runner configuration (LocalRunner or DockerRunner).
- Resource limits (CPU, memory) for containerized runs.

Acceptance criteria:
- Builds that use DockerRunner do not interfere with host tooling and respect resource limits.

### M5 — Extensibility and ecosystem (Weeks 15–18)
Scope:
- Public extension points and documentation.

Deliverables:
- Stable SPIs: Runner, Notifier, VCS Provider (GitHub, later GitLab).
- Example extensions in a `examples/` module.
- Developer docs and versioned API.

Acceptance criteria:
- Third parties can implement a new Notifier without modifying the core.

### M6 — Reliability and operations (Weeks 19–22)
Scope:
- Improve operability and resiliency.

Deliverables:
- Retries with backoff for transient failures (clone/pull/network).
- Idempotency for webhook deliveries (deduplicate by delivery id + commit sha).
- Metrics: Prometheus endpoints for build counts, durations, success rate.
- Log retention and compression.

Acceptance criteria:
- P99 build start latency within target with concurrent webhooks.

### M7 — Authentication and multi‑tenancy (Weeks 23–26)
Scope:
- Basic authentication and authorization; project scoping.

Deliverables:
- Simple user model (local accounts or OIDC profile).
- Role-based access (viewer, maintainer, admin).
- Per‑user/project API tokens for webhook secrets.

Acceptance criteria:
- Unauthorized users cannot view other projects/build logs.

---

## Architecture Evolution
- Introduce layers:
  - api: REST controllers and UI
  - core: services, orchestration, policies
  - runners: LocalRunner, DockerRunner
  - integrations: GitHub, Notifiers
  - persistence: repositories & migrations
- Use Spring Profiles: dev (H2), prod (PostgreSQL), demo (seeded data)
- Correlation ID filter for each request/webhook

## Data Model (initial)
- Project(id, name, repositoryName, localPath, defaultBranch, runnerType, timeoutSec, secret)
- Build(id, projectId, commitSha, ref, status [PENDING|RUNNING|SUCCESS|FAILED|CANCELLED|TIMED_OUT], createdAt, startedAt, finishedAt, durationMs, logUrl, errorMessage)
- Notification(id, projectId, buildId, channel, status, sentAt, retries)

## APIs (draft)
- GET /api/projects
- POST /api/projects
- GET /api/projects/{id}
- GET /api/projects/{id}/builds
- GET /api/builds/{id}
- GET /api/builds/{id}/log (supports Range or streaming)
- POST /webhook (GitHub)

## Quality & Security
- Unit + integration tests for webhook, runner, and repo operations
- Static analysis: PMD enabled (existing), add SpotBugs
- Security headers, request size limits
- Secrets: never log; use env vars or Spring Config for secrets

## Observability
- Metrics: builds.total, builds.success, builds.duration.histogram
- Logging: JSON layout with correlationId, projectId, buildId
- Tracing (optional): OpenTelemetry via Spring Observability

## Dev Experience
- Makefile/Gradle tasks: run, test, lint, format
- Demo profile with seeded project and sample repo
- Example repositories for Gradle and Maven

## Risks & Mitigations
- Running untrusted code: prefer DockerRunner in prod; document risks
- Log growth: cap logs, compress, retention policy
- Webhook floods: backpressure, queueing (future M8: async workers)

## Release Plan
- Tag versions per milestone: v0.1 (M0), v0.2 (M1), ... v0.7 (M7)
- Changelog per release

## Backlog (selected)
- GitLab webhook provider
- Manual rebuild endpoint
- Artifact publishing (to local artifact store)
- Parallel steps (matrix builds)
- Scheduling builds (cron)

## Contribution Workflow
- Conventional commits
- PR checks: build + tests + PMD + SpotBugs
- Code owners for modules
