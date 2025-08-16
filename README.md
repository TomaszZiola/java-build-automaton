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

-   [ ] **Configuration Module:** Manage projects from a database instead of a hardcoded path.
-   [ ] **Build History Module:** Save every build (status, logs, commit hash) to a database.
-   [ ] **User Interface:** A simple frontend with Thymeleaf to display the list of projects and their build histories.
-   [ ] **Notification System:** Send email or Discord/Slack notifications about the build outcome.
-   [ ] **Docker Support:** Run builds in isolated Docker containers.

---

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
