# Java Build Automaton 🚀

![Java](https://img.shields.io/badge/Java-21-orange.svg?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-brightgreen.svg?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin-blue.svg?style=for-the-badge&logo=gradle)
![GitHub Actions](https://img.shields.io/badge/GitHub-Webhook-black?style=for-the-badge&logo=github)

A simple, lightweight CI/CD server written in Spring Boot that automates the process of building and testing projects after every `push` to a GitHub repository.

## 🎯 Project Description

The goal of this project is to build a miniature Continuous Integration server from scratch to explore the fundamental principles of DevOps tools. The application listens for `push` events sent by GitHub webhooks, then clones or updates the repository, runs the build process (e.g., using Gradle/Maven), and reports on the outcome.

This is not just another CRUD application—it's a practical tool that solves a real-world automation problem in the software development lifecycle.

---

## ✨ Key Features

* **Webhook Reception:** Fully integrated with the GitHub API to listen for `push` events.
* **Dynamic Process Execution:** Securely runs system commands (`git`, `gradle`, `mvn`) from within the Java application using `ProcessBuilder`.
* **Workspace Isolation:** Each project is built in its own dedicated working directory.
* **Real-time Logging:** Captures and displays logs from the build process live in the application console.
* **Error Handling:** Detects build failures based on the process exit code.

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3.5.4
* **Build Tool:** Gradle (Kotlin DSL)
* **API:** Spring Web (REST)
* **Integration:** GitHub Webhooks
* **Coming Soon:**
    * **Database:** PostgreSQL (for storing build history)
    * **Frontend:** Thymeleaf (for visualizing results)
    * **Containerization:** Docker

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
    The application will start on port `8080`.

4.  **Expose your local server with Ngrok:**
    ```bash
    ngrok http 8080
    ```
    Copy the public URL provided by Ngrok (e.g., `https://xxxx-xx-xxx.ngrok.io`).

5.  **Configure the Webhook on GitHub:**
    * In your test repository, go to `Settings` > `Webhooks` > `Add webhook`.
    * **Payload URL:** Paste the URL from Ngrok, adding `/webhook` at the end.
    * **Content type:** Change to `application/json`.
    * Save the webhook.

6.  **Push a commit to your test repository and watch the logs in your application's console!**

---

## 🗺️ Roadmap

This project is actively under development. Here are the planned features:

See the full, living plan in [ROADMAP.md](./ROADMAP.md).

-   [ ] **Configuration Module:** Manage projects from a database instead of a hardcoded path.
-   [ ] **Build History Module:** Save every build (status, logs, commit hash) to a database.
-   [ ] **User Interface:** A simple frontend with Thymeleaf to display the list of projects and their build histories.
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

Important:
- The build process uses the system `gradle` command (not the wrapper). Make sure Gradle is installed and on your PATH. Alternatively, modify `BuildService` to use `./gradlew` in your repo.
- No webhook secret validation is implemented yet; see Security notes below.

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
  "status": "success | not_found",
  "message": "..."
}
```

- status="success" means the project was found and a build was started.
- status="not_found" means the repository name didn’t match any project in the database.

---

## 📡 API Endpoints (Current)

- POST `/webhook`
  - Request: `{ "repository": { "full_name": "owner/repo" } }`
  - Response: `{ "status": string, "message": string }`

---

## ✅ Requirements

- Java 21+
- Git installed and available in PATH
- Gradle CLI installed and available in PATH (unless you adapt to use the project wrapper)

---

## ⚠️ Security and Limitations

- Secrets: There is currently no verification of the GitHub webhook signature/secret. Do not expose this endpoint publicly without adding signature validation.
- Execution: Builds run on the host using your local Gradle and Git, in the configured working directory. There is no sandboxing or container isolation yet. Use only with trusted repositories.
- Logging: Logs are printed to application stdout; no retention/capping is implemented.

See the living roadmap for planned improvements: [ROADMAP.md](./ROADMAP.md).

---

## 👩‍💻 Development & Testing

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Tests: `./gradlew test`
- Code Quality: PMD is configured (see `config/pmd/ruleset.xml`).

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

This project is licensed under the MIT License. See the `LICENSE` file for more details.
