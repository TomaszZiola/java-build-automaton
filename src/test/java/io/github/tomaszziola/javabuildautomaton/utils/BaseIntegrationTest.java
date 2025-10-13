package io.github.tomaszziola.javabuildautomaton.utils;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.MAVEN;
import static io.github.tomaszziola.javabuildautomaton.models.ProjectModel.unpersisted;
import static org.hibernate.internal.util.collections.CollectionHelper.listOf;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class BaseIntegrationTest {

  @Autowired private ProjectRepository projectRepository;

  public void populateDatabase() {
    final Project projectOne = unpersisted();
    setField(projectOne, "buildTool", MAVEN);
    final Project projectSecond = unpersisted();
    setField(projectSecond, "username", "RobertRoslik");
    setField(projectSecond, "repositoryFullName", "RobertRoslik/java-awsomeapp");
    setField(projectSecond, "repositoryUrl", "https://github.com/RobertRoslik/java-awsomeapp.git");
    setField(projectSecond, "repositoryName", "java-awsomeapp");
    persist(listOf(projectOne, projectSecond));
  }

  private void persist(final List<Project> projects) {
    projectRepository.saveAll(projects);
  }
}
