# Java Build Automaton - Production Roadmap

## Vision & Scope

### Problem Statement
Modern software teams need lightweight, secure CI/CD automation that can be easily deployed and maintained without the complexity of enterprise CI systems. The Java Build Automaton addresses this by providing a minimal, production-ready "micro CI server" that:

- Receives GitHub push webhooks with cryptographic verification
- Applies intelligent build policies (branch filtering, idempotency)
- Executes builds in isolated environments using standard build tools
- Provides persistent build history and real-time logging
- Offers both web UI and REST API access
- Runs reliably in containerized environments with PostgreSQL

### What "Done" Means for v1.0
- **Security**: HMAC webhook validation, request size limits, proper secrets management
- **Reliability**: Timeout handling, graceful error recovery, process isolation
- **Observability**: Structured logging, health checks, build status tracking
- **Operability**: Docker/Compose deployment, database migrations, configuration via environment variables
- **Functionality**: Webhook ingestion → build queuing → execution → status reporting

### Non-Goals
- Multi-tenant architecture or user authentication (single-tenant assumed)
- Build artifact storage or deployment capabilities
- Complex build pipelines or multi-stage workflows
- Integration with external systems beyond GitHub webhooks
- Horizontal scaling or load balancing (vertical scaling sufficient for v1.0)

## Architecture Overview

### High-Level Components
```
GitHub → Webhook Filter → Ingestion Service → Build Queue → Build Executor
                                                     ↓
                                              Database (Builds, Projects)
                                                     ↓
                                              Web UI ← REST API
```

### Data Flow
1. **Ingestion**: GitHub webhook → HMAC validation → idempotency check → branch policy
2. **Queuing**: Project lookup → build creation (QUEUED status) → async processing
3. **Execution**: Git pull → build tool detection → process execution → log capture
4. **Persistence**: Status updates → log storage → completion timestamps
5. **Access**: REST API + Thymeleaf UI for build monitoring

### Key Design Principles
- **Defense in Depth**: Multiple validation layers (HMAC, idempotency, branch policy)
- **Fail-Safe**: Always persist build state, even on unexpected failures
- **Observability**: Correlation IDs throughout request lifecycle
- **Configurability**: Environment-driven configuration for all deployment contexts

## Security Model

### HMAC Webhook Validation
- **Implementation**: `WebhookSecurityService` + `WebhookSignatureFilter`
- **Algorithm**: HMAC-SHA256 with configurable shared secret
- **Configuration**: `APP_GITHUB_WEBHOOK_SECRET` environment variable
- **Fallback**: Development mode with `ALLOW_MISSING_WEBHOOK_SECRET=true`
- **Protection**: Validates `X-Hub-Signature-256` header against request body

### Idempotency Protection
- **Implementation**: `IdempotencyService` with database constraint enforcement
- **Mechanism**: Unique constraint on GitHub delivery ID in `webhook_delivery` table
- **Race Condition Safe**: Uses `DataIntegrityViolationException` for duplicate detection
- **Cleanup**: Consider TTL-based cleanup for old delivery records (future enhancement)

### Request Limits & Safety
- **Current State**: Basic Spring Boot defaults
- **Recommended Additions**:
  - Request body size limits (e.g., 1MB max webhook payload)
  - Rate limiting per IP/endpoint
  - Request timeout configuration
  - Maximum log size limits to prevent disk exhaustion

### Secrets Management
- **Current**: Environment variables for database and webhook secrets
- **Production Ready**: Compatible with Docker secrets, Kubernetes ConfigMaps/Secrets
- **Rotation**: Manual process (documented in runbooks)

## Data Model & Migrations

### Current Schema (Flyway)

#### Projects (`V1_0_0__create_project.sql`)
```sql
CREATE TABLE project (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),          -- ⚠️ Missing NOT NULL
    repository_name VARCHAR(255), -- ⚠️ Missing NOT NULL  
    local_path VARCHAR(255),    -- ⚠️ Missing NOT NULL
    build_tool VARCHAR(255)     -- ⚠️ Missing NOT NULL (causes NPE)
);
```

