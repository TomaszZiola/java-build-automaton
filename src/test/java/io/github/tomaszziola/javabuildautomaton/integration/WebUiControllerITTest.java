package io.github.tomaszziola.javabuildautomaton.integration;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static io.github.tomaszziola.javabuildautomaton.utils.MyMatchers.containsWholeWordOutsideUrls;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.port;
import static io.restassured.http.ContentType.HTML;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.github.tomaszziola.javabuildautomaton.utils.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class WebUiControllerITTest extends BaseIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void registerProps(final DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
  }

  @Test
  @DisplayName("GET / should return dashboard HTML with projects table")
  void get_shouldReturnEmptyDashboard() {

    given()
        .when()
        .get("/")
        .then()
        .statusCode(200)
        .contentType(HTML)
        .header("Content-Type", containsString("text/html"))
        .body(containsString("Project Dashboard"))
        .body(containsString("No projects have been configured yet."))
        .body(containsString("href=\"/projects/create\""))
        .body(containsString("+ Create your first project"))
        .body(not(containsString("Repository Name")))
        .body(not(containsString("Repository URL")))
        .body(not(containsString("Build Tool")))
        .body(not(containsString("<tbody>")));
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
        .body(containsWholeWordOutsideUrls(secondProject.getRepositoryName()))
        .body(containsWholeWordOutsideUrls(firstProject.getRepositoryName()))
        .body(containsString(secondProject.getRepositoryUrl()))
        .body(containsString(firstProject.getRepositoryUrl()))
        .body(containsString(secondProject.getBuildTool().toString()))
        .body(containsString(firstProject.getBuildTool().toString()))
        .body(containsString("/projects/" + firstProject.getId()))
        .body(containsString("/projects/" + secondProject.getId()));
  }

  @Test
  @DisplayName("GET /projects/create should return project creation form with build tool options")
  void get_CreateProject_shouldReturnForm() {
    given()
        .when()
        .get("/projects/create")
        .then()
        .statusCode(200)
        .contentType(HTML)
        .body(containsString("Repository URL"))
        .body(containsString("Build Tool"))
        .body(containsString("MAVEN"));
  }

  @Test
  @DisplayName(
      "POST /projects/create with invalid repo URL should return form with validation error")
  void postCreateProject_withInvalidUrl_shouldShowValidationError() {
    given()
        .formParam("repositoryUrl", "not-a-valid-url")
        .formParam("buildTool", "MAVEN")
        .when()
        .post("/projects/create")
        .then()
        .statusCode(200)
        .contentType(HTML)
        .body(containsString("<!DOCTYPE html>"))
        .body(
            containsString(
                "Repository URL must match pattern: https://github.com/{user}/{repo}.git"))
        .body(containsString("MAVEN"));
  }

  @Test
  @DisplayName(
      "POST /projects/create with valid data should redirect to / and show new project on dashboard")
  void postCreateProject_withValidData() {
    final var url = "https://github.com/foo/bar.git";

    final var location =
        given()
            .relaxedHTTPSValidation()
            .formParam("repositoryUrl", url)
            .formParam("buildTool", MAVEN)
            .when()
            .post("/projects/create")
            .then()
            .statusCode(302)
            .extract()
            .header("Location");

    assertThat(location).isEqualTo("http://localhost:" + port + "/");

    given()
        .when()
        .get(location)
        .then()
        .statusCode(200)
        .contentType(HTML)
        .body(containsString(url))
        .body(containsWholeWordOutsideUrls("bar"))
        .body(containsString(MAVEN.toString()));
  }

  @Test
  @DisplayName("GET /projects/{id} should return project details page (HTML)")
  void get_ProjectDetails_shouldReturnForm() {
    final var projectId = populateSingleProjectDatabase().getId();

    given()
        .when()
        .get("/projects/" + projectId)
        .then()
        .statusCode(200)
        .contentType(HTML)
        .body(containsString("No builds have been run for this project yet."));
  }
}
