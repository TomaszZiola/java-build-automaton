package io.github.tomaszziola.javabuildautomaton.api.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ErrorResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import io.github.tomaszziola.javabuildautomaton.utils.BaseUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest extends BaseUnit {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("ProjectNotFoundException is mapped to 404 Not Found with proper payload")
  void projectNotFoundHandled() {
    // given
    final var exception = new ProjectNotFoundException(123L);

    // when
    final var response = handler.handleProjectNotFound(exception, httpServletRequest);

    // then
    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    final ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.status()).isEqualTo(404);
    assertThat(body.error()).isEqualTo("Not Found");
    assertThat(body.message()).contains("123");
    assertThat(body.path()).isEqualTo("/api/projects/123");
    assertThat(body.timestamp()).isNotNull();
  }

  @Test
  @DisplayName("BuildNotFoundException is mapped to 404 Not Found with proper payload")
  void buildNotFoundHandled() {
    // given
    final var exception = new BuildNotFoundException(123L);

    // when
    final var response = handler.handleBuildNotFound(exception, httpServletRequest);

    // then
    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    final ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.status()).isEqualTo(404);
    assertThat(body.error()).isEqualTo("Not Found");
    assertThat(body.message()).contains("123");
    assertThat(body.path()).isEqualTo("/api/projects/123");
    assertThat(body.timestamp()).isNotNull();
  }

  @Test
  @DisplayName("WorkspaceException is mapped to 400 Bad Request with proper payload")
  void workspaceExceptionHandled() {
    // given
    final var exception = new WorkspaceException("workspace failed");

    // when
    final var response = handler.handleWorkspaceException(exception, httpServletRequest);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    final ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.status()).isEqualTo(400);
    assertThat(body.error()).isEqualTo("Workspace not prepared");
    assertThat(body.message()).isEqualTo("workspace failed");
    assertThat(body.path()).isEqualTo("/api/projects/123");
    assertThat(body.timestamp()).isNotNull();
  }
}
