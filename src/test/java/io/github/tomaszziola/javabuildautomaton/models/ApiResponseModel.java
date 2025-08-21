package io.github.tomaszziola.javabuildautomaton.models;

import static io.github.tomaszziola.javabuildautomaton.api.dto.ApiStatus.FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ApiResponse;

public final class ApiResponseModel {

  private ApiResponseModel() {}

  public static ApiResponse basic() {
    return new ApiResponse(
        FOUND, "Project found in the database: test-project-from-db. Build process started.");
  }
}
