package io.github.tomaszziola.javabuildautomaton;

import io.github.tomaszziola.javabuildautomaton.project.Project;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Bean
  public CommandLineRunner commandLineRunner(final ProjectRepository repository) {
    return args -> {
      final Project testProject = new Project();
      testProject.setName("test-project-from-db");
      testProject.setRepositoryName("TomaszZiola/test");
      testProject.setLocalPath("/Users/Tomasz/Documents/test");

      repository.save(testProject);

      LOGGER.info(">>> Dodano testowy projekt do bazy danych");
    };
  }
}
