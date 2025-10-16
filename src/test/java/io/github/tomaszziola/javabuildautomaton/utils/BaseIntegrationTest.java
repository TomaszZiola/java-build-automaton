package io.github.tomaszziola.javabuildautomaton.utils;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static io.github.tomaszziola.javabuildautomaton.models.ProjectModel.unpersisted;
import static java.util.List.of;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class BaseIntegrationTest {

  @Autowired private ProjectRepository projectRepository;

  @LocalServerPort int port;

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
    second.setBuildTool(MAVEN);

    return projectRepository.saveAll(of(first, second));
  }

  public Project populateSingleProjectDatabase() {
    final var project = unpersisted();
    return projectRepository.save(project);
  }
}
