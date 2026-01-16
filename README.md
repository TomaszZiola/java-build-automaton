# Java Build Automaton 🚀

![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen.svg?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin-blue.svg?style=for-the-badge&logo=gradle)
![GitHub Actions](https://img.shields.io/badge/GitHub-Webhook-black?style=for-the-badge&logo=github)

A simple, lightweight CI/CD server written in Spring Boot that automates building and testing projects after every GitHub `push` event.

## 🎯 Overview

The goal of this project is to explore DevOps fundamentals by building a miniature Continuous Integration server. It listens for GitHub webhooks, manages project workspaces, executes builds (Gradle/Maven), and persists history.

## ✨ Key Features

* **Webhook Integration:** Automated builds triggered by GitHub `push` events with HMAC signature validation.
* **Workspace Management:** Automatic repository cloning and updates in isolated directories.
* **Asynchronous Build Queue:** Concurrent execution using virtual threads with configurable limits.
* **Dockerized Builds:** Support for running builds inside isolated Docker containers (configurable).
* **Web UI:** Dashboard for project overview, build history, and detailed execution logs.
* **Persistence:** Full history and project configurations stored in PostgreSQL.

## 🛠️ Tech Stack

* **Backend:** Java 25, Spring Boot 3.5, Spring Data JPA, Hibernate.
* **Database:** PostgreSQL with Flyway migrations.
* **Frontend:** Thymeleaf templates.
* **DevOps:** Docker, Docker Compose, GitHub Webhooks, Ngrok.

## 🚀 Quick Start (Docker)

The fastest way to run the application and its database:

1. **Build the image:**
   ```bash
   docker build -t java-build-automaton:latest .
   ```
2. **Start the stack:**
   ```bash
   docker compose up -d
   ```
   Access the UI at `http://localhost:8080`.

## 🛠️ Local Development

### Prerequisites
* Java 25+
* Git, Docker (optional for isolated builds)
* [Ngrok](https://ngrok.com/) (for local webhook testing)

### Setup
1. **Clone & Build:**
   ```bash
   git clone https://github.com/YourUsername/java-build-automaton.git
   cd java-build-automaton
   ./gradlew build
   ```
2. **Configure Database:** Ensure PostgreSQL is running and set environment variables:
   ```bash
   export JDBC_CONNECTION_STRING=jdbc:postgresql://localhost:5432/jba
   export DB_USERNAME=jba
   export DB_PASSWORD=jba
   ```
3. **Run:**
   ```bash
   ./gradlew bootRun
   ```

## ⚙️ Key Configuration

| Property | Env Variable | Default | Description |
|----------|--------------|---------|-------------|
| `workspace.base-dir` | `WORKSPACE_BASE_DIR` | - | Directory for cloned repos |
| `webhook.webhook-secret` | `WEBHOOK_WEBHOOK_SECRET` | - | GitHub Webhook secret |
| `build.max-parallel` | `BUILD_MAX_PARALLEL` | 3 | Max concurrent builds |
| `build.docker.enabled` | `BUILD_DOCKER_ENABLED` | true | Run builds in Docker |

## 📡 API & Web UI

* **Dashboard:** `GET /`
* **Webhooks:** `POST /webhook` (Requires `X-Hub-Signature-256`)
* **Projects API:** `GET/POST /api/projects`
* **Health:** `/actuator/health`

## 🗺️ Roadmap

Planned features include:
* **Notification System:** Discord/Slack integration for build outcomes.
* **Admin UI:** Enhanced project management (Update/Delete).
* **Real-time Logs:** Streaming build output to the browser via WebSockets.

## 📄 License

All rights reserved. See the repository for future licensing updates.
