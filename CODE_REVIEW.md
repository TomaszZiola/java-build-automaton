Java Build Automaton — Code Review (2025-08-20)

Scope
- Reviewed core build pipeline (BuildService, ProcessExecutor), domain (Build, Project), web/API (WebhookController, ProjectService, ApiResponse), config (CorrelationIdFilter, DataSeeder), and tests.
- All existing unit tests pass locally at time of review.

Strengths
- Clear separation of concerns: Webhook -> Service -> Build execution.
- Logging and MDC correlation implemented correctly with cleanup.
- Build flow persists intermediate and final build states with logs.
- Tests cover happy path and failure path for BuildService, MDC behavior, and lookup flow.

Findings and Recommendations

1. BuildService robustness
- Observation: startBuildProcess catches IOException/InterruptedException only. Any RuntimeException (e.g., NPE from project fields) will bubble up and may leave build record in-progress.
- Recommendation: Add a catch-all for RuntimeException to mark build as FAILED and persist logs; consider using a domain-specific exception (BuildProcessException) to wrap causes.
- Observation: No validation for project.getLocalPath() / BuildTool; working directory may be null/blank or missing.
- Recommendation: Validate inputs and log a clear message before executing external processes.
- Observation: Builds run synchronously and block the request thread if triggered in request handling path.
- Recommendation: Consider making build execution asynchronous (e.g., @Async or queue), returning 202 Accepted to webhook; not required immediately, but plan for it.

2. ProcessExecutor resiliency
- Observation: Single-threaded stream consumption with redirectErrorStream(true) avoids basic deadlocks, but long-running processes with very large output can still be problematic, and there is no timeout.
- Recommendations:
  - Add a configurable timeout and destroy process on timeout or interrupt.
  - Preserve exit code and include command/working directory in exception messages for debugging.
  - Use explicit Charset (e.g., UTF-8) for InputStreamReader.
  - Optionally capture stderr separately without redirectErrorStream to distinguish errors from standard output.

3. Security and safety of external commands
- Observation: Commands are constructed as String array, which avoids shell injection, good. However, values originate from configuration/DB.
- Recommendation: Validate/whitelist BuildTool and consider restricting allowed commands. Avoid logging secrets in command args.

5. CorrelationIdFilter
- Observation: Solid implementation; it sets and clears MDC and returns header.
- Recommendation: Consider making header name configurable; consider case-insensitive retrieval (Servlet API headers are already case-insensitive in practice).

6. API layer
- Observation: WebhookController does not validate payload; exceptions will bubble up.
- Recommendation: Add @Validated and validation annotations on GitHubWebhookPayload DTO; add a ControllerAdvice for consistent error responses including correlation id.

7. Data seeding
- Observation: DataSeeder seeds a hardcoded path specific to a developer machine.
- Risk: Running the app in different environments will persist invalid paths and cause builds to fail.
- Recommendation: Gate seeding under a profile (e.g., @Profile("dev")) and/or externalize path via configuration.

8. Observability
- Recommendation: Include correlation id in structured logs via logback pattern (already supported by MDC); optionally store correlation id with Build records for traceability.

9. Testing
- Observation: Unit tests are comprehensive for current scope; integration tests are absent.
- Recommendation: Add a slice/integration test for ProcessExecutor with a harmless command (e.g., echo) guarded by profile, or mock at boundary and add a test for error propagation in BuildService when a RuntimeException occurs.

Minimal Code Changes Suggested (non-breaking)
- Catch RuntimeException in BuildService to mark build as FAILED and persist logs.
- Validate working directory existence before executing commands and fail fast with a meaningful message.
- Make DataSeeder dev-only via @Profile("dev").

These are intentionally minimal changes to improve robustness without altering observable behavior in tests. If you want, I can implement them in a follow-up.

Appendix: Potential Future Enhancements
- Asynchronous build execution with status endpoint.
- Store truncated logs with pagination or external log storage.
- Support for build parameters and multi-step pipelines.
- Authentication and signature verification for GitHub webhooks.