#### Builds (`V1_1_0__create_build.sql`)
```sql
CREATE TABLE build (
    id BIGINT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status VARCHAR(255),        -- ⚠️ Missing NOT NULL
    start_time TIMESTAMP,       -- ⚠️ Missing NOT NULL
    end_time TIMESTAMP,
    logs TEXT,
    CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES project (id)
);
```

#### Webhook Deliveries (`V1_2_0__webhook_delivery_idempotency.sql`) ✅
```sql
CREATE TABLE webhook_delivery (
    id BIGSERIAL PRIMARY KEY,
    delivery_id VARCHAR(128) NOT NULL UNIQUE,
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### Required Schema Improvements
- **Project table**: Add NOT NULL constraints on critical fields
- **Build table**: Add NOT NULL on status, start_time
- **Indexes**: Add performance indexes on frequently queried fields
- **Audit fields**: Consider created_at, updated_at timestamps

### Migration Strategy
- Use additive migrations to avoid downtime
- Validate existing data before applying constraints
- Consider default values for nullable fields

## Operational Model

### Container Deployment

#### Multi-Stage Dockerfile ✅
- **Build stage**: Eclipse Temurin JDK 21 with Gradle wrapper
- **Runtime stage**: Eclipse Temurin JRE 21 with non-root user
- **Security**: Runs as `appuser` (uid 10001)
- **Configurability**: `JAVA_OPTS` environment variable

#### Docker Compose Stack ✅
```yaml
services:
  db:
    image: postgres:16
    healthcheck: pg_isready validation
    volumes: persistent data storage
  app:
    depends_on: db health check
    healthcheck: actuator/health endpoint
    environment: full configuration via env vars
