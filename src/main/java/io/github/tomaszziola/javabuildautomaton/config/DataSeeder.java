package io.github.tomaszziola.javabuildautomaton.config;

import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.FAILED;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.IN_PROGRESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildStatus.SUCCESS;
import static io.github.tomaszziola.javabuildautomaton.buildsystem.BuildTool.GRADLE;

import io.github.tomaszziola.javabuildautomaton.buildsystem.BuildRepository;
import io.github.tomaszziola.javabuildautomaton.buildsystem.entity.Build;
import io.github.tomaszziola.javabuildautomaton.project.ProjectRepository;
import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DataSeeder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataSeeder.class);

  @Value("${app.seed.project.local-path}")
  private String seedProjectLocalPath;

  @Bean
  public CommandLineRunner commandLineRunner(
      final ProjectRepository projectRepository, final BuildRepository buildRepository) {
    return args -> {
      if (projectRepository.count() > 0) {
        LOGGER.info(">>> Database already seeded. Skipping.");
        return;
      }

      LOGGER.info(">>> Seeding database with test data...");

      final var testProject = new Project();
      testProject.setName("test-project-from-db");
      testProject.setRepositoryName("TomaszZiola/test");
      testProject.setLocalPath(seedProjectLocalPath);
      testProject.setBuildTool(GRADLE);
      projectRepository.save(testProject);

      final var build1 = new Build();
      build1.setProject(testProject);
      build1.setStatus(SUCCESS);
      build1.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
      build1.setEndTime(Instant.now().minus(2, ChronoUnit.HOURS).plus(90, ChronoUnit.SECONDS));
      build1.setLogs("Build successful...\n...details...");

      final var build2 = new Build();
      build2.setProject(testProject);
      build2.setStatus(FAILED);
      build2.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
      build2.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS).plus(45, ChronoUnit.SECONDS));
      build2.setLogs("Build failed...\n...error details...");

      final var build3 = new Build();
      build3.setProject(testProject);
      build3.setStatus(IN_PROGRESS);
      build3.setStartTime(Instant.now());
      build3.setEndTime(null);
      build3.setLogs("Build in progress...");

      buildRepository.saveAll(List.of(build1, build2, build3));

      LOGGER.info(">>> Test project and 3 sample builds added to database");
    };
  }
}
