package io.github.tomaszziola.javabuildautomaton.buildsystem;

import java.io.Serial;

public class WorkspaceException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public WorkspaceException(final String message) {
    super(message);
  }

  public WorkspaceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
