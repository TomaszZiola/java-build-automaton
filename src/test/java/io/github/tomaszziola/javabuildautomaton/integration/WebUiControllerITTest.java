package io.github.tomaszziola.javabuildautomaton.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.HTML;
import static org.hamcrest.Matchers.containsString;

import io.github.tomaszziola.javabuildautomaton.utils.BaseIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class WebUiControllerITTest extends BaseIntegrationTest {

  @LocalServerPort int port;

  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @BeforeAll
  static void startContainer() {
    POSTGRESQL_CONTAINER.start();
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @DynamicPropertySource
  static void registerProps(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
  }

  @Test
  @DisplayName("GET / should return dashboard HTML with projects table")
  void get_shouldReturnDashboard() {
    populateDatabase();

    given()
        .when()
        .get("/")
        .then()
        .statusCode(200)
        .contentType(HTML)
        .header("Content-Type", containsString("text/html"))
        .body(containsString("<!DOCTYPE html>"))
        .body(containsString("java-build-automaton"))
        .body(containsString("java-awsomeapp"))
        .body(containsString("MAVEN"))
        .body(containsString("GRADLE"))
        .body(containsString("TomaszZiola"))
        .body(containsString("RobertRoslik"))
        .body(containsString("RobertRoslik/java-awsomeapp"))
        .body(containsString("TomaszZiola/java-build-automaton"))
        .body(containsString("https://github.com/TomaszZiola/java-build-automaton.git"))
        .body(containsString("https://github.com/RobertRoslik/java-awsomeapp.git"))
        .body(containsString("/projects/1"))
        .body(containsString("/projects/2"));
  }
}
