package io.github.tomaszziola.javabuildautomaton.exception;

import java.io.Serial;
import java.io.Serializable;

public class BuildProcessException extends RuntimeException implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public BuildProcessException(final String message) {
    super(message);
  }
}
