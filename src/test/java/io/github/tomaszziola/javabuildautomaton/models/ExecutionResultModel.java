package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;

public final class ExecutionResultModel {

  private ExecutionResultModel() {}

  public static ExecutionResult basic(final String log) {
    return new ExecutionResult(true, log + "'s ok\n");
  }

  public static ExecutionResult basic() {
    return new ExecutionResult(true, "pull's ok\n");
  }
}