```

### Environment Variables Convention
- **Database**: `SPRING_DATASOURCE_*` (Spring Boot standard)
- **Security**: `APP_GITHUB_WEBHOOK_SECRET`
- **Build**: `APP_BUILD_TIMEOUT_SECONDS` (recommended addition)
- **Deployment**: `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`

### Target Production Environments
- **Local Development**: Docker Compose with local volumes
- **Cloud Deployment**: ECS with RDS PostgreSQL
- **Infrastructure as Code**: Terraform modules included (`infra/`)

### Health & Monitoring
- **Spring Actuator**: Health, info endpoints enabled
- **Container Health Checks**: Both database and application
- **Logging**: Structured JSON via Logstash encoder
- **Correlation IDs**: Full request tracing via MDC

## Milestones & Prioritized Tasks

### Milestone 1: Core Stability (P0 - Critical) 🔴
**Target**: Fix blocking robustness issues that can leave builds in inconsistent state

#### Tasks:
1. **BuildService Exception Handling** (Size: S)
   - Add catch-all `RuntimeException` handler in `executeBuildSteps`
   - Mark build as FAILED, append normalized error message
   - Ensure final state persistence even on unexpected errors
   - **Acceptance**: No builds left IN_PROGRESS on RuntimeException

2. **Input Validation** (Size: S)  
   - Validate Project fields: non-null BuildTool, non-blank localPath
   - Add null checks before switch statement in BuildExecutor
   - Fail fast with actionable error messages
   - **Acceptance**: Clear error messages for invalid project configuration

3. **Database Schema Constraints** (Size: M)
   - Add NOT NULL constraints to project table critical fields
   - Add NOT NULL constraints to build status and start_time
   - Create migration with data validation
   - **Acceptance**: Database prevents invalid states at schema level

### Milestone 2: Process Resilience (P1 - High Priority) 🟡
**Target**: Prevent hanging processes and resource exhaustion

#### Tasks:
1. **ProcessExecutor Timeout** (Size: M)
   - Implement configurable timeout (default 10 minutes)
   - Use `process.waitFor(timeout, TimeUnit)` 
   - Force kill processes that exceed timeout
   - Log timeout events with process details
   - **Acceptance**: No processes run longer than configured timeout

2. **Build Tool Wrapper Detection** (Size: S)
   - Check for `gradlew`/`mvnw` in working directory first
   - Fallback to system `gradle`/`mvn` if wrappers not found
   - Make behavior configurable via application property
   - **Acceptance**: Prefers project wrappers when available

3. **Memory-Safe Output Handling** (Size: M)
   - Implement bounded buffer for process output
   - Stream large outputs to temporary files
   - Add configurable limits on log retention
   - **Acceptance**: Process output doesn't cause OOM errors

### Milestone 3: Enhanced Security (P1 - High Priority) 🟡
**Target**: Production-grade security and operational safety

#### Tasks:
1. **Request Size Limits** (Size: S)
   - Configure `spring.servlet.multipart.max-file-size`
   - Set reasonable webhook payload limits (1MB)
   - Return proper 413 responses for oversized requests
   - **Acceptance**: Large payloads rejected gracefully

2. **Payload Validation** (Size: M)
   - Add `@Validated` to webhook controller
   - Implement Bean Validation on `GitHubWebhookPayload`
   - Create `@ControllerAdvice` for consistent error responses
   - Include correlation ID in error responses
   - **Acceptance**: Invalid payloads return structured 400 errors

3. **Rate Limiting** (Size: L)
   - Implement basic rate limiting for webhook endpoint
   - Use in-memory sliding window or Redis-based solution
   - Configure limits per IP address
   - **Acceptance**: Excessive requests are throttled appropriately

### Milestone 4: Operational Excellence (P2 - Medium Priority) 🟢
**Target**: Production monitoring and maintainability

#### Tasks:
1. **Enhanced Observability** (Size: M)
   - Store correlation ID in build records
   - Add build duration metrics
   - Implement custom health indicators
   - Add application metrics endpoint
   - **Acceptance**: Full request traceability through logs and database

2. **Configuration Management** (Size: S)
   - Create comprehensive environment variable documentation
   - Add configuration validation at startup
   - Implement configuration health checks
   - **Acceptance**: Misconfigurations detected at startup

3. **Log Management** (Size: M)
   - Implement log rotation and cleanup
   - Add configurable log retention policies
   - Consider external log storage integration
   - **Acceptance**: Log storage growth is controlled and predictable

### Milestone 5: Developer Experience (P2 - Medium Priority) 🟢
**Target**: Improved APIs and developer productivity

#### Tasks:
1. **Enhanced API Response Model** (Size: S)
   - Standardize error response format (code, message, correlationId)
   - Include build ID or status check URL in success responses
   - Add OpenAPI/Swagger documentation
   - **Acceptance**: APIs follow consistent response patterns

2. **Build Status Endpoints** (Size: M)
   - Add GET `/api/builds/{id}` endpoint
   - Add GET `/api/projects/{id}/builds` for build history
   - Include pagination for build lists
   - **Acceptance**: Build status accessible via REST API

3. **Async Build Processing** (Size: L)
   - Implement proper async build execution
   - Add build queue management endpoints
   - Provide real-time build status updates
   - **Acceptance**: Build requests return immediately with queue status

## Risks & Trade-offs

### Technical Risks
1. **Process Management Complexity**
   - **Risk**: Timeout handling may not catch all edge cases
   - **Mitigation**: Comprehensive testing with various build scenarios
   - **Trade-off**: Complexity vs. reliability

2. **Database Migration Safety**
   - **Risk**: Adding NOT NULL constraints may fail on existing data
   - **Mitigation**: Data validation and cleanup before constraint addition
   - **Trade-off**: Migration complexity vs. data integrity

3. **Memory Usage Growth**
   - **Risk**: Large build outputs may consume excessive memory
   - **Mitigation**: Bounded buffers and streaming to disk
   - **Trade-off**: Performance vs. memory safety

### Operational Risks
1. **Secret Rotation**
   - **Risk**: Webhook secret rotation requires coordination with GitHub
   - **Mitigation**: Document rotation procedure with downtime windows
   - **Trade-off**: Security vs. operational complexity

2. **Database Storage Growth**
   - **Risk**: Build logs accumulate indefinitely
   - **Mitigation**: Implement retention policies and archival
   - **Trade-off**: Historical data vs. storage costs

### Architecture Trade-offs
1. **Synchronous vs. Asynchronous Processing**
   - **Current**: Synchronous webhook processing
   - **Future**: Message queue for true async processing
   - **Trade-off**: Simplicity vs. scalability

2. **Monolith vs. Microservices**
   - **Decision**: Single Spring Boot application
   - **Rationale**: Operational simplicity for target scale
   - **Trade-off**: Deployment simplicity vs. component scalability

## Appendix

### Example curl Commands

#### Webhook with HMAC Signature
```bash
# Generate signature
PAYLOAD='{"ref":"refs/heads/main","repository":{"name":"test-repo"}}'
SECRET="your-webhook-secret"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" -binary | xxd -p)

