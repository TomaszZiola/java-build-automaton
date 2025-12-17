package io.github.tomaszziola.javabuildautomaton.utils;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static io.github.tomaszziola.javabuildautomaton.models.ProjectModel.unpersisted;
import static java.nio.file.Files.createDirectories;
import static java.util.List.of;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.restassured.RestAssured;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class BaseIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
      new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ProjectRepository projectRepository;

  @LocalServerPort int port;

  @TempDir private static Path tmpWorkspaceDir;

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) throws IOException {
    createDirectories(tmpWorkspaceDir);
    registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("workspace.base-dir", tmpWorkspaceDir::toString);
  }

  @BeforeEach
  void cleanDb() {
    projectRepository.deleteAll();
    RestAssured.port = port;
  }

  public List<Project> populateManyProjectsDatabase() {
    final Project first = unpersisted();

    final var second = unpersisted();
    second.setUsername("RobertRoslik");
    second.setRepositoryName("java-awsomeapp");
    second.setRepositoryFullName("RobertRoslik/java-awsomeapp");
    second.setRepositoryUrl("https://github.com/RobertRoslik/java-awsomeapp.git");
    second.setWebhookSecret("topSecret");
    second.setBuildTool(MAVEN);

    return projectRepository.saveAll(of(first, second));
  }

  public Project populateSingleProjectDatabase() {
    final var project = unpersisted();
    return projectRepository.save(project);
  }
}
