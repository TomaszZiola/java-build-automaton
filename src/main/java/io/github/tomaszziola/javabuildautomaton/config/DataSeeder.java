package io.github.tomaszziola.javabuildautomaton.config;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Bean
  public CommandLineRunner commandLineRunner(final ProjectRepository repository) {
    return args -> {
      final var testProject = new Project();
      testProject.setName("test-project-from-db");
      testProject.setRepositoryName("TomaszZiola/test");
      testProject.setLocalPath("/Users/Tomasz/Documents/IdeaProjects/test");
      testProject.setBuildTool(GRADLE);

      repository.save(testProject);

      LOGGER.info(">>> Test project added to database");
    };
  }
}
