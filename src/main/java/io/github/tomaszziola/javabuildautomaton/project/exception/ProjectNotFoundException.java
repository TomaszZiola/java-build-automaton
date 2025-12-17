package io.github.tomaszziola.javabuildautomaton.project.exception;

import java.io.Serial;
import java.io.Serializable;

public class ProjectNotFoundException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public ProjectNotFoundException(Long projectId) {
    super("Project not found with id: " + projectId);
  }
}
