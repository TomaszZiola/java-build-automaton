package io.github.tomaszziola.javabuildautomaton.buildsystem;

public class WorkspaceException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public WorkspaceException(final String message) {
    super(message);
  }

  public WorkspaceException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
