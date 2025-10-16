package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.buildsystem.ExecutionResult;

public final class ExecutionResultModel {

  private ExecutionResultModel() {}

  public static ExecutionResult basic(String log) {
    return new ExecutionResult(true, log + "'s ok\n");
  }
}
