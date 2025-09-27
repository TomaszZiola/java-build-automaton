package io.github.tomaszziola.javabuildautomaton.api.exception;

import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import io.github.tomaszziola.javabuildautomaton.api.dto.ErrorResponse;
import io.github.tomaszziola.javabuildautomaton.buildsystem.WorkspaceException;
import io.github.tomaszziola.javabuildautomaton.buildsystem.exception.BuildNotFoundException;
import io.github.tomaszziola.javabuildautomaton.project.exception.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ProjectNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleProjectNotFound(
      final ProjectNotFoundException exception, final HttpServletRequest request) {
    final var errorResponse =
        new ErrorResponse(
            now(), NOT_FOUND.value(), "Not Found", exception.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, NOT_FOUND);
  }

  @ExceptionHandler(BuildNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBuildNotFound(
      final BuildNotFoundException exception, final HttpServletRequest request) {
    final var errorResponse =
        new ErrorResponse(
            now(), NOT_FOUND.value(), "Not Found", exception.getMessage(), request.getRequestURI());
    return new ResponseEntity<>(errorResponse, NOT_FOUND);
  }

  @ExceptionHandler(WorkspaceException.class)
  public ResponseEntity<ErrorResponse> handleWorkspaceException(
      final WorkspaceException exception, final HttpServletRequest request) {
    final var errorResponse =
        new ErrorResponse(
            now(),
            BAD_REQUEST.value(),
            "Workspace not prepared",
            exception.getMessage(),
            request.getRequestURI());
    return new ResponseEntity<>(errorResponse, NOT_FOUND);
  }
}