# Send webhook
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -H "X-Hub-Signature-256: sha256=$SIGNATURE" \
  -H "X-GitHub-Delivery: $(uuidgen)" \
  -d "$PAYLOAD"
```

#### API Examples
```bash
# Get build status
curl http://localhost:8080/api/builds/1

# List project builds
curl http://localhost:8080/api/projects/1/builds

# Health check
curl http://localhost:8080/actuator/health
```

### Environment Configuration

#### Development (.env)
```bash
# Database
DB_USERNAME=jba
DB_PASSWORD=jba-dev-password
DB_NAME=jba
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jba

# Security
APP_GITHUB_WEBHOOK_SECRET=dev-webhook-secret-change-in-production
ALLOW_MISSING_WEBHOOK_SECRET=true

# Build Configuration
APP_BUILD_TIMEOUT_SECONDS=600
```

#### Production Environment
```bash
# Database (use managed PostgreSQL)
DB_USERNAME=jba_prod
DB_PASSWORD=<generated-strong-password>
DB_NAME=jba_production
SPRING_DATASOURCE_URL=jdbc:postgresql://rds-endpoint:5432/jba_production

# Security (rotate regularly)
APP_GITHUB_WEBHOOK_SECRET=<github-webhook-secret-from-repo-settings>
ALLOW_MISSING_WEBHOOK_SECRET=false

# Build Configuration
APP_BUILD_TIMEOUT_SECONDS=900
JAVA_OPTS="-Xmx1g -Xms512m"

# Additional production settings
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Runbooks

#### Rotate Webhook Secret
1. Generate new secret: `openssl rand -hex 32`
2. Update GitHub webhook configuration with new secret
3. Update `APP_GITHUB_WEBHOOK_SECRET` environment variable
4. Restart application container
5. Verify webhook delivery with test push

#### Reset Database Volume
```bash
# Stop services
docker-compose down

# Remove volume (⚠️ DATA LOSS)
docker volume rm java-build-automaton_jba-db-data

# Restart with fresh database
docker-compose up -d
```

#### Backup Database
```bash
# Create backup
docker exec jba-db pg_dump -U $DB_USERNAME $DB_NAME > backup_$(date +%Y%m%d).sql

# Restore backup
docker exec -i jba-db psql -U $DB_USERNAME $DB_NAME < backup_20250919.sql
```

#### Monitor Build Queue
```bash
# Check running processes
docker exec jba-app ps aux | grep -E "(gradle|mvn)"

# View recent logs
docker logs --tail=100 -f jba-app

# Check database build status
docker exec -it jba-db psql -U $DB_USERNAME $DB_NAME -c "
  SELECT status, COUNT(*) 
  FROM build 
  GROUP BY status;
"
```

#### Emergency Build Cleanup
```sql
-- Mark stuck IN_PROGRESS builds as FAILED
UPDATE build 
SET status = 'FAILED', 
    logs = COALESCE(logs, '') || E'\n[[ERROR]] Build marked failed due to timeout cleanup',
    end_time = NOW()
WHERE status = 'IN_PROGRESS' 
  AND start_time < NOW() - INTERVAL '1 hour';
```

---

**Document Version**: 1.0  
**Last Updated**: 2025-09-19  
**Status**: Ready for Implementation
