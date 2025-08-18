package io.github.tomaszziola.javabuildautomaton.models;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;

public final class ApiResponseModel {

  private ApiResponseModel() {}

  public static ApiResponse basic() {
    return new ApiResponse("success", "Project found in the database: test-project-from-db");
  }
}
