package io.github.tomaszziola.javabuildautomaton.buildsystem.exception;

import java.io.Serial;
import java.io.Serializable;

public class BuildNotFoundException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public BuildNotFoundException(Long buildId) {
    super("Build not found with id: " + buildId);
  }
}
