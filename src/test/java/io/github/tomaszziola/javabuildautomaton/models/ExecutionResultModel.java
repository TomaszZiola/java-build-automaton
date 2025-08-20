package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;

public final class ExecutionResultModel {

  private ExecutionResultModel() {}

  public static ExecutionResult basic() {
    return new ExecutionResult(true, "everything's ok\n");
  }
}
