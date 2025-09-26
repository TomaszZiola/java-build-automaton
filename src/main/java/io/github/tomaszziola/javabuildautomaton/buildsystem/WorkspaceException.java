package io.github.tomaszziola.javabuildautomaton.buildsystem;

public class WorkspaceException extends RuntimeException {
  public WorkspaceException(String message) {
    super(message);
  }

  public WorkspaceException(String message, Throwable cause) {
    super(message, cause);
  }
}
