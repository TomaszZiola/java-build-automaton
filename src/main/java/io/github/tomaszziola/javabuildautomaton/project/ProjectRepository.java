package io.github.tomaszziola.javabuildautomaton.project;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByRepositoryName(String repositoryName);
}
