package io.github.tomaszziola.javabuildautomaton.buildsystem.exception;

import java.io.Serial;

public class WorkspaceException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public WorkspaceException(String message) {
    super(message);
  }

  public WorkspaceException(String message, final Throwable cause) {
    super(message, cause);
  }
}
