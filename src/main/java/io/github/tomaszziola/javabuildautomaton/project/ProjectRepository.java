package io.github.tomaszziola.javabuildautomaton.project;

import io.github.tomaszziola.javabuildautomaton.project.entity.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByRepositoryFullName(String repositoryFullName);
}
