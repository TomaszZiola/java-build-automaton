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
class ProjectRestControllerITTest extends BaseIntegrationTest {

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
    final var savedProjects = populateManyProjectsDatabase();
    final var firstProject = savedProjects.getFirst();
    final var secondProject = savedProjects.getLast();

    given()
        .when()
        .get("/")
        .then()
        .statusCode(200)
        .contentType(HTML)
        .header("Content-Type", containsString("text/html"))
        .body(containsString("<!DOCTYPE html>"))
        .body(containsString(secondProject.getRepositoryFullName()))
        .body(containsString(firstProject.getRepositoryFullName()))
        .body(containsString(secondProject.getUsername()))
        .body(containsString(firstProject.getUsername()))
        .body(containsString(secondProject.getRepositoryName()))
        .body(containsString(firstProject.getRepositoryName()))
        .body(containsString(secondProject.getRepositoryUrl()))
        .body(containsString(firstProject.getRepositoryUrl()))
        .body(containsString(secondProject.getBuildTool().toString()))
        .body(containsString(firstProject.getBuildTool().toString()))
        .body(containsString("/projects/" + firstProject.getId()))
        .body(containsString("/projects/" + secondProject.getId()));
  }
}
